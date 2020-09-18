#!/bin/bash
echo "start build"

echo "2019-02-01-1400"

echo "Build images via docker-compose build, tail -f build_extr.log to follow along"
docker-compose -f ../docker-build/bei-stack/docker-compose.yml build --force-rm --no-cache >> ./build_extr.log 2>&1

echo "check for built images"
docker images | grep -i bei

echo "finish build"
