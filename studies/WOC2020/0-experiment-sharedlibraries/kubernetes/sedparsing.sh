#!/bin/sh
echo "Sed automation parsing"

sed -e 's/\s\+/;/g' 1npm.txt > s1npm.txt
sed -e 's/\s\+/;/g' 2npm.txt > s2npm.txt
echo "Done parsing"
