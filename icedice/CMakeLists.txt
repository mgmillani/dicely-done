cmake_minimum_required(VERSION 2.8)
project( libicedice )
add_definitions(-Wall)
find_package( OpenCV REQUIRED )

add_library(icedice src/icedice.cpp src/shapes.cpp src/extra.cpp)
target_link_libraries( icedice ${OpenCV_LIBS} )
include_directories("include")
