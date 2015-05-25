#ifndef SHAPES_HPP_
#define SHAPES_HPP_

#include <opencv2/imgproc/imgproc.hpp>

std::vector<cv::Vec3f> findCircles(cv::Mat canny, double maxMeanSqrDist);

std::vector<cv::Vec4i> findLines(cv::Mat canny, int t);

#endif
