#!/bin/bash

# Check if a file is given as an argument
if [ $# -ne 1 ]; then
    echo "path miss"
    exit 1
fi

file_path="$1"

# Check if the file has less than 3 lines AND more than 1000 words AND more than 2000 characters to be considered UNSAFE
if [[ $(wc -l < "$file_path") -lt 3 && $(wc -w < "$file_path") -gt 1000 && $(wc -m < "$file_path") -gt 2000 ]]; then
    echo $file_path
    exit 2
fi

# Check if the file contains non-ASCII characters
if LC_ALL=C grep -q '[^[:print:][:space:]]' "$file_path"; then
    echo $file_path
    exit 2
fi

# Search for keywords in the file
keywords=("corrupted" "dangerous" "risk" "attack" "malware" "malicious") # List of keywords to search for in the file
for keyword in "${keywords[@]}"; do
    if grep -qi "$keyword" "$file_path"; then
        echo $file_path
        exit 2
    fi
done

echo "SAFE"
exit 0 #end the script as safe
