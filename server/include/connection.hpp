#ifndef CONNECTION_HPP_
#define CONNECTION_HPP_

#include <list>

#include "player.hpp"

class Connection
{
public:
	Connection(int port, RemoteGame *game);
	void receiveMessages();
	void join(const char *name);
	void ack();
	void bet(t_score val);
	void reroll(int *dice, int n);
	void quit();
	
	RemoteGame *game;
	int port;
	// TODO: change this to include address
	Player *sender; // the player who sent the last message
	std::list<Player*> players;
};

#endif
