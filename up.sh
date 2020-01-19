#!/bin/bash

docker network create network > /dev/null 2>&1
docker-compose down && \
docker build -f Dockerfile-core -t core . && \
docker build -f Dockerfile-parser -t parser . && \
docker-compose up -d infinispan redis-redisbloom mongo minio1
sleep 5
docker-compose up -d parser
sleep 10
docker-compose up -d core
