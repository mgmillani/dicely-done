cmake_minimum_required(VERSION 2.8)
project( DicelyDone )
find_package( OpenCV REQUIRED )
add_definitions(-Wall)

include_directories(icedice icedice/include)
add_subdirectory(icedice)

add_executable( server-main server/src/server.cpp )
target_link_libraries( server-main ${OpenCV_LIBS} )
target_link_libraries( server-main icedice )
