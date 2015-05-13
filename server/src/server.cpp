#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>
#include <stdio.h>

#include "icedice.h"
#include "circles.h"
#include "squares.h"

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

char circWindow[] = "Circle";
char sqrWindow[] = "Square";

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

  /// Convert it to gray
  cvtColor( src, src_gray, CV_BGR2GRAY );
  
	vector<Vec3f> circles = findCircles(src_gray, &circ_img, cMaxMeanSqr);
	std::vector<cv::Vec4i> lines = findLines(src_gray, &line_img);
	
	//cvtColor(circ_img, circ_img, CV_GRAY2BGR);
	cvtColor(line_img, line_img, CV_GRAY2BGR);
	
	//src = dst;
	

	/// Draw the circles detected	
  for( size_t i = 0; i < circles.size(); i++ )
  {
		Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
		int radius = cvRound(circles[i][2]);
		// circle center
		circle( src, center, 3, Scalar(0,255,0), -1, 8, 0 );
		circle( circ_img, center, 3, Scalar(0,255,0), -1, 8, 0 );
		// circle outline
		circle( src, center, radius, Scalar(0,0,255), 3, 8, 0 );
		circle( circ_img, center, radius, Scalar(0,0,255), 1, 8, 0 );
	}
	
	for( size_t i=0 ; i<lines.size() ; i++)
	{
		cv::Vec4i v = lines[i];
		cv::line(src, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
		cv::line(line_img, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
	}

  /// Show your results
  namedWindow( circWindow, CV_WINDOW_AUTOSIZE );
  //imshow( "Hough Circle Transform Image", src );
  imshow( circWindow, circ_img );
  namedWindow( sqrWindow, CV_WINDOW_AUTOSIZE );
  imshow( sqrWindow, line_img );
  //imshow( "Hough Circle Transform Lines", line_img );


	infoC.src = infoS.src =  src;
	infoC.src_gray = infoC.src_gray = src_gray;

	char* trackbar_label = "Treshold 1:";
  createTrackbar( trackbar_label, circWindow, &cMaxMeanSqr, 500, CircleMethod, &infoC );
  createTrackbar( trackbar_label, sqrWindow, &sMaxMeanSqr, 500, SquareMethod, &infoS );

  while((char)waitKey(0) != 'q')
		;
  return 0;
}

/** @function CircleMethod */
void CircleMethod( int t, void* infoP )
{
	t_info *info = (t_info *)infoP;
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
	imshow( circWindow, info->view );
	
}

void SquareMethod(int t, void* infoP)
{
	t_info *info = (t_info *)infoP;
	std::vector<cv::Vec4i> lines = findLines(info->src_gray, &info->view);
	
	for( size_t i=0 ; i<lines.size() ; i++)
	{
		cv::Vec4i v = lines[i];
		cv::line(info->view, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
		cv::line(info->view, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
	}
	
	namedWindow( circWindow, CV_WINDOW_AUTOSIZE );
	imshow( circWindow, info->view );
}
