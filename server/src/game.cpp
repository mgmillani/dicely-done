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
	unsigned int v = h.values[0];
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
	this->currentPlayer = this->players.begin();
	this->round = Game::Round::INITIAL;
	this->needed = Game::Info::HAND;
	this->startScore = 45;
}
	
void Game::join(string player)
{
	Player *p = new Player(player, this->startScore);
	
	this->players.push_back(p);
	if(this->players.size() == 1)
		this->currentPlayer = this->players.begin();
}
void Game::quit(string player)
{
	for(list<Player*>::iterator it ; it!=players.end() ; it++)
	{
		Player *p = *it;
		if(p->name.compare(player) == 0)
		{
			players.erase(it);
			delete p;
			return;
		}		
	}
}
void Game::giveHand(t_hand hand)
{
	Player *p = *this->currentPlayer;
	ERR("Now playing: %s\n", p->name.c_str());
	Round next;
	switch(this->round)
	{
		case Round::INITIAL:
			p->hand = hand;
			this->currentPlayer++;
			next = Round::BET;
			break;
		case Round::RECAST:
			p->hand = hand;
			this->currentPlayer++;
			next = Round::RESULT;
			break;
		case Round::RESULT:
		case Round::MATCH:
		case Round::BET:
			break;
	}
		
	if(this->currentPlayer == this->players.end())
	{
		this->round = next;
		this->updateNeeded();
		this->currentPlayer = this->players.begin();
	}
}
void Game::giveBet(t_score bet)
{
	Player *p = *this->currentPlayer;
	ERR("Now playing: %s\n", p->name.c_str());
	Round next = this->round;;
	switch(this->round)
	{
		case Round::INITIAL:
			break;
		case Round::RECAST:				
			break;
		case Round::RESULT:
			break;
		case Round::MATCH:
			p->score -= bet;
			p->bet += bet;
			next = Round::RECAST;
			this->currentPlayer++;
			break;
		case Round::BET:
			p->score -= bet;
			p->bet += bet;
			next = Round::MATCH;
			this->currentPlayer++;
			break;
	}
		
	if(this->currentPlayer == this->players.end())
	{
		this->round = next;
		this->updateNeeded();
		this->currentPlayer = this->players.begin();
	}
}
void Game::giveAck()
{
	if(this->round == Round::RESULT)
	{
		//calculates the winner
		this->decideWinner();
		// winner gets the pot
		Player *p = *this->winner;
		ERR("%s won!\n", p->name.c_str());
		p->score += this->pot;
		// clears the pot
		this->pot = 0;
		// resets player bets
		for(list<Player*>::iterator it = this->players.begin() ; it!=this->players.end() ; it++)
		{
			(*it)->bet = 0;
		}
		// rotate player order
		this->players.push_back(*this->players.begin());
		this->players.pop_front();
		// start from first player
		this->currentPlayer = this->players.begin();
		// go back to the first round
		this->round = Round::INITIAL;
		this->updateNeeded();
	}
}
	
void Game::updateNeeded()
{
	switch(this->round)
	{
		case Round::INITIAL:
		case Round::RECAST:
			this->needed = Info::HAND;
			ERR("Throw your dice\n");
			break;
		case Round::BET:
		case Round::MATCH:
			this->needed = Info::BET;
			ERR("Place your bet\n");
			break;
		case Round::RESULT:
			this->needed = Info::ACK;
			ERR("Finish\n");
			break;
	}
}

void Game::decideWinner()
{
	int rank = (int)(Game::Rank::HIGHEST)+1;
	for(list<Player*>::iterator it=this->players.begin() ; it!=this->players.end() ; it++)
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
