#include <sstream>
#include <list>
#include <iterator>
#include <fstream>
#include <chrono>

#include <stdlib.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/fcntl.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>

#include "game.hpp"
#include "player.hpp"

#include  "magic.h"
#include "debug.h"

using namespace std;
using namespace std::chrono;

const string gStartGame = "startgame";
const string gStartTurn = "startturn";

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

Game::Game(t_score minBet, t_score startScore, int minPlayers)
{
	this->round = Game::Round::INITIAL;
	this->needed = Game::Info::NOTHING;
	this->pot = 0;
	this->minBet = minBet;
	this->playerBet = minBet;
	this->startScore = startScore;
	this->minPlayers = minPlayers;
}

bool Game::join(Player *playerA)
{
	for(list<Player*>::iterator it = this->players.begin() ; it!=players.end() ; it++)
	{
		Player *p = *it;
		if(p->name.compare(playerA->name) == 0)
			return false;
	}
	Player *player = new Player(playerA->name, this->startScore);
	player->socket = playerA->socket;
	this->players.push_back(player);
	this->informJoin(player);
	if(this->round == Round::INITIAL)
	{
		this->activePlayers.push_back(player);
		if(this->players.size() >= this->minPlayers)
		{
			 this->informStart();
			 this->informPlayer();
			 this->needed = Game::Info::HAND;
		}
	}
	if(this->players.size() == 1)
	{
		this->currentPlayer = this->activePlayers.begin();
	}
	this->pot += this->minBet;
	player->bet = this->minBet;
	player->score -= this->minBet;
	return true;
}

bool Game::join(string player)
{
	Player *p = new Player(player, this->startScore);
	if(!this->join(p))
	{
		delete p;
		return false;
	}
	return true;
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
			if((p->bet + bet >= this->playerBet || p->bet + bet == p->score) && p->score >= bet)
			{
				p->score -= bet;
				p->bet += bet;
				if(p->bet > this->playerBet)
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
			list<Player *>::iterator folder = this->currentPlayer;
			this->informFold(p);
			this->nextPlayer();
			this->activePlayers.erase(folder);
			if(this->activePlayers.size() == 1)
			{
				this->round = Round::RESULT;
				this->finish();
				this->informRound();
			}

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

void Game::giveAck(Player *player)
{
	if(this->round == Round::RESULT)
	{
		// checks if the player is already active
		bool found = false;
		for(list<Player*>::iterator it = this->activePlayers.begin() ; it!=this->activePlayers.end() && !found ; it++)
		{
			found = player->name.compare((*it)->name) == 0;
		}
		if(!found)
		{
			this->activePlayers.push_back(player);
		}
	}

	// if everyone agreed to restart
	if(this->activePlayers.size() == this->players.size())
	{
		this->restart();
	}
}

void Game::giveAck()
{
	if(this->round == Round::RESULT)
	{
		this->restart();
	}
}

void Game::restart()
{
	// clears the pot
	this->pot = 0;
	// resets player bets
	for(list<Player*>::iterator it = this->players.begin() ; it!=this->players.end() ; it++)
	{
		Player *p = *it;
		if(p->score > 0)
		{
			p->bet = this->minBet;
			p->score -= this->minBet;
			pot += this->minBet;
		}
		else
			p->bet = 0;
	}
	this->playerBet = this->minBet;
	// rotate player order
	this->players.push_back(*this->players.begin());
	this->players.pop_front();
	this->activePlayers = this->players;
	// start from first player
	this->currentPlayer = this->activePlayers.begin();
	// go back to the first round
	this->round = Round::INITIAL;
	this->updateNeeded();
	this->informStart();
	this->informPlayer();
	this->informRound();

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
	int sum;
	for(list<Player*>::iterator it=this->activePlayers.begin() ; it!=this->activePlayers.end() ; it++)
	{
		Player *p = *it;
		p->rank = getRank(p);
		if((int)p->rank < rank)
		{
			rank = (int)p->rank;
			sum = 0;
			for(int i=0 ; i<p->hand.len ; i++)
				sum += p->hand.values[i];
			this->winner = it;
		}
		// in case of a tie
		else if((int)p->rank == rank)
		{
			int sum2 = 0;
			for(int i=0 ; i<p->hand.len ; i++)
				sum2 += p->hand.values[i];
			if(sum2 > sum)
			{
				sum = sum2;
				this->winner = it;
			}
		}
	}
}

void Game::nextPlayer()
{
	this->currentPlayer++;

	if(this->currentPlayer == this->activePlayers.end())
	{
		this->currentPlayer = this->activePlayers.begin();
		this->nextRound();
		this->updateNeeded();
	}
	this->informPlayer();
}

void Game::finish()
{
	this->round = Round::RESULT;
	//calculates the winner
	this->decideWinner();
	// winner gets the pot
	Player *p = *this->winner;
	p->score += this->pot;
	this->informWinner();
	this->activePlayers.clear();
	this->updateNeeded();
}

void Game::nextRound()
{
	switch(this->round)
	{
		case Round::INITIAL: this->round = Round::BET;     break;
		case Round::RECAST:  this->round = Round::RESULT;  this->finish(); break;
		case Round::BET:     this->round = Round::MATCH;   break;
		case Round::MATCH:   this->round = Round::RECAST;  break;
		case Round::RESULT:  this->round = Round::INITIAL; break;
	}
	this->updateNeeded();
	this->informRound();
}

Game::Info Game::getNeeded()
{
	return this->needed;
}

bool Game::isPlayerTurn(Player *player)
{
	return player->name.compare((*this->currentPlayer)->name) == 0;
}

void Game::informStart(Player *player)
{
	printf("startgame %d\n", this->minBet);
}

void Game::informStart()
{
	printf("startgame %d\n", this->minBet);
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

MultiGame::MultiGame(t_score minBet, t_score startScore, int minPlayers)
{
	this->round = Game::Round::INITIAL;
	this->needed = Game::Info::NOTHING;
	this->pot = 0;
	this->minBet = minBet;
	this->playerBet = minBet;
	this->startScore = startScore;
	this->minPlayers = minPlayers;
}

void MultiGame::add(Game *game)
{
	this->games.push_back(game);
}

bool MultiGame::isPlayerTurn(Player *player)
{
	list<Game*>::iterator it = this->games.begin();
	Game *g = *it;
	return g->isPlayerTurn(player);
}

bool MultiGame::join(std::string player)
{
	bool retval = true;
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		retval = (*it)->join(player);
	}
	return retval;
}

bool MultiGame::join(Player *player)
{
	bool retval = true;
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		retval = (*it)->join(player);
	}
	return retval;
}

void MultiGame::quit(std::string player)
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->quit(player);
	}
}
void MultiGame::restart()
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->restart();
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
void MultiGame::giveAck(Player *player)
{
	for(list<Game*>::iterator it = this->games.begin() ; it!=this->games.end() ; it++)
	{
		(*it)->giveAck(player);
	}
}
Game::Info MultiGame::getNeeded()
{
	Game *g = *(this->games.begin());
	return g->getNeeded();
}

/**
 * ==========================
 * Local Game with log file
 * ==========================
 */

LocalGame::LocalGame(t_score minBet, t_score startScore, int minPlayers)
{
	this->round = Game::Round::INITIAL;
	this->needed = Game::Info::NOTHING;
	this->pot = 0;
	this->minBet = minBet;
	this->playerBet = minBet;
	this->startScore = startScore;
	this->minPlayers = minPlayers;
	stringstream ss;
	time_t t = time(0);
	struct tm * now = localtime( & t );
	char date[1024];
	sprintf(date, "%04d-%02d-%02d_%02d-%02d-%02d",(now->tm_year + 1900), (now->tm_mon + 1), now->tm_mday, now->tm_hour, now->tm_min, now->tm_sec);
	ss << "dicely-done_" << date << ".log";
	ERR("log: %s\n", ss.str().c_str());
	this->logFile = std::ofstream(ss.str(), std::ofstream::out);
	this->gameStart = steady_clock::now();
}

LocalGame::~LocalGame()
{
	this->logFile.close();
}

LocalGame::LocalGame() : Game::Game()
{
	/*this->window = gtk_window_new(GTK_WINDOW_TOPLEVEL);
	gtk_window_set_title(GTK_WINDOW(window), "Center");
	gtk_window_set_default_size(GTK_WINDOW(window), 230, 150);
	gtk_window_set_position(GTK_WINDOW(window), GTK_WIN_POS_CENTER);
	gtk_widget_show(window);
	g_signal_connect_swapped(G_OBJECT(window), "destroy", G_CALLBACK(gtk_main_quit), NULL);*/
	stringstream ss;
	time_t t = time(0);
	struct tm * now = localtime( & t );
	char date[1024];
	sprintf(date, "%04d-%02d-%02d_%02d-%02d-%02d",(now->tm_year + 1900), (now->tm_mon + 1), now->tm_mday, now->tm_hour, now->tm_min, now->tm_sec);
	ss << "dicely-done_" << date << ".log";
	ERR("log: %s\n", ss.str().c_str());
	this->logFile = std::ofstream(ss.str(), std::ofstream::out);
	this->gameStart = steady_clock::now();
}

void LocalGame::informStart()
{
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();
	Player *p = *this->currentPlayer;

	this->logFile << dt << "s ";
	this->logFile << p->name << " ";
	this->logFile << "startgame " << this->minBet << "\n";
	this->logFile.flush();
}
void LocalGame::informStart(Player *p)
{
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();

	this->logFile << dt << "s ";
	this->logFile << p->name << " ";
	this->logFile << "startgame " << this->minBet << "\n";
	this->logFile.flush();
}
void LocalGame::informPlayer()
{
	if(this->round == Round::RESULT)
		return;

	Player *p = *this->currentPlayer;
	stringstream msg;
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();
	this->logFile << dt << "s ";
	this->logFile << p->name << " ";

	this->logFile <<  "startturn";
	this->logFile << " " << (int)this->round;
	string msgStr;
	switch(this->round)
	{
		case Round::INITIAL:
		case Round::RECAST:
			this->logFile << "\n";
			break;
		case Round::RESULT: break;
		case Round::BET:
		case Round::MATCH:
			this->logFile << " " << this->playerBet << "\n";
			break;
	}
	this->logFile.flush();
}

void LocalGame::informRound()
{
	this->informPlayer();
}

void LocalGame::informWinner()
{
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();
	Player *p = *this->currentPlayer;
	this->logFile << dt << "s ";
	this->logFile << p->name << " ";
	this->logFile << "endgame " << (*this->winner)->name << " " << this->pot << "\n";
	this->logFile.flush();
}

void LocalGame::informHand(Player *p)
{
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();
	this->logFile << dt << "s ";
	this->logFile << p->name << " ";

	this->logFile << "dice " << p->name;
	for(int i=0 ; i<p->hand.len ; i++)
	{
		this->logFile << " " << p->hand.values[i];
	}

	this->logFile << "\n";
	this->logFile.flush();
}

void LocalGame::informBet(Player *p)
{
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();
	this->logFile << dt << "s ";
	this->logFile << p->name << " ";

	this->logFile << "betplaced " << p->name << " " << p->bet << " " << this->pot << "\n";
	this->logFile.flush();

}

void LocalGame::informFold(Player *p)
{
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();
	this->logFile << dt << "s ";
	this->logFile << p->name << " ";
	this->logFile << "folded " << p->name << "\n";
	this->logFile.flush();
}

void LocalGame::informQuit(Player *p)
{
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();
	this->logFile << dt << "s ";
	this->logFile << p->name << " ";
	this->logFile << "quit " << p->name << "\n";
	this->logFile.flush();
}

void LocalGame::informJoin(Player *p)
{
	time_point<steady_clock> t1 = steady_clock::now();
	duration<double> elapsed_sec = t1 - this->gameStart;
	double dt = elapsed_sec.count();
	this->logFile << dt << "s ";
	this->logFile << p->name << " ";
	this->logFile << "joined " << p->name << "\n";
	this->logFile.flush();
}



/***************
 * REMOTE GAME *
 ***************/

RemoteGame::RemoteGame(t_score minBet, t_score startScore, int minPlayers)
{
	this->round = Game::Round::INITIAL;
	this->needed = Game::Info::NOTHING;
	this->pot = 0;
	this->minBet = minBet;
	this->playerBet = minBet;
	this->startScore = startScore;
	this->minPlayers = minPlayers;
}

void RemoteGame::broadcast(string msg)
{
	for(list<Player*>::iterator it = this->players.begin() ; it!=this->players.end() ; it++)
	{
		Player *player = *it;
		send(player->socket, msg.c_str(), msg.size(), 0);
	}
}

void RemoteGame::informStart(Player *player)
{
	stringstream ss;
	ss << "startgame " << this->minBet << "\n";
	string msg = ss.str();
	send(player->socket, msg.c_str(), msg.size(), 0);
}

void RemoteGame::informStart()
{
	stringstream ss;
	ss << "startgame " << this->minBet << "\n";
	string msg = ss.str();
	for(list<Player*>::iterator it = this->players.begin() ; it!=this->players.end() ; it++)
	{
		Player *player = *it;
		send(player->socket, msg.c_str(), msg.size(), 0);
	}

}
void RemoteGame::informPlayer()
{
	if(this->round == Round::RESULT)
		return;

	Player *p = *this->currentPlayer;
	stringstream msg;
	msg << "startturn";
	msg << " " << (int)this->round;
	string msgStr;
	switch(this->round)
	{
		case Round::INITIAL:
		case Round::RECAST:
			msg << "\n";
			msgStr = msg.str();
			send(p->socket, msgStr.c_str(), msgStr.size(), 0);
			break;
		case Round::RESULT: break;
		case Round::BET:
		case Round::MATCH:
			msg << " " << this->playerBet << "\n";
			msgStr = msg.str();
			send(p->socket, msgStr.c_str(), msgStr.size(), 0);
			break;
	}
}

void RemoteGame::informRound()
{
	this->informPlayer();
/*
	stringstream msg;
	msg << "startturn";
	msg << " " << (int)this->round;
	switch(this->round)
	{
		case Round::INITIAL:
		case Round::RECAST:
			msg << "\n";
			this->broadcast(msg.str());
			break;
		case Round::RESULT: break;
		case Round::BET:
		case Round::MATCH:
			msg << " " << this->playerBet << "\n";
			this->broadcast(msg.str());
			break;
	}
*/
}

void RemoteGame::informWinner()
{
	stringstream ss;
	ss << "endgame " << (*this->winner)->name << " " << this->pot << "\n";
	this->broadcast(ss.str());
}

void RemoteGame::informHand(Player *p)
{
	stringstream ss;
	ss << "dice " << p->name;
	for(int i=0 ; i<p->hand.len ; i++)
	{
		ss << " " << p->hand.values[i];
	}

	ss << "\n";
	this->broadcast(ss.str());
}

void RemoteGame::informBet(Player *p)
{
	stringstream ss;
	ss << "betplaced " << p->name << " " << p->bet << " " << this->pot << "\n";
	this->broadcast(ss.str());
}

void RemoteGame::informFold(Player *p)
{
	stringstream ss;
	ss << "folded " << p->name << "\n";
	this->broadcast(ss.str());
}

void RemoteGame::informQuit(Player *p)
{
	stringstream ss;
	ss << "quit " << p->name << "\n";
	this->broadcast(ss.str());
}

void RemoteGame::informJoin(Player *p)
{
	stringstream ss;
	ss << "joined " << p->name << "\n";
	this->broadcast(ss.str());

	for(auto it=this->players.begin() ; it!=this->players.end() ; it++)
	{
		Player *p2 = *it;
		if(p->name.compare(p2->name) != 0)
		{
			stringstream ss;
			ss << "joined " << p2->name << "\n";
			send(p->socket, ss.str().c_str(), ss.str().size(), 0);
		}
	}

}
