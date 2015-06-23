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
	Game(t_score minBet, t_score startScore );
	
	bool join(std::string player);
	bool join(Player *player);
	void quit(std::string player);
	void giveHand(t_hand hand);
	void giveHandAck();
	void giveBet(t_score bet);	
	void giveFold();
	void finish();
	void updateNeeded();
	void restart();
	void giveAck();
	void giveAck(Player *player);
	void decideWinner();
	void nextPlayer();
	void nextRound();
	bool isPlayerTurn(Player *player);
	Game::Info getNeeded();
	/**
	 * notification functions. Used to signalize someone of what is going on
	 */
	/**
	 * let the game begin!
	 */
	virtual void informStart();
	virtual void informStart(Player *player);
	/**
	 * who is the current player
	 */
	virtual void informPlayer();
	/**
	 * what is the current round
	 */
	virtual void informRound();
	/**
	 * which is the hand of the given player
	 */
	virtual void informHand(Player *p);
	/**
	 * total bet of the given player
	 */
	virtual void informBet(Player *p);
	/**
	 * given player gave up
	 */
	virtual void informFold(Player *p);
	/**
	 * given player left the game
	 */
	virtual void informQuit(Player *p);
	/**
	 * given player has joined
	 */
	virtual void informJoin(Player *p);
	/**
	 * who won the game
	 */
	virtual void informWinner();
	
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
public:
	RemoteGame(t_score minBet, t_score startScore );
	void broadcast(std::string msg);
	/**
	 * let the game begin!
	 */
	virtual void informStart();
	virtual void informStart(Player *player);
	/**
	 * who is the current player
	 */
	virtual void informPlayer();
	/**
	 * what is the current round
	 */
	virtual void informRound();
	/**
	 * which is the hand of the given player
	 */
	virtual void informHand(Player *p);
	/**
	 * total bet of the given player
	 */
	virtual void informBet(Player *p);
	/**
	 * given player gave up
	 */
	virtual void informFold(Player *p);
	/**
	 * given player left the game
	 */
	virtual void informQuit(Player *p);
	/**
	 * given player has joined
	 */
	virtual void informJoin(Player *p);
	/**
	 * who won the game
	 */
	virtual void informWinner();
};

// hack to show Gtk window parallel to a running server
// receives commands from above and sends them to multiple game instances
class MultiGame : public Game
{
public:
	MultiGame(t_score minBet, t_score startScore );
	void add(Game *game);
	std::list<Game*> games;
	
	bool join(std::string player);
	bool join(Player *player);
	void quit(std::string player);
	void giveHand(t_hand hand);
	void giveHandAck();
	void giveBet(t_score bet);	
	void giveFold();
	void giveAck();
	void restart();
	void giveAck(Player *player);
	bool isPlayerTurn(Player *player);
	Game::Info getNeeded();
};

//#include <gtkmm.h>

class LocalGame : public Game
{
public:
	LocalGame(t_score minBet, t_score startScore );
	LocalGame();
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
	//GtkWindow *window;
};

/**
 * calculates the rank of the hand of the player
 */
Game::Rank getRank(const Player *p);


#endif
