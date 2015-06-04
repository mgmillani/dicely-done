#ifndef PLAYER_HPP_
#define PLAYER_HPP_

#include <string>

typedef unsigned int t_score;

#include "hand.hpp"

class Player
{
public:
	Player(std::string name, t_score score);

	t_hand hand;
	std::string name;
	bool active;
	t_score score;
	t_score bet;
};

#endif
