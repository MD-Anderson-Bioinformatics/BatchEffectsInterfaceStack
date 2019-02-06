#!/bin/bash
echo "start build"

echo "2019-02-01-1400"

echo "Build images via docker-compose build, tail -f build_dvlp.log to follow along"
docker-compose -f ./bei-stack/docker-compose.yml build --force-rm --no-cache >> ./build_dvlp.log 2>&1

echo "finish build"
