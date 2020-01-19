#!/usr/bin/env bash

if [ -z $1 ]
    then
        echo "Add parameter PID of java process"
    else
        watch -n 1 ps x -o pid,rss,command -p $1
fi
