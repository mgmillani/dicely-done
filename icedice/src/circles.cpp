#include "circles.h"
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>

#include "debug.h"

using namespace cv;

vector<Vec3f> findCirclesOLD(Mat src_gray, Mat *view, double param1, double param2)
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

vector<Vec3f> findCircles(Mat src_gray, Mat *view, double maxMeanSqrDist)
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

	findContours( *view, contours, hierarchy, CV_RETR_CCOMP, CV_CHAIN_APPROX_NONE );
	*view = Mat::zeros(src_gray.rows, src_gray.cols, CV_8UC3);
	
	vector<Vec3f> circles;
	
	for(int id0=0, k=0 ; id0 >= 0; id0 = hierarchy[id0][0], k++ )
	{
		//ERR("hierarchy: %d %d %d %d\n", hierarchy[id0][0], hierarchy[id0][1], hierarchy[id0][2], hierarchy[id0][3]);
		// o contorno atual vai de contours[id0] até contours[id1];
		int id1 = hierarchy[id0][0];
		if(id1 < 0)
			id1 = contours.size();
		//ERR("contour: %d -> %d\n", id0,id1);
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
				rectangle(*view, p, p, c);
				//line(*view, p0, p, c);
				p0 = p;
			}
			
		}
		center.x = center.x / count;
		center.y = center.y / count;
		
		//circle(*view, center, 3, c);
		
		double radius = 0;
		for(int i=id0 ; i<id1 ; i++)
		{
			vector<Point> cont = contours[i];
			for(unsigned int j=0 ; j<cont.size() ; j++)
			{
				Point p = cont[j];
				Point diff = center - p;
				radius += diff.x * diff.x + diff.y * diff.y;
			}
		}
		radius = sqrt(radius / count);
		
		double sqrSdist = 0;
		for(int i=id0 ; i<id1 ; i++)
		{
			vector<Point> cont = contours[i];
			for(unsigned int j=0 ; j<cont.size() ; j++)
			{
				Point p = cont[j];
				Point diff = center - p;
				double dist = sqrt(diff.x * diff.x + diff.y * diff.y) - radius;
				sqrSdist += dist * dist;
			}
		}
		sqrSdist = sqrSdist / count;
			
		if(sqrSdist < maxMeanSqrDist)
		{
			//ERR("sqrSdist: %lf < %lf\n", sqrSdist, maxMeanSqrDist);
			Vec3f circ;
			circ[0] = center.x;
			circ[1] = center.y;
			circ[2] = radius;
			circles.push_back(circ);
		}
	}
	
	return circles;
	
}
