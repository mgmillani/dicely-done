#include <list>
#include <string>
#include <random>

#include <time.h>
#include <stdlib.h>
#include <string.h>

#include <sys/types.h>
#include <sys/socket.h>
#include <sys/fcntl.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>

#include <unistd.h>

#include "connection.hpp"

#include "debug.h"

#define MSG_SIZE 64

Connection::Connection(int port, MultiGame *game, bool verbose)
{
	
	this->verbose = verbose;
	this->generator.seed(time(NULL));
	this->tcpPort = port;
	this->game = game;
	this->distribution = std::uniform_int_distribution<int>(1,6);
	this->setupSocket();
}

void Connection::setupSocket()
{
	struct hostent *he;
	//Resolv hostname to IP Address
	if ((he=gethostbyname("0.0.0.0")) == NULL) {  // get the host info
			herror("gethostbyname");
	}

	struct sockaddr_in tcpAddr;
	memset( &tcpAddr, '\0', sizeof(tcpAddr));
	
	bool fail = true;
	int attempts = 0;
	
	while(fail)
	{
		attempts++;
		
		tcpAddr.sin_family = AF_INET;
		tcpAddr.sin_addr = *((struct in_addr *)he->h_addr);
		tcpAddr.sin_port = htons(this->tcpPort);
		
		ERR("Server port: %d\n", this->tcpPort);
		this->tcpSocket = socket(PF_INET, SOCK_STREAM, 0);
		if(fcntl(this->tcpSocket, F_SETFL, O_NONBLOCK) == -1)
		{
			TRACE("Error: %s (%d)\n", strerror(errno), errno);
		}
	
		if( -1 == bind(this->tcpSocket, (struct sockaddr *) &tcpAddr, sizeof(tcpAddr) ) )
		{
			TRACE("Error: %s (%d)\n", strerror(errno), errno);
			this->tcpPort++;
			close(this->tcpSocket);
		}
		else
		{
			listen(this->tcpSocket, SOMAXCONN);
			fail = false;
		}
	}
}

void Connection::newConnections()
{
	int newSocket = accept(this->tcpSocket, NULL, NULL);
	if(newSocket < 0 && (errno == EAGAIN || errno == EWOULDBLOCK))
	{
		//TRACE("Error: %s (%d)\n", strerror(errno), errno);
		return;
	}
	if(fcntl(newSocket, F_SETFL, O_NONBLOCK) == -1)
	{
		TRACE("Error: %s (%d)\n", strerror(errno), errno);
		return;
	}
	
	Player *p = new Player("",0);
	p->socket = newSocket;
	this->players.push_back(p);
	
	if(this->verbose)
	{
		ERR("new client on socket %d\n", p->socket);
	}
	
}

void Connection::receiveMessages()
{
	this->newConnections();
	char buffer[MSG_SIZE];
	for(std::list<Player*>::iterator it=this->players.begin() ; it != this->players.end() ; it++)
	{
		Player *player = *it;
		this->sender = player;
		int socket = player->socket;
		ssize_t s = recv(socket, buffer, sizeof(buffer)-1, 0);
		typedef enum e_state {S_TYPE, S_BET, S_REROLL, S_JOIN, S_SKIP} e_state;
		e_state state = S_TYPE;
		t_hand hand;
		hand.len = 0;
		while( s > 0 )
		{
			buffer[s] = '\0';
			if(this->verbose)
			{
				ERR("Player <%s> on socket %d sent '%s'\n", player->name.c_str(), player->socket, buffer);
			}
			
			char *word = strtok(buffer, " \n\t\r");
			if(word != NULL)
			{
				do
				{
					switch(state)
					{
						// reads the type of the message
						case S_TYPE:
							if     (strcmp(word, "bet") == 0)
								state = S_BET;
							else if(strcmp(word, "reroll") == 0)
								state = S_REROLL;
							else if(strcmp(word, "restart") == 0)
							{
								state = S_SKIP;
								this->restart(player);
							}
							else if(strcmp(word, "quit") == 0)
							{
								state = S_SKIP;
								this->quit(player);
							}
							else if(strcmp(word, "join") == 0)
								state = S_JOIN;
							else if(strcmp(word, "roll") == 0)
							{
								state = S_SKIP;
								this->roll(player);
							}
							else if(strcmp(word, "fold") == 0)
							{
								state = S_SKIP;
								this->fold(player);
							}
							break;
							// reads the name of the player
						case S_JOIN:
							player->name = std::string(word);
							this->join(player);
							break;
							// reads dice
						case S_REROLL:
							hand.values[hand.len] = atoi(word);
							hand.len++;
							break;
						case S_BET:
							this->bet(player, atoi(word));
							break;
						case S_SKIP:
							break;						
					}
				}while(NULL != (word = strtok(NULL, " \n\t\r")) && state != S_SKIP );
			}
			s = recv(socket, buffer, sizeof(buffer), 0);
		}
		if(s==0)
		{
			if(this->verbose)
			{
				ERR("Player <%s> on socket %d disconnected\n", player->name.c_str(), player->socket);
			}
			it = this->players.erase(it);
			close(player->socket);
			delete player;			
		}
		else
		{
			if(state == S_REROLL)
				this->reroll(player, hand);
		}
	}
	
}
void Connection::join(Player *player)
{
	this->game->join(player);
}
void Connection::ack(Player *player)
{
	if(this->game->getNeeded() == Game::Info::ACK && this->game->isPlayerTurn(player))
		this->game->giveAck();
}
void Connection::restart(Player *player)
{
	if(this->game->getNeeded() == Game::Info::ACK)
		this->game->giveAck(player);
}
void Connection::fold(Player *player)
{

	if(this->game->isPlayerTurn(player))
	{
		this->game->giveFold();
	}
}

void Connection::roll(Player *player)
{

	if(this->game->getNeeded() == Game::Info::HAND && this->game->isPlayerTurn(player))
	{
		t_hand hand;
		for(int i=0 ; i<5 ; i++)
			hand.values[i] = this->distribution(generator);
		hand.len = 5;
	
		this->game->giveHand(hand);
		this->game->giveHandAck();
	}
}
void Connection::bet(Player *player, t_score val)
{
	if(this->game->getNeeded() == Game::Info::BET && this->game->isPlayerTurn(player))
		this->game->giveBet(val);
}
void Connection::reroll(Player *player, t_hand hand)
{
	if(this->game->getNeeded() == Game::Info::HAND && this->game->isPlayerTurn(player))
	{
		for(int i=hand.len ; i<5 ; i++)
			hand.values[i] = this->distribution(generator);
		hand.len = 5;
	
		this->game->giveHand(hand);
		this->game->giveHandAck();
	}
	
}
void Connection::quit(Player *player)
{
	this->game->quit(player->name);
}
