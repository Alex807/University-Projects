#!/bin/bash

USE_VALGRIND=false
if [ "$1" == "--valgrind" ] || [ "$1" == "-v" ]; then
    USE_VALGRIND=true
fi

echo "Building project..."
cmake -B build -DCMAKE_BUILD_TYPE=Debug
cmake --build build

if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo "Build successful!"

if [ "$USE_VALGRIND" = true ]; then
    echo $'Running with Valgrind... \n'
    valgrind --leak-check=full --show-leak-kinds=all --track-origins=yes ./build/bin/executable
else
    echo $'Running executable...\n'
    ./build/bin/executable
fi
