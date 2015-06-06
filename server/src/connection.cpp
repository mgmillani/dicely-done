#include <string>

#include "connection.hpp"

Connection::Connection(int port, RemoteGame *game)
{
	this->port = port;
	this->game = game;
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

}
void Connection::bet(t_score val)
{
	this->game->giveBet(val);
}
void Connection::reroll(int *dice, int n)
{
	
}
void Connection::quit()
{
	
}
