#include "squares.h"
#include "opencv2/imgproc/imgproc.hpp"
#include <iostream>

#include "debug.h"

using namespace cv;

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
