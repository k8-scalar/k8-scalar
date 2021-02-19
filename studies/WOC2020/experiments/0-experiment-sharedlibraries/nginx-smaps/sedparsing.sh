#!/bin/sh
echo "Sed automation parsing"

sed -e 's/\s\+/;/g' 1nginx.txt > s1nginx.txt
sed -e 's/\s\+/;/g' 2nginx.txt > s2nginx.txt
echo "Done parsing"
