#ifndef ICEDICE_H_
#define ICEDICE_H_

#include <opencv2/imgproc/imgproc.hpp>

typedef struct s_face
{
	cv::Vec2f center;
	int value;
	cv::Vec3f color;
}t_face;

std::vector<t_face> findFaces(cv::Mat *src);


#endif
