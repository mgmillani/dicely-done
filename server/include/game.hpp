#ifndef GAME_HPP_
#define GAME_HPP_

#include <deque>
#include <list>

#include "player.hpp"
#include "hand.hpp"

class Game
{
public:
	enum class Round {INITIAL, BET, MATCH, RECAST, RESULT};
	enum class Info {HAND, BET, ACK};
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
	Round round;
	Info needed; // what kind of information the game needs to be continue
	t_score pot; // accumulated bets
	t_score startScore; // with how many points each player starts
};

#endif
