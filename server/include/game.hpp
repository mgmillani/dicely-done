#ifndef GAME_HPP_
#define GAME_HPP_

#include <deque>
#include <list>
#include <string>

#include "hand.hpp"

class Player;
typedef unsigned int t_score;

/**
 * returns first.hand < second.hand
 */
bool comparePlayerHand(const Player *first, const Player *second);

class Game
{
public:
	enum class Round {INITIAL, BET, MATCH, RECAST, RESULT};
	enum class Info {HAND, BET, ACK};
	enum class Rank
	{ NONE=0
	, FIBONACCI
	, POWER_OF_TWO
	, PRIME
	, EVEN_ODD
	, STRAIGHT
	, FULL_HOUSE
	, HIGHEST
	};
	Game();
	
	void join(std::string player);
	void quit(std::string player);
	void giveHand(t_hand hand);
	void giveBet(t_score bet);	
	void updateNeeded();
	void giveAck();
	void decideWinner();
	
	std::list<Player*> players;
	std::list<Player*>::iterator currentPlayer;
	std::list<Player*>::iterator winner;
	Game::Round round;
	Game::Info needed; // what kind of information the game needs to be continue
	t_score pot; // accumulated bets
	t_score startScore; // with how many points each player starts
};

/**
 * calculates the rank of the hand of the player
 */
Game::Rank getRank(const Player *p);


#endif
