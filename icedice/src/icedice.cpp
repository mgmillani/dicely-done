#include "opencv2/highgui/highgui.hpp"
#include "icedice.hpp"
#include "shapes.hpp"

#include "debug.h"

using namespace cv;

std::vector<t_face> findFaces(Mat *image)
{
	Mat canny;
	// Convert image to gray
  cvtColor( *image, canny, CV_BGR2GRAY );
  GaussianBlur( canny, canny, Size(9, 9), 2, 2 );
	cv::Canny(canny, canny, 20, 140, 3);
	std::vector<Vec3f> circles = findCircles(canny, 10);
	std::vector<Vec4i> lines = findLines(canny, 15);
	
	// goes through every line, assuming that it is part of a square
	std::vector<t_face> faces;
	for(size_t i=0 ; i<lines.size() ; i++)
	{
		Vec4i line = lines[i];		
		// direction vector of the line
		double vx = line[2] - line[0];
		double vy = line[3] - line[1];		
		double len = sqrt(vx*vx + vy*vy);
		// normalized
		vx = vx / len;
		vy = vy / len;
		// the line can then be described as:
		//   P + t * v
		t_face faceN, faceP;		
		faceN.value = 0;
		faceN.center[0] = 0;
		faceN.center[1] = 0;
		faceP.value = 0;
		faceP.center[0] = 0;
		faceP.center[1] = 0;
		
		// checks how many circles are near this line
		for(size_t j=0 ; j<circles.size() ; j++)
		{
			Vec3f circ = circles[j];
			cv::circle(*image, Point(circ[0], circ[1]), 3, Scalar(255,0,255), 5, 8, 0);
			/**
			 * the vector perpendicular to the line is given by:
			 *   ux = -vy
			 *   uy =  vx
			 * 
			 * The line from the center C of the circle that is perpendicular to the line is given by
			 *   C + k * u
			 * the distance is then obtained by solving
			 *   Cx + k * ux = Px + t * vx
			 *   Cy + k * uy = Py + t * vy
			 * If abs(k) < len and 0 < t < len, then the point is within the square described by the line.
			 * After some algebraic steps, we conclude that
			 *
			 *       -vy * k + Cx - Px
			 * t = -------------------- 
			 *              vx
			 *
			 *      vy (Cx - Px) - vx (Cy - Py)
			 * k = -----------------------------
			 *               vx² + vy²
			 *
			 * for vx = 0, we have
			 *  
			 *      Cx - Px
			 * k = --------
			 *        vy
			 *
			 *      Cx - Px
			 * t = ---------
			 *        vy
			 */ 
			double dx = circ[0] - line[0];
			double dy = circ[1] - line[1];
			double k = (vy * dx - vx * dy) / (vx*vx + vy*vy);
			double t;
			// vx == 0
			if(line[0] == line[2])
			{
				t = dx / vy;
			}
			else
			{
				t = (-vy*k + dx) / vx;
			}
			
			if(t < 0 || t > len)
				continue;
			
			// within square on the positive side
			if(k < len && k >= 0)
			{
				faceP.value++;
				faceP.center[0] += circ[0];
				faceP.center[1] += circ[1];
			}
			// within square on the negative side
			else if(k > -len && k < 0)
			{
				faceN.value++;
				faceN.center[0] += circ[0];
				faceN.center[1] += circ[1];
			}
		}
		
		vx *= len;
		vy *= len;
		if(faceP.value > 0)
		{
			faceP.center[0] /= faceP.value;
			faceP.center[1] /= faceP.value;
			// if the center of the face is too far away from the center of the circles, there was something wrong with the detection of this face
			double x = ((line[0] + line[2]) + vy)/2;
			double y = ((line[1] + line[3]) - vx)/2;
			double dx = x - faceP.center[0];
			double dy = y - faceP.center[1];
			
			if(sqrt(dx*dx + dy*dy) < len/10)
			{
				faces.push_back(faceP);			
				cv::line(*image, Point(line[0], line[1]), Point(line[0] + vx, line[1] + vy), CV_RGB(0,0,255), 1 );
				cv::line(*image, Point(line[0] +vy, line[1] - vx), Point(line[2] +vy, line[3] - vx), CV_RGB(0,0,255) );
				cv::line(*image, Point(line[0], line[1]), Point(line[0] + vy, line[1] - vx), CV_RGB(0,255,0) );
				cv::line(*image, Point(line[0], line[1]), Point(faceP.center[0], faceP.center[1]), CV_RGB(0,0,255), 1 );
			}
		}
		if(faceN.value > 0)
		{
			faceN.center[0] /= faceN.value;
			faceN.center[1] /= faceN.value;
			// if the center of the face is too far away from the center of the circles, there was something wrong with the detection of this face
			double x = (line[0] + line[2] - vy)/2;
			double y = (line[1] + line[3] + vx)/2;
			double dx = x - faceN.center[0];
			double dy = y - faceN.center[1];
			if(sqrt(dx*dx + dy*dy) < len/10)
			{
				faces.push_back(faceN);
				cv::line(*image, Point(line[0], line[1]), Point(line[0] + vx, line[1] + vy), CV_RGB(0,0,255), 1 );
				cv::line(*image, Point(line[0] -vy, line[1] + vx), Point(line[2] -vy, line[3] + vx), CV_RGB(0,0,255) );
				cv::line(*image, Point(line[0], line[1]), Point(line[0] - vy, line[1] + vx), CV_RGB(0,255,0) );
				cv::line(*image, Point(line[0], line[1]), Point(faceN.center[0], faceN.center[1]), CV_RGB(0,0,255), 1 );
			}
		}
		
	}
	
	return faces;
}
