#include <iostream>
#include <stdio.h>

#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <opencv2/opencv.hpp>

#include "icedice.hpp"

#include "debug.h"

using namespace cv;

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

void CircleMethod( int t, void* info );
void SquareMethod(int t, void* infoP);


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
		df.trust = increment;
		faces.push_back(df);
	}
}

/** @function main */
int main(int argc, char** argv)
{

	VideoCapture cap(1);
	if(!cap.isOpened())
	{
		TRACE("Failed to open camera");
		return -1;
	}
	
	namedWindow( window, CV_WINDOW_AUTOSIZE );
	std::vector<t_dface> faces;
	while(1)
	{
		Mat frame;
		cap >> frame;
		std::vector<t_face> newFaces = findFaces(&frame);
		
		processNewFaces(newFaces, faces, frame.rows/80);
		
		ERR("Faces:%d\n", faces.size());
		for(size_t i=0 ; i<faces.size() ; i++)
		{
			t_dface dface = faces[i];
			t_face face = dface.f;
			Point center(face.center[0], face.center[1]);
			int v = face.value;
			if(dface.trust > increment)
			{
				ERR("  Value:%d   %d\n",v, dface.trust);
				circle(frame, center, 20, Scalar(255 * (v&1) ,255 * (v&2), 255 * (v&4)), 3, 8, 0);
			}
		}
		
		imshow(window, frame);
		if((char)waitKey(30) == 'q' ) break;
	}

  return 0;
}
