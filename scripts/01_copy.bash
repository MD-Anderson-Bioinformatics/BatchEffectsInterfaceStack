#!/bin/bash
echo "start copy"

echo "2019-02-01-1015"

if [ $# -eq 0 ]
	then
		echo "1) No arguments supplied--provide a path for build location"
		exit 1
fi
if [ -z "$1" ]
	then
		echo "2) No argument supplied--provide a path for build location"
		exit 1
fi

LOCATION=${1}
echo LOCATION=${LOCATION}

echo "make dirs"
mkdir -p ${LOCATION}
mkdir -p ${LOCATION}/build

echo "docker_data"
cp -r ./docker_data ${LOCATION}
echo "02_sed.bash"
cp -r ./02_sed.bash    ${LOCATION}/build/.
echo "03_build.bash"
cp -r ./03_build.bash  ${LOCATION}/build/.
echo "docker-build"
cp -r ../docker-build/* ${LOCATION}/build/.
echo "server.xml"
cp -r ./server.xml  ${LOCATION}/build/bei-stack/bei-image/installations/.

ls -l ${LOCATION}/*

echo "switch to ${LOCATION} to continue running"
echo "Edit <REPLACE> values in 02_sed.bash before proceeding"

echo "finish copy"
