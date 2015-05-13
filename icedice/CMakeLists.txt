cmake_minimum_required(VERSION 2.8)
project( libicedice )
find_package( OpenCV REQUIRED )

add_library(icedice src/icedice.cpp src/circles.cpp src/squares.cpp)
target_link_libraries( icedice ${OpenCV_LIBS} )
include_directories("include")
