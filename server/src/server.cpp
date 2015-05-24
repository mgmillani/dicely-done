#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>
#include <stdio.h>

#include "icedice.hpp"
#include "circles.hpp"
#include "squares.hpp"
#include "faces.hpp"

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

	if(argc == 1)
	{
		printf("usage: %s <img>\n", argv[0]);
		return 1;
	}

  Mat src, src_gray, dst, circ_img, line_img;
  t_info infoC;
  t_info infoS;

  /// Read the image
  src = imread( argv[1], 1 );

  if( !src.data )
    { return -1; }

  std::vector<t_face> faces = findFaces(&src);
  for(size_t i=0 ; i<faces.size() ; i++)
  {
		t_face face = faces[i];
		Point center(face.center[0], face.center[1]);
		int v = face.value;
		TRACE("Value:%d\n",v);
		circle(src, center, 20, Scalar(255 * (v&1) ,255 * (v&2), 255 * (v&4)), 1, 8, 0);
  }

  /// Show your results
  namedWindow( window, CV_WINDOW_AUTOSIZE );
  imshow( window, src );

	/*infoC.src = infoS.src =  src;
	infoC.src_gray = infoS.src_gray = src_gray;

	char* trackbar_label = "Treshold 1:";
  createTrackbar( trackbar_label, circWindow, &cMaxMeanSqr, 500, CircleMethod, &infoC );
  createTrackbar( trackbar_label, sqrWindow, &sMaxMeanSqr, 500, SquareMethod, &infoS );
	*/
  while((char)waitKey(0) != 'q')
		;
  return 0;
}

/** @function CircleMethod */
void CircleMethod( int t, void* infoP )
{
	/*t_info *info = (t_info *)infoP;
	if ( t==0)
		t = 1;
	
	vector<Vec3f> circles = findCircles(info->src_gray, &info->view, t);
	
	//cvtColor(info->view, info->view, CV_GRAY2BGR);
	
	/// Draw the circles detected	
  for( size_t i = 0; i < circles.size(); i++ )
  {
		Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
		int radius = cvRound(circles[i][2]);
		// circle center		
		circle( info->view, center, 3, Scalar(0,255,0), -1, 8, 0 );
		// circle outline
		circle( info->view, center, radius, Scalar(0,0,255), 1, 8, 0 );
	}
	
	namedWindow( circWindow, CV_WINDOW_AUTOSIZE );
	imshow( circWindow, info->view );*/
	
}

void SquareMethod(int t, void* infoP)
{
	/*t_info *info = (t_info *)infoP;
	
	std::vector<cv::Vec4i> lines = findLines(info->src_gray, t, &info->view);
	
	cvtColor( info->view, info->view, CV_GRAY2BGR );
	
	for( size_t i=0 ; i<lines.size() ; i++)
	{
		cv::Vec4i v = lines[i];
		cv::line(info->view, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
		cv::line(info->view, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
	}
	
	namedWindow( sqrWindow, CV_WINDOW_AUTOSIZE );
	imshow( sqrWindow, info->view );*/
}
