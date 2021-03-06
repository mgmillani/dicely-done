#include <opencv2/imgproc/imgproc.hpp>

#include "shapes.hpp"

#include "debug.h"

using namespace cv;

vector<Vec3f> findCircles(Mat canny, double maxMeanSqrDist)
{
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;
	int dilation_size = 1;
	Mat temp;
	Mat element = getStructuringElement( MORPH_RECT,
                                       Size( 2*dilation_size + 1, 2*dilation_size+1 ),
                                       Point( dilation_size, dilation_size ) );
	//dilate(canny, temp, element);
	temp = canny;
	findContours( temp, contours, hierarchy, CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE );
	
	vector<Vec3f> circles;
	if(hierarchy.size() == 0)
		return circles;
	for(int id0=0, k=0 ; id0 >= 0; id0 = hierarchy[id0][0], k++ )
	{
		// current contour goes from contours[id0] until contours[id1-1];
		int id1 = hierarchy[id0][0];
		if(id1 < 0)
			id1 = contours.size();
						
		Point center(0,0);
		int count = 0;
		Scalar c = Scalar(22*k,255 - 15*k,0);
		for(int i=id0 ; i<id1 ; i++)
		{
			vector<Point> cont = contours[i];
			count += cont.size();
			Point p0 = cont[0];
			for(unsigned int j=0 ; j<cont.size() ; j++)
			{
				Point p = cont[j];
				center += p;
				p0 = p;
			}			
		}
		center.x = center.x / count;
		center.y = center.y / count;
		
		double radius = 0;
		for(int i=id0 ; i<id1 ; i++)
		{
			vector<Point> cont = contours[i];
			for(unsigned int j=0 ; j<cont.size() ; j++)
			{
				Point p = cont[j];
				Point diff = center - p;
				radius += sqrt(diff.x * diff.x + diff.y * diff.y);
			}
		}
		radius = radius / count;
		
		double avgDist = 0;
		for(int i=id0 ; i<id1 ; i++)
		{
			vector<Point> cont = contours[i];
			for(unsigned int j=0 ; j<cont.size() ; j++)
			{
				Point p = cont[j];
				Point diff = center - p;
				double dist = sqrt(diff.x * diff.x + diff.y * diff.y) - radius;
				avgDist += abs(dist); // * dist;
			}
		}
		avgDist = avgDist / count;
			
		if(avgDist < radius * 0.3 && count > 12)
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

std::vector<Vec4i> findLines(Mat canny, int t)
{	
	int dilation_size = 1;
	Mat temp;
	Mat element = getStructuringElement( MORPH_RECT,
                                       Size( 2*dilation_size + 1, 2*dilation_size+1 ),
                                       Point( dilation_size, dilation_size ) );
	dilate(canny, temp, element);
	
	std::vector<cv::Vec4i> lines;
	cv::HoughLinesP(temp, lines, 1, CV_PI/180, 85, 10, 1);
	
	return lines;
}
