cmake_minimum_required(VERSION 2.8)
project( DicelyDone )
add_definitions(-Wall -std=c++11)

find_package( OpenCV REQUIRED )

include_directories(icedice icedice/include)
add_subdirectory(icedice)

add_executable( server-main server/src/server.cpp server/src/hand.cpp server/src/player.cpp server/src/game.cpp server/src/connection.cpp)
include_directories(server server/include)
target_link_libraries( server-main ${OpenCV_LIBS} )
target_link_libraries( server-main icedice )
