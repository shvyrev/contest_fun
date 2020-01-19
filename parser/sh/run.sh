#!/usr/bin/env bash

echo "Jvm Application";

mvn clean package;
echo "\n Java application ready \n"

docker build -f src/main/docker/Dockerfile.jvm -t quarkus/parser-jvm .;
echo "\n Docker image ready \n"

docker run -i --rm -p 8888:8888 quarkus/parser-jvm;
echo "\n Application ready \n"
