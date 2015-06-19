#ifndef CONNECTION_HPP_
#define CONNECTION_HPP_

#include <list>
#include <random>

#include "player.hpp"

class Connection
{
public:
	Connection(int port, MultiGame *game, bool verbose);
	void receiveMessages();
	void join(Player *player);
	void ack(Player *player);
	void bet(Player *player, t_score val);
	void restart(Player *player);
	void reroll(Player *player, t_hand h);
	void roll(Player *player);
	void quit(Player *player);
	void setupSocket();
	int doTcp(char *buffer, size_t bufferLen);
	void newConnections();
	
	MultiGame *game;
	int tcpPort;
	int tcpSocket;
	bool verbose;
	std::default_random_engine generator;
	std::uniform_int_distribution<int> distribution;
	// TODO: change this to include address
	Player *sender; // the player who sent the last message
	std::list<Player*> players;
};

#endif
