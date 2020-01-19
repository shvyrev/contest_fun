#!/usr/bin/env bash

echo "Native Application";

export MAVEN_OPTS="-Xmx4096m -XX:MaxPermSize=1024m";
mvn clean package -Pnative -Dquarkus.native.container-build=true;
echo "\n Native build ready\n";

docker build -f src/main/docker/Dockerfile.native -t quarkus/core .;
echo "\n Docker image ready \n";

docker run -i --rm -p 8080:8080 quarkus/core;
echo "\n  Application ready on port : 8080 \n";