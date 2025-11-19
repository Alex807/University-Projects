cmake_minimum_required(VERSION 3.16)

project(ProiectPAOO VERSION 1.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)
add_library(paoo_core STATIC
    src/elev.cpp
    src/school.cpp
)
target_include_directories(paoo_core
    PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/include
)

add_executable(proiect_paoo
    src/main.cpp
)
target_link_libraries(proiect_paoo PRIVATE paoo_core)

enable_testing()