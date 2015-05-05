#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>
#include <stdio.h>

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

char circWindow[] = "Circle";

typedef struct
{
	Mat src, src_gray, view;
}t_info;

void CircleMethod( int t, void* info );

vector<Vec3f> findCircles(Mat src_gray, Mat *view, double param1, double param2)
{
	//TRACE("%lf %lf", param1, param2);
	/// Reduce the noise so we avoid false circle detection
  GaussianBlur( src_gray, *view, Size(9, 9), 2, 2 );
	cv::Canny(*view,*view, 20, 140, 3);

	cv::threshold(*view, *view, 200, 255 ,0);
	//*view = src_gray;
  vector<Vec3f> circles;

  /// Apply the Hough Transform to find the circles
  HoughCircles( *view, circles, CV_HOUGH_GRADIENT, 1, view->rows/50, param1, param2, 0, view->rows/15 );
  
	return circles;
}

vector<Vec3f> findCircles2(Mat src_gray, Mat *view, double maxMeanSqrDist)
{
	GaussianBlur( src_gray, *view, Size(9, 9), 2, 2 );
	cv::Canny(*view,*view, 20, 140, 3);
	int dilation_size = 1;
	Mat element = getStructuringElement( MORPH_RECT,
                                       Size( 2*dilation_size + 1, 2*dilation_size+1 ),
                                       Point( dilation_size, dilation_size ) );
	dilate(*view, *view, element);
	
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;

	findContours( *view, contours, hierarchy, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE );
	*view = Mat::zeros(src_gray.rows, src_gray.cols, CV_8UC3);
	int idx = 0;
	for( ; idx >= 0; idx = hierarchy[idx][0] )
	{
		Scalar color( rand()&255, rand()&255, rand()&255 );
		drawContours( *view, contours, idx, color, CV_FILLED, 8, hierarchy );
	}
	
	vector<Vec3f> circles;
	//for(int idx=0 ; idx >= 0; idx = hierarchy[idx][0] )
	for(int i=0 ; i<contours.size() ; i++)
	{
		Point center(0,0);
		vector<Point> cont = contours[i];
		for(int j=0 ; j<cont.size() ; j++)
		{
			Point p = cont[j];
			center += p;
		}
		center.x = center.x / cont.size();
		center.y = center.y / cont.size();
		
		double radius = 0;
		for(int j=0 ; j<cont.size() ; j++)
		{
			Point p = cont[j];
			Point diff = center - p;
			radius += diff.x * diff.x + diff.y * diff.y;
		}
		radius = sqrt(radius / cont.size());
		
		double sqrSdist = 0;
		for(int j=0 ; j<cont.size() ; j++)
		{
			Point p = cont[j];
			Point diff = center - p;
			double dist = sqrt(diff.x * diff.x + diff.y * diff.y) - radius;
			sqrSdist += dist * dist;
		}
		sqrSdist = sqrSdist / cont.size();
		//ERR("sqrSdist: %lf\n", sqrSdist);
		if(sqrSdist < maxMeanSqrDist)
		{
			Vec3f circ;
			circ[0] = center.x;
			circ[1] = center.y;
			circ[2] = radius;
			circles.push_back(circ);
		}
	}
	
	return circles;
	
}

std::vector<cv::Vec4i> findLines(Mat src_gray, Mat *view)
{
	GaussianBlur( src_gray, *view, Size(9, 9), 2, 2 );
	cv::Canny(*view, *view, 20, 140, 3);
	//cv::threshold(*view, *view, 220, 255 ,0);
	
	int dilation_size = 1;
	Mat element = getStructuringElement( MORPH_RECT,
                                       Size( 2*dilation_size + 1, 2*dilation_size+1 ),
                                       Point( dilation_size, dilation_size ) );
	dilate(*view, *view, element);
	
	std::vector<cv::Vec4i> lines;
	cv::HoughLinesP(*view, lines, 1, CV_PI/360, 50, 30, 0);
	
	return lines;
}

/** @function main */
int main(int argc, char** argv)
{

	if(argc == 1)
	{
		printf("usage: %s <img>\n", argv[0]);
		return 1;
	}

  Mat src, src_gray, dst, circ_img, line_img;
  t_info info;

  /// Read the image
  src = imread( argv[1], 1 );

  if( !src.data )
    { return -1; }

  /// Convert it to gray
  cvtColor( src, src_gray, CV_BGR2GRAY );

	//vector<Vec3f> circles = findCircles2(src_gray, &circ_img, cP1, cP2);
	vector<Vec3f> circles = findCircles2(src_gray, &circ_img, cMaxMeanSqr);
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
  //imshow( "Hough Circle Transform Lines", line_img );


	info.src = src;
	info.src_gray = src_gray;

	char* trackbar_label = "Treshold 1:";
  createTrackbar( trackbar_label, circWindow, &cMaxMeanSqr, 500, CircleMethod, &info );

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
	
	vector<Vec3f> circles = findCircles2(info->src_gray, &info->view, t);
	
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
