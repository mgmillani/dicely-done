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
	enum class Round {INITIAL=1, BET, MATCH, RECAST, RESULT};
	enum class Info {HAND, BET, ACK, HAND_ACK, NOTHING};
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
	void giveHandAck();
	void giveBet(t_score bet);	
	void giveFold();
	void updateNeeded();
	void giveAck();
	void decideWinner();
	void nextPlayer();
	void nextRound();
	/**
	 * notification functions. Used to signalize someone of what is going on
	 */
	/**
	 * let the game begin!
	 */
	void informStart();
	/**
	 * who is the current player
	 */
	void informPlayer();
	/**
	 * what is the current round
	 */
	void informRound();
	/**
	 * which is the hand of the given player
	 */
	void informHand(Player *p);
	/**
	 * total bet of the given player
	 */
	void informBet(Player *p);
	/**
	 * given player gave up
	 */
	void informFold(Player *p);
	/**
	 * given player left the game
	 */
	void informQuit(Player *p);
	/**
	 * given player has joined
	 */
	void informJoin(Player *p);
	/**
	 * who won the game
	 */
	void informWinner();
	
	std::list<Player*> players;
	std::list<Player*> activePlayers;
	std::list<Player*>::iterator currentPlayer;
	std::list<Player*>::iterator winner;
	Game::Round round;
	Game::Info needed; // what kind of information the game needs to be continue
	t_score pot; // accumulated bets
	t_score playerBet; // how much each player is betting
	t_score startScore; // with how many points each player starts
	t_score minBet; // how much each player bets when the game starts
};

class RemoteGame : public Game
{
	void informPlayer();
	void informRound();
	void informWinner();
};

/**
 * calculates the rank of the hand of the player
 */
Game::Rank getRank(const Player *p);


#endif
