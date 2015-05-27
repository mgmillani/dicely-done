#ifndef EXTRA_HPP_
#define EXTRA_HPP_

#include <opencv2/imgproc/imgproc.hpp>

/**
 * uses the given mask to reduce  the number of different values for each channel
 * Effectively, I[x,y] = I[x,y] & mask
 */
cv::Mat& reduceChannelRange(cv::Mat& I, uchar mask);

#endif
