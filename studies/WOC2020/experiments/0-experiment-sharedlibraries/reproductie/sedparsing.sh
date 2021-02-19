#!/bin/sh
echo "Sed automation parsing"

for i in `seq 1 20`
do
        sed -e 's/\s\+/;/g' pmap$i.txt > spmap$i.txt
done
echo "Done parsing"