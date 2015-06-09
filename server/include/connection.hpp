#ifndef CONNECTION_HPP_
#define CONNECTION_HPP_

#include <list>
#include <random>

#include "player.hpp"

class Connection
{
public:
	Connection(int port, MultiGame *game);
	void receiveMessages();
	void join(const char *name);
	void ack();
	void bet(t_score val);
	void reroll(int *dice, int n);
	void quit();
	
	MultiGame *game;
	int port;
	std::default_random_engine generator;
	std::uniform_int_distribution<int> distribution;
	// TODO: change this to include address
	Player *sender; // the player who sent the last message
	std::list<Player*> players;
};

#endif
