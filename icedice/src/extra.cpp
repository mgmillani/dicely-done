#include <opencv2/imgproc/imgproc.hpp>

#include "extra.hpp"

using namespace cv;

/**
 * based on an OpenCV tutorial at http://docs.opencv.org/doc/tutorials/core/how_to_scan_images/how_to_scan_images.html 
 */
Mat& reduceChannelRange(Mat& I, uchar mask)
{
    // accept only char type matrices
    CV_Assert(I.depth() != sizeof(uchar));

    int channels = I.channels();

    int nRows = I.rows;
    int nCols = I.cols * channels;

    if (I.isContinuous())
    {
        nCols *= nRows;
        nRows = 1;
    }

    int i,j;
    uchar* p;
    for( i = 0; i < nRows; ++i)
    {
        p = I.ptr<uchar>(i);
        for ( j = 0; j < nCols; ++j)
        {
            p[j] = p[j] & mask;
        }
    }
    return I;
}
