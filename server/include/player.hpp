#ifndef PLAYER_HPP_
#define PLAYER_HPP_

#include <string>

typedef unsigned int t_score;

#include "hand.hpp"
#include "game.hpp"

class Player
{
public:
	Player(std::string name, t_score score);

	t_hand hand;
	std::string name;
	bool active;
	t_score score;
	t_score bet;
	Game::Rank rank; // rank of the player's hand.
	
	/**
	 * compares rank
	 */
	bool operator<(const Player &p);
};

#endif
