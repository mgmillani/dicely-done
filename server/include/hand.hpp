#ifndef HAND_HPP_
#define HAND_HPP_

#define HAND_SIZE 5
#define MIN_DTs 1

typedef struct s_hand
{
	int values[HAND_SIZE];
	int len;
}t_hand;

#include <chrono>

/**
 * checks if the hand seen by the camera is stable
 * updates maybe and valid hands; resets t0 when needed.
 * returns:
 *   true  , when a new valid hand was found,
 *   false , otherwise
 */
bool updateHands(t_hand *valid, t_hand *maybe, t_hand *view, std::chrono::time_point<std::chrono::steady_clock> *t0);

/**
 * checks if two hands are the same
 */
bool sameHand(t_hand *first, t_hand *second);

#endif
