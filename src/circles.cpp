#include "opencv2/highgui/highgui.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>
#include <stdio.h>

using namespace cv;

int threshold_value = 70;
int threshold_type = 0;;
int const max_value = 255;
int const max_type = 4;
int const max_BINARY_value = 255;

vector<Vec3f> findCircles(Mat src_gray, Mat dst)
{
	/// Reduce the noise so we avoid false circle detection
  GaussianBlur( src_gray, dst, Size(9, 9), 2, 2 );
	cv::Canny(dst, dst, 20, 140, 3);

  vector<Vec3f> circles;

  /// Apply the Hough Transform to find the circles
  HoughCircles( dst, circles, CV_HOUGH_GRADIENT, 1, dst.rows/50, 100, 15, 0, dst.rows/15 );
  
	return circles;
}

std::vector<cv::Vec4i> findLines(Mat src_gray, Mat dst)
{
	cv::Canny(src_gray, dst, 20, 140, 3);
	std::vector<cv::Vec4i> lines;
	cv::HoughLinesP(dst, lines, 1, CV_PI/180, 50, 30, 10);
	
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

  Mat src, src_gray, dst;

  /// Read the image
  src = imread( argv[1], 1 );

  if( !src.data )
    { return -1; }

  /// Convert it to gray
  cvtColor( src, src_gray, CV_BGR2GRAY );

  

	vector<Vec3f> circles = findCircles(src_gray, dst);
	std::vector<cv::Vec4i> lines = findLines(src_gray, dst);
	
	cv::Canny(src_gray, dst, 20, 140, 3);
	
	cvtColor(dst, dst, CV_GRAY2BGR);
	
	//src = dst;
	

	/// Draw the circles detected	
  for( size_t i = 0; i < circles.size(); i++ )
  {
		Point center(cvRound(circles[i][0]), cvRound(circles[i][1]));
		int radius = cvRound(circles[i][2]);
		// circle center
		circle( src, center, 3, Scalar(0,255,0), -1, 8, 0 );
		// circle outline
		circle( src, center, radius, Scalar(0,0,255), 3, 8, 0 );
	}
	
	for( size_t i=0 ; i<lines.size() ; i++)
	{
		cv::Vec4i v = lines[i];
		cv::line(src, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
	}

  /// Show your results
  namedWindow( "Hough Circle Transform Demo", CV_WINDOW_AUTOSIZE );
  imshow( "Hough Circle Transform Demo", src );

  waitKey(0);
  return 0;
}
