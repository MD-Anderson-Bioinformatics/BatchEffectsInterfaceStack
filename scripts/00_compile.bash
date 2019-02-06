#!/bin/bash
echo "start compile"

echo "2019-02-01-0940"

BASEDIR=`pwd`

cd ${BASEDIR}
cd ../apps/BatchEffectsInterface
ant -f build.xml
ls -l ./dist

cd ${BASEDIR}
cp ../apps/BatchEffectsInterface/dist/BatchEffectsInterface.war ../docker-build/bei-stack/bei-image/installations/.

echo "-------------------------------------------"

ls -l ../docker-build/bei-stack/bei-image/installations/.

echo "finish compile"
