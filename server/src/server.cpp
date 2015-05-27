#include <iostream>
#include <stdio.h>

#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <opencv2/opencv.hpp>

#include "icedice.hpp"

#include "debug.h"

using namespace cv;

int threshold_value = 70;
int threshold_type = 0;;
int const max_value = 255;
int const max_type = 4;
int const max_BINARY_value = 255;

int cP1 = 100;
int cP2 = 15;
int cMaxMeanSqr = 15;
int sMaxMeanSqr = 15;
int sMinLength = 10;

char window[] = "Faces";

typedef struct
{
	Mat src, src_gray, view;
}t_info;

void CircleMethod( int t, void* info );
void SquareMethod(int t, void* infoP);


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
	
	while(1)
	{
		Mat frame;
		cap >> frame;
		std::vector<t_face> faces = findFaces(&frame);
		
		for(size_t i=0 ; i<faces.size() ; i++)
		{
			t_face face = faces[i];
			Point center(face.center[0], face.center[1]);
			int v = face.value;
			TRACE("Value:%d\n",v);
			circle(frame, center, 20, Scalar(255 * (v&1) ,255 * (v&2), 255 * (v&4)), 3, 8, 0);
		}
		
		imshow(window, frame);
		if((char)waitKey(30) == 'q' ) break;
	}

  return 0;
}
