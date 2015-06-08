#include <iostream>
#include <stdio.h>
#include <chrono>

#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <opencv2/opencv.hpp>

#include "icedice.hpp"
#include "hand.hpp"
#include "game.hpp"
#include "connection.hpp"

#include "magic.h"
#include "debug.h"

using namespace cv;
using namespace std::chrono;

char window[] = "Faces";

const int increment = 3;
const int maxTrust = 15;

typedef struct
{
	Mat src, src_gray, view;
}t_info;

typedef struct
{
	t_face f;
	int trust;
}t_dface;

void processNewFaces(std::vector<t_face> &newFaces, std::vector<t_dface> &faces, int maxDist)
{
	// sees if some face was already detected
	for(size_t j=0 ; j<faces.size() ; j++)
	{
		t_dface dface = faces[j];
		t_face face = dface.f;
		for(size_t i=0 ; i<newFaces.size() ; i++)
		{
			t_face nface = newFaces[i];
			if(nface.value != face.value)
				continue;
			double dx = face.center[0] - nface.center[0];
			double dy = face.center[1] - nface.center[1];
			double dist = sqrt(dx*dx + dy*dy);
			if(dist < maxDist)
			{
				dface.trust += increment;
				nface = newFaces[newFaces.size() -1];
				newFaces[i] = nface;
				i--;
				newFaces.pop_back();
			}
		}
		// removes unlikely face
		dface.trust--;
		faces[j] = dface;
		if(dface.trust == 0)
		{
			dface = faces[faces.size()-1];
			faces[j] = dface;
			j--;
			faces.pop_back();
		}
		else if(dface.trust > maxTrust)
		{
			faces[j].trust = maxTrust;
		}

	}

	// add new faces
	for(size_t i=0 ; i<newFaces.size() ; i++)
	{
		t_dface df;
		df.f = newFaces[i];
		if(df.f.value >= 1 && df.f.value <= 6)
		{
			df.trust = increment;
			faces.push_back(df);
		}
	}
}

int main(int argc, char** argv)
{

	VideoCapture cap(argc > 1 ? atoi(argv[1]) : 1);
	if(!cap.isOpened())
	{
		TRACE("Failed to open camera");
		return -1;
	}

	namedWindow( window, CV_WINDOW_AUTOSIZE );
	std::vector<t_dface> faces;
	time_point<steady_clock> t0;
	t0 = steady_clock::now();
	t_hand validHand;
	validHand.len = 0;
	t_hand maybeHand;
	maybeHand.len = 0;
	t_hand viewHand;

	RemoteGame game;
	Connection conn(1337, &game);

	conn.join("Geralt");
	conn.join("Zoltan");

	int n=2;
	while(1)
	{
		Mat frame;
		cap >> frame;

		std::vector<t_face> newFaces;
		int j;

		conn.receiveMessages();

		switch(game.needed)
		{

			case Game::Info::HAND:
			case Game::Info::HAND_ACK:

				newFaces = findFaces(&frame);
				processNewFaces(newFaces, faces, frame.rows/80);

				j=0;
				for(size_t i=0 ; i<faces.size() ; i++)
				{
					t_dface dface = faces[i];
					t_face face = dface.f;
					Point center(face.center[0], face.center[1]);
					int v = face.value;
					if(v == 0)
					{
						TRACE("wtf?");
					}
					if(dface.trust > increment*2)
					{
						//ERR("  Value:%d   %d\n",v, dface.trust);
						circle(frame, center, 20, Scalar(255 * (v&1) ,255 * (v&2), 255 * (v&4)), 3, 8, 0);
						if(j < HAND_SIZE)
							viewHand.values[j++] = v;
					}
				}

				viewHand.len = j;

				// checks if the valid hand changed
				if(updateHands(&validHand, &maybeHand, &viewHand, &t0))
				{
					if(validHand.len > 0)
						game.giveHand(validHand);	
					else if(validHand.len == 0 && game.needed == Game::Info::HAND_ACK)
						game.giveHandAck();
				}


				break;
			case Game::Info::BET:
				game.giveBet(n);
				n = (n+1)%5;
				break;
			case Game::Info::ACK:
				game.giveAck();
				break;
		}

		imshow(window, frame);
		if((char)waitKey(10) == 'q' ) break;
	}

  return 0;
}
