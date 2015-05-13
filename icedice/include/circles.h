#ifndef CIRCLES_H_
#define CIRCLES_H_

#include <opencv2/imgproc/imgproc.hpp>

using namespace cv;

vector<Vec3f> findCircles(Mat src_gray, Mat *view, double maxMeanSqrDist);

#endif
