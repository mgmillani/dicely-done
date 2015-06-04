#include <list>

#include "game.hpp"
#include "player.hpp"

#include  "magic.h"
#include "debug.h"

using namespace std;

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
	// dummy
	this->winner = this->players.begin();
}
