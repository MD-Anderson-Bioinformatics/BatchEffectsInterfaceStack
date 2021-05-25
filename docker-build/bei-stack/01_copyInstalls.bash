#!/bin/bash

echo "START 01_copyInstalls"

set -e

BASE_DIR=$1

echo "Copy BatchEffectsInterface WAR"
cp ${BASE_DIR}/apps/BatchEffectsInterface/target/*.war ${BASE_DIR}/docker-build/bei-stack/bei-image/installations/BEI#BEI.war

echo "Copy Batch Effects Viewer WAR"
cp ${BASE_DIR}/../DataAPI/apps/BatchEffectsViewer/target/*.war ${BASE_DIR}/docker-build/bei-stack/bev-image/installations/BEI#BEV.war

echo "Copy GDCAPI JAR"
cp ${BASE_DIR}/../StandardizedData/apps/GDCAPI/target/*.jar ${BASE_DIR}/docker-build/bei-stack/dc-image/installations/.
mv ${BASE_DIR}/docker-build/bei-stack/dc-image/installations/GDCAPI-*.jar ${BASE_DIR}/docker-build/bei-stack/dc-image/installations/GDCAPI.jar

echo "Copy StdMWUtils JAR"
cp ${BASE_DIR}/../StandardizedData/apps/StdMWUtils/target/*.jar ${BASE_DIR}/docker-build/bei-stack/dc-image/installations/.
mv ${BASE_DIR}/docker-build/bei-stack/dc-image/installations/StdMWUtils-*.jar ${BASE_DIR}/docker-build/bei-stack/dc-image/installations/StdMWUtils.jar

echo "List BEI Installations"
ls -lh ${BASE_DIR}/docker-build/bei-stack/bei-image/installations/

echo "List BEV Installations"
ls -lh ${BASE_DIR}/docker-build/bei-stack/bev-image/installations/

echo "List GDCDC Installations"
ls -lh ${BASE_DIR}/docker-build/bei-stack/dc-image/installations/

echo "FINISH 01_copyInstalls"

