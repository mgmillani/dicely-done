#include <string>
#include <random>

#include "connection.hpp"

Connection::Connection(int port, MultiGame *game)
{
	this->port = port;
	this->game = game;
	this->distribution = std::uniform_int_distribution<int>(1,6);
}
void Connection::receiveMessages()
{
	// for every incoming package
	
	//// store address if it is a new client (join)
	//// this->players.push_back(player);
	
	//// store who sent it
		
	//// pick correct method to parse package
	/*
	 * this->join(name);
	 * this->ack(); 
	 * ...
	 */
	
}
void Connection::join(const char *name)
{
	std::string s(name);
	this->game->join(s);
}
void Connection::ack()
{
	if(this->game->needed == Game::Info::ACK)
		this->game->giveAck();
}
void Connection::bet(t_score val)
{
	if(this->game->needed == Game::Info::BET)
		this->game->giveBet(val);
}
void Connection::reroll(int *dice, int n)
{
	t_hand h;
	for(int i=0 ; i<n ; i++)
		h.values[i] = dice[i];
	for(int i=n ; n<5 ; i++)
		h.values[i] = this->distribution(generator);
		
	h.len = 5;
	if(this->game->needed == Game::Info::HAND)
	{
		this->game->giveHand(h);
		this->game->giveHandAck();
	}
	
}
void Connection::quit()
{
	
}
