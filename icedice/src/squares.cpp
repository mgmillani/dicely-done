#include "squares.hpp"
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>

#include "debug.h"

using namespace cv;

std::vector<cv::Vec4i> findLines(Mat canny, int t)
{	
	int dilation_size = 1;
	Mat temp;
	Mat element = getStructuringElement( MORPH_RECT,
                                       Size( 2*dilation_size + 1, 2*dilation_size+1 ),
                                       Point( dilation_size, dilation_size ) );
	dilate(canny, temp, element);
	
	std::vector<cv::Vec4i> lines;
	cv::HoughLinesP(temp, lines, 1, CV_PI/180, 50, 50, 2);
	
	return lines;
}

std::vector<cv::Vec4i> findLines2(Mat src_gray, int minLength, Mat *view)
{
	GaussianBlur( src_gray, *view, Size(9, 9), 2, 2 );
	cv::Canny(*view, *view, 20, 140, 3);
	//cv::threshold(*view, *view, 220, 255 ,0);
	
	int dilation_size = 1;
	Mat element = getStructuringElement( MORPH_RECT,
                                       Size( 2*dilation_size + 1, 2*dilation_size+1 ),
                                       Point( dilation_size, dilation_size ) );
	//dilate(*view, *view, element);
	
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	std::vector<cv::Vec4i> lines;

	findContours( *view, contours, hierarchy, CV_RETR_CCOMP, CV_CHAIN_APPROX_SIMPLE );
	*view = Mat::zeros(src_gray.rows, src_gray.cols, CV_8UC3);
	
	for(int id0=0, k=0 ; id0 >= 0; id0 = hierarchy[id0][0], k++ )
	{
		int id1 = hierarchy[id0][0];
		for(int i=id0 ; i<id1 ; i++)
		{
			vector<Point> cont = contours[i];
			int count = 1;
			Point_<double> p0 = cont[0];
			Vec4i line;
			line[0] = p0.x;
			line[1] = p0.y;
			Point_<double> p1 = cont[1];
			Vec2d avgDir = (p1 - p0);
			avgDir = avgDir / norm(avgDir);
			for(int j=1 ; j<cont.size() ; j++, p0 = p1, count++)
			{
				p1 = cont[j];
				Point diff = p1 - p0;
				Vec2d dir = (p1 - p0);
				dir = dir / norm(dir);				
				double projection = (dir[0] * avgDir[0] + dir[1]*avgDir[1]);
				if(projection > 0.7)
				{
					ERR("<%lf, %lf> . ", dir[0], dir[1]);
					ERR("<%lf, %lf>\n", avgDir[0], avgDir[1]);
					ERR("proj: %lf\n", projection);
					ERR("== %d ==\n", k);
				}
				
				//ERR("P0 = (%d, %d)   P1 = (%d, %d) : ", p0.x, p0.y, p1.x, p1.y);
				//double dist = sqrt(diff.x*diff.x + diff.y*diff.y);
				//ERR("Distance: %lf\n", dist);
				/*if( dist >= minLength )
				{
					Vec4i l;
					l[0] = p0.x;
					l[1] = p0.y;
					l[2] = p1.x;
					l[3] = p1.y;
					lines.push_back(l);
					}*/
			}
		}
	}
	
	return lines;
}


