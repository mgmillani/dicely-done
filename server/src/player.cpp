#include <string>

#include "player.hpp"


Player::Player(std::string name, t_score score)
{
	this->name = name;
	this->hand.len = 0;
	this->score = score;
}
