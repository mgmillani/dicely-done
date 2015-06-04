#include <string>

#include "player.hpp"
#include "game.hpp"

#include "debug.h"

Player::Player(std::string name, t_score score)
{
	this->name = name;
	this->hand.len = 0;
	this->score = score;
	this->rank = Game::Rank::NONE;
}

bool Player::operator<(const Player &p)
{
	return p.rank < this->rank;
}
