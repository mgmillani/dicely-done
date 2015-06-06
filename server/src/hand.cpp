#include <chrono>

#include "hand.hpp"

#include "magic.h"
#include "debug.h"

using namespace std::chrono;

bool sameHand(t_hand *first, t_hand *second)
{
	if(first->len != second->len)
		return false;

	bool found = true;
	for(int i=0 ; i<first->len && found ; i++)
	{
		found = false;
		for(int k=i ; k<second->len && !found ; k++)
		{
			if(first->values[i] == second->values[k])
			{
				// marks a value as already used by moving it to its corresponding position in first
				SWAP(second->values[i], second->values[k]);
				found = true;
			}
		}
		// if it is different
		if(!found)
			return false;
	}
	
	return true;
}

bool updateHands(t_hand *valid, t_hand *maybe, t_hand *view, time_point<steady_clock> *t0)
{
	// checks if the hand is stable
	// if the lenghts differ, it is not stable
	bool stable = sameHand(maybe, view);
	
	// replace old hand with new one if it is not stable
	if(!stable)
	{
		for(int i=0 ; i<view->len ; i++)
		{
			maybe->values[i] = view->values[i];
		}
		maybe->len = view->len;
		// resets the timer
		*t0 = steady_clock::now();
	}
	// if the candidate remained stable for long enough, accepts it
	else
	{
		time_point<steady_clock> t1 = steady_clock::now();			
		duration<double> elapsed_sec = t1 - *t0;
		double dt = elapsed_sec.count();
		if(dt > MIN_DTs)
		{
			// resets the timer
			*t0 = steady_clock::now();
			if(sameHand(valid, maybe))
				return false;
			// copies canditate to current
			for(int i=0 ; i<maybe->len ; i++)
			{
				valid->values[i] = maybe->values[i];
			}

			valid->len = maybe->len;
			return true;
		}		
	}
	
	return false;
}

