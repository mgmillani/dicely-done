#ifndef SQUARES_H_
#define SQUARES_H_

#include <opencv2/imgproc/imgproc.hpp>

using namespace cv;

std::vector<cv::Vec4i> findLines(Mat src_gray, int t);

std::vector<cv::Vec4i> findLines2(Mat src_gray, int minLength, Mat *view);

#endif
