cmake_minimum_required(VERSION 3.10)
project(StudentProject LANGUAGES CXX)

# Set C++ standard (good practice: use modern C++17 for your features like move semantics)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Output executables to build/bin (good practice: keep build artifacts separate)
set(CMAKE_RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin)

# Include directories (target-based for modern CMake)
add_executable(executable
    src/main.cpp
    src/Student.cpp
    src/demo.cpp
)

target_include_directories(executable PRIVATE include)

# Optional: Enable warnings for good code quality
target_compile_options(executable PRIVATE -Wall -Wextra -pedantic)