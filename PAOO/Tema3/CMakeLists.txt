cmake_minimum_required(VERSION 3.10)
project(StudentProject LANGUAGES CXX)

# Force out-of-source build in 'build' directory
if(CMAKE_SOURCE_DIR STREQUAL CMAKE_BINARY_DIR)
    file(MAKE_DIRECTORY ${CMAKE_SOURCE_DIR}/build)
    message(FATAL_ERROR "In-source builds are not allowed. Please run:\n  cmake -B build\n  cmake --build build")
endif()

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(CMAKE_BUILD_TYPE Debug)

set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)
file(MAKE_DIRECTORY ${CMAKE_RUNTIME_OUTPUT_DIRECTORY})

add_executable(executable
    src/main.cpp
    src/Student.cpp
    src/demo.cpp
)

target_include_directories(executable PRIVATE include)
target_compile_options(executable PRIVATE -Wall -Wextra -pedantic)
