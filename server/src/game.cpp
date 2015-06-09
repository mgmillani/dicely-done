#include <stdlib.h>
#include <list>

#include "game.hpp"
#include "player.hpp"

#include  "magic.h"
#include "debug.h"

using namespace std;

bool comparePlayerHand(Player *first, Player *second)
{
	if(first->rank == Game::Rank::NONE)
		first->rank = getRank(first);
	if(second->rank == Game::Rank::NONE)
		second->rank = getRank(second);
	if(first->rank != second->rank)
		return first->rank < second->rank;
	
	// if they have the same rank, we need to use some undraw criterion.
	return true;
}

int compareUint(const void *a, const void *b)
{
	return *((unsigned int *)a) - *((unsigned int *)b);
}

Game::Rank getRank(const Player *p)
{
	static t_hand fibonacci;
	static unsigned int powerOfTwo[] = {2,4,8,16,32,64,128};
	static unsigned int primes[] = {2,3,5,7,11,13,17,19,23,29,31,37};
	fibonacci.len = 5;
	fibonacci.values[0] = 1;
	fibonacci.values[1] = 1;
	fibonacci.values[2] = 2;
	fibonacci.values[3] = 3;
	fibonacci.values[4] =	5;
	t_hand h = p->hand;
	// if it is fibonacci
	if(sameHand(&h, &fibonacci))
		return Game::Rank::FIBONACCI;
	// power of two
	unsigned int sum = 0;
	for(int i=0; i< h.len ; i++)
		sum += h.values[i];
	unsigned int *n = (unsigned int *)bsearch(&sum, powerOfTwo, sizeof(powerOfTwo)/sizeof(powerOfTwo[0]), sizeof(powerOfTwo[0]), compareUint);
	if(n != NULL)
		return Game::Rank::POWER_OF_TWO;
	// primes
	n = (unsigned int *)bsearch(&sum, primes, sizeof(primes)/sizeof(primes[0]), sizeof(primes[0]), compareUint);
	if(n != NULL)
		return Game::Rank::PRIME;
	// all odd / even
	bool sameParity = true;
	for( int i=1; i< h.len && sameParity ; i++)
	{
		if(h.values[i]%2 != h.values[i-1])
			sameParity = false;
	}
	if(sameParity)
		return Game::Rank::EVEN_ODD;
	// straight
	qsort(h.values, h.len, sizeof(h.values[0]), compareUint);
	bool straight = true;
	for( int i=0; i< h.len-1 && straight ; i++)
	{
		if(h.values[i] != h.values[i+1] -1)
			straight = false;
	}
	if(straight)
		return Game::Rank::STRAIGHT;
	// full house
	int v = h.values[0];
	int row = 1;
	bool pair = false;
	bool toak = false;
	for( int i=1; i< h.len ; i++)
	{
		if(h.values[i] == v)
			row++;
		else
		{
			if(row == 2)
				pair = true;
			else if(row == 3)
				toak = true;
			v = h.values[i];
			row = 1;
		}
	}
	if(pair && toak)
	{
		return Game::Rank::FULL_HOUSE;
	}
	
	// nothing
	return Game::Rank::HIGHEST;
	
}

Game::Game()
{
	this->round = Game::Round::INITIAL;
	this->needed = Game::Info::NOTHING;
	this->pot = 0;
	this->minBet = 5;
	this->playerBet = minBet;
	this->startScore = 45;
}
	
void Game::join(string player)
{
	Player *p = new Player(player, this->startScore);
	this->players.push_back(p);
	if(this->round == Round::INITIAL)
		this->activePlayers.push_back(p);
	if(this->players.size() == 1)
	{
		this->currentPlayer = this->activePlayers.begin();
		this->needed = Game::Info::HAND;
		this->informPlayer();
	}
	this->pot += this->minBet;
	p->bet = this->minBet;
	this->informJoin(p);
}

void Game::quit(string player)
{
	for(list<Player*>::iterator it = this->activePlayers.begin() ; it!=activePlayers.end() ; it++)
	{
		Player *p = *it;
		if(p->name.compare(player) == 0)
		{
			this->activePlayers.erase(it);
			break;
		}
	}
	for(list<Player*>::iterator it = this->players.begin() ; it!=players.end() ; it++)
	{
		Player *p = *it;
		if(p->name.compare(player) == 0)
		{
			this->informQuit(p);
			players.erase(it);			
			delete p;
			return;
		}		
	}
}

void Game::giveHand(t_hand hand)
{
	Player *p = *this->currentPlayer;
	switch(this->round)
	{
		case Round::INITIAL:
			if(hand.len == 5)
			{
				for(int i=0 ; i <hand.len ; i++)
					if(hand.values[i] == 0)
						return;
				p->hand = hand;
				this->informHand(p);
				this->needed = Game::Info::HAND_ACK;
			}
			break;
		case Round::RECAST:
			if(hand.len == 5)
			{
				p->hand = hand;
				for(int i=0 ; i <hand.len ; i++)
					if(hand.values[i] == 0)
						return;
				this->informHand(p);
				this->needed = Game::Info::HAND_ACK;
			}
			break;
		case Round::RESULT:
		case Round::MATCH:
		case Round::BET:
			break;
	}
}
void Game::giveBet(t_score bet)
{
	Player *p = *this->currentPlayer;
	switch(this->round)
	{
		case Round::INITIAL:
			break;
		case Round::RECAST:				
			break;
		case Round::RESULT:
			break;
		case Round::MATCH:
			if(p->bet + bet > this->playerBet)
				break;
		case Round::BET:
			if(p->bet + bet >= this->playerBet)
			{
				p->score -= bet;
				p->bet += bet;
				this->playerBet = p->bet;
				this->pot += bet;
				this->informBet(p);
				this->nextPlayer();
			}
			break;
	}
}

void Game::giveFold()
{
	Player *p = *this->currentPlayer;
	switch(this->round)
	{
		case Round::INITIAL:
		case Round::RECAST:
		case Round::RESULT:
			break;
		case Round::MATCH:			
		case Round::BET:
			this->activePlayers.erase(this->currentPlayer);
			this->informFold(p);			
			this->nextPlayer();
			break;
	}
}

void Game::giveHandAck()
{
	switch(this->round)
	{
		case Round::INITIAL:
		case Round::RECAST:
			this->needed = Game::Info::HAND;
			this->nextPlayer();
			break;
		default:
			break;
	}
}

void Game::giveAck()
{
	switch(this->round)
	{

		case Round::RESULT:
			// clears the pot
			this->pot = 0;
			// resets player bets
			for(list<Player*>::iterator it = this->players.begin() ; it!=this->players.end() ; it++)
			{
				(*it)->bet = this->minBet;
				pot += this->minBet;
				this->playerBet = this->minBet;
			}
			// rotate player order
			this->players.push_back(*this->players.begin());
			this->players.pop_front();
			this->activePlayers = this->players;
			// start from first player
			this->currentPlayer = this->activePlayers.begin();
			// go back to the first round
			this->round = Round::INITIAL;
			this->updateNeeded();
			this->informPlayer();
			this->informRound();
			break;
		default:
			break;
	}
}
	
void Game::updateNeeded()
{
	switch(this->round)
	{
		case Round::INITIAL:
		case Round::RECAST:
			this->needed = Info::HAND;
			break;
		case Round::BET:
		case Round::MATCH:
			this->needed = Info::BET;
			break;
		case Round::RESULT:
			this->needed = Info::ACK;
			break;
	}
}

void Game::decideWinner()
{
	int rank = (int)(Game::Rank::HIGHEST)+1;
	for(list<Player*>::iterator it=this->activePlayers.begin() ; it!=this->activePlayers.end() ; it++)
	{
		Player *p = *it;
		p->rank = getRank(p);
		if((int)p->rank < rank)
		{
			rank = (int)p->rank;
			this->winner = it;
		}
	}
}

void Game::nextPlayer()
{
	this->currentPlayer++;	
	
	if(this->currentPlayer == this->activePlayers.end())
	{
		this->nextRound();
		this->updateNeeded();
		this->currentPlayer = this->activePlayers.begin();
	}
	this->informPlayer();
}

void Game::nextRound()
{
	switch(this->round)
	{
		case Round::INITIAL: this->round = Round::BET; break;
		case Round::RECAST:  this->round = Round::RESULT;
		{
			//calculates the winner
			this->decideWinner();
			// winner gets the pot
			Player *p = *this->winner;
			p->score += this->pot;
			this->informWinner();
		}
			break;
		case Round::BET:     this->round = Round::MATCH; break;
		case Round::MATCH:   this->round = Round::RECAST; break;
		case Round::RESULT:  this->round = Round::INITIAL; break;
	}
	this->updateNeeded();
	this->informRound();
}

Game::Info Game::getNeeded()
{
	return this->needed;
}

void Game::informStart()
{
	printf("startgame\n");
}
void Game::informPlayer()
{
	//printf("Playing: %s\n", (*this->currentPlayer)->name.c_str());
}

void Game::informRound()
{
	
	switch(this->round)
	{
		case Round::INITIAL: 
		case Round::RECAST:  
			printf("startturn %d\n", (int)this->round);
			break;
		case Round::RESULT: break;
		case Round::BET:     
		case Round::MATCH:   printf("startturn %d %u\n", (int)this->round, this->playerBet); break;
		
	}
}

void Game::informWinner()
{
	printf("endgame %s %d\n", (*this->winner)->name.c_str(), this->pot);
}

void Game::informHand(Player *p)
{
	printf("dice %s ", p->name.c_str());
	for(int i=0 ; i<p->hand.len ; i++)
	{
		printf("%d ", p->hand.values[i]);
	}
	printf("\n");
}

void Game::informBet(Player *p)
{
	printf("betplaced %s %d %d\n", p->name.c_str(), p->bet, this->pot);
}

void Game::informFold(Player *p)
{
	printf("folded %s\n", p->name.c_str());
}

void Game::informQuit(Player *p)
{
	printf("quit %s\n", p->name.c_str());
}

void Game::informJoin(Player *p)
{
	printf("join %s\n", p->name.c_str());
}

/**
 * ========================
 * Game with multiple games
 * ======================== 
 */

void MultiGame::add(Game *game)
{
	this->games.push_back(game);
}

void MultiGame::join(std::string player)
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->join(player);
	}
}
void MultiGame::quit(std::string player)
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->quit(player);
	}
}
void MultiGame::giveHand(t_hand hand)
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->giveHand(hand);
	}
}
void MultiGame::giveHandAck()
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->giveHandAck();
	}
}
void MultiGame::giveBet(t_score bet)
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->giveBet(bet);
	}
}
void MultiGame::giveFold()
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->giveFold();
	}
}
void MultiGame::giveAck()
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->giveAck();
	}
}
Game::Info MultiGame::getNeeded()
{
	Game *g = *(this->games.begin());
	return g->getNeeded();
}

/**
 * ==========================
 * Local Game with GTK window
 * ==========================
 */
 
LocalGame::LocalGame() : Game::Game()
{
	/*this->window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
	gtk_window_set_title(GTK_WINDOW(window), "Center");
	gtk_window_set_default_size(GTK_WINDOW(window), 230, 150);
	gtk_window_set_position(GTK_WINDOW(window), GTK_WIN_POS_CENTER);
	gtk_widget_show(window);
	g_signal_connect_swapped(G_OBJECT(window), "destroy", G_CALLBACK(gtk_main_quit), NULL);*/
} 

void LocalGame::informStart()
{
	
}
void LocalGame::informPlayer()
{
	
}

void LocalGame::informRound()
{
	
	switch(this->round)
	{
		case Round::INITIAL: 
		case Round::RECAST:  
			printf("startturn %d\n", (int)this->round);
			break;
		case Round::RESULT: break;
		case Round::BET:     
		case Round::MATCH:   printf("startturn %d %u\n", (int)this->round, this->playerBet); break;
		
	}
}

void LocalGame::informWinner()
{
	printf("endgame %s %d\n", (*this->winner)->name.c_str(), this->pot);
}

void LocalGame::informHand(Player *p)
{
	printf("dice %s ", p->name.c_str());
	for(int i=0 ; i<p->hand.len ; i++)
	{
		printf("%d ", p->hand.values[i]);
	}
	printf("\n");
}

void LocalGame::informBet(Player *p)
{
	printf("betplaced %s %d %d\n", p->name.c_str(), p->bet, this->pot);
}

void LocalGame::informFold(Player *p)
{
	printf("folded %s\n", p->name.c_str());
}

void LocalGame::informQuit(Player *p)
{
	printf("quit %s\n", p->name.c_str());
}

void LocalGame::informJoin(Player *p)
{
	printf("join %s\n", p->name.c_str());
}
