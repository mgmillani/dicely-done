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

vector<Vec3f> findCircles(Mat src_gray, Mat *view)
{
	/// Reduce the noise so we avoid false circle detection
  GaussianBlur( src_gray, *view, Size(9, 9), 2, 2 );
	cv::Canny(*view,*view, 20, 140, 3);

	cv::threshold(*view, *view, 200, 255 ,0);

  vector<Vec3f> circles;

  /// Apply the Hough Transform to find the circles
  HoughCircles( *view, circles, CV_HOUGH_GRADIENT, 1, view->rows/50, 100, 15, 0, view->rows/15 );
  
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

  /// Read the image
  src = imread( argv[1], 1 );

  if( !src.data )
    { return -1; }

  /// Convert it to gray
  cvtColor( src, src_gray, CV_BGR2GRAY );

	vector<Vec3f> circles = findCircles(src_gray, &circ_img);
	std::vector<cv::Vec4i> lines = findLines(src_gray, &line_img);
	
	cvtColor(circ_img, circ_img, CV_GRAY2BGR);
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
		circle( circ_img, center, radius, Scalar(0,0,255), 3, 8, 0 );
	}
	
	for( size_t i=0 ; i<lines.size() ; i++)
	{
		cv::Vec4i v = lines[i];
		cv::line(src, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
		cv::line(line_img, cv::Point(v[0], v[1]), cv::Point(v[2], v[3]), CV_RGB(0,0,255));
	}

  /// Show your results
  namedWindow( "Hough Circle Transform Demo", CV_WINDOW_AUTOSIZE );
  imshow( "Hough Circle Transform Demo", src );
  imshow( "Hough Circle Transform Circles", circ_img );
  imshow( "Hough Circle Transform Lines", line_img );

  while((char)waitKey(0) != 'q')
		;
  return 0;
}
