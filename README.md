# Dicely Done


# Server

## Dependencies
  - OpenCV
  
  
## Building
Run `cmake .` at the project root to generate the Makefiles, then run `make`
to compile the server.

## Usage
Run `path/to/project/server-main [webcam-num]`.


# Android Client

## Dependencies
  - JDK 1.7

## Building
The Android project is based on the gradle build system. A wrapper to
it is provided in `android-client/gradlew` - it downloads the necessary
jars and runs the build system.

In order to generate `.apk` files, run `android-client/gradlew build`.
The generated packages may be found at `android-client/app/build/outputs/apk`.

In order to install the `.apk` to an Android device connected by a USB cable,
use one of the `installApi*` tasks. To obtain a list of all tasks, run
`android-client/gradlew tasks`. Note that in order to communicate with the
device, it must have USB-debugging enabled.

