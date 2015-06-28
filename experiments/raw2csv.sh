#!/bin/bash

group=$1
file=$2

cat $file | awk -f raw2csv.awk -v group=$group
