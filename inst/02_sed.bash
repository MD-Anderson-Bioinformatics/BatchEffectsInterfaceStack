#!/bin/bash
echo "start sedFiles"

echo "BEA_VERSION_TIMESTAMP"

BASEDIR="<REPLACE>"
DESIREDTAG="BEI_<REPLACE>"
SUBNET="<REPLACE>"
SERVER_TITLE="<REPLACE>"

USER_ID="1011"
ENVIRON="EXTERNAL"
BEV_PORT="8080"
BEI_PORT="8181"

BEI_URL="<REPLACE>:${BEV_PORT}"
BEV_URL="<REPLACE>:${BEI_PORT}"

BEI_IMAGETXT="mdabcb/bei_image"
BEI_DESIREDTAG=$DESIREDTAG
GDC_IMAGETXT="mdabcb/gdc_image"
GDC_DESIREDTAG=$DESIREDTAG
MBATCH_IMAGETXT="mdabcb/mbatch_image"
MBATCH_DESIREDTAG=$DESIREDTAG
BEV_IMAGETXT="mdabcb/bev_image"
BEV_DESIREDTAG=$DESIREDTAG
PROP_DIR=${BASEDIR}/ext/PROPS
JOB_OUTPUT_DIR=${BASEDIR}/ext/OUTPUT
WEBSITE_DIR=${BASEDIR}/ext/WEBSITE
UTIL_DIR=${BASEDIR}/ext/UTIL

echo "PROPS/bei.properties_template"
if [ -e ${PROP_DIR}/bei.properties ]; then
	rm ${PROP_DIR}/bei.properties
fi
sed -e "s|<SERVER_TITLE>|${SERVER_TITLE}|g" -e "s|<BEI_URL>|${BEI_URL}|g" -e "s|<BEV_URL>|${BEV_URL}|g" ${PROP_DIR}/bei.properties_template > ${PROP_DIR}/bei.properties

echo "bei-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/bei-stack/bei-image/Dockerfile ]; then
	rm ${BASEDIR}/build/bei-stack/bei-image/Dockerfile
fi
sed -e "s|<DOCKER_UID>|${USER_ID}|g" ${BASEDIR}/build/bei-stack/bei-image/Dockerfile_template > ${BASEDIR}/build/bei-stack/bei-image/Dockerfile

echo "bev-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/bei-stack/bev-image/Dockerfile ]; then
	rm ${BASEDIR}/build/bei-stack/bev-image/Dockerfile
fi
sed -e "s|<USERID>|${USER_ID}|g" ${BASEDIR}/build/bei-stack/bev-image/Dockerfile_template > ${BASEDIR}/build/bei-stack/bev-image/Dockerfile

echo "dc-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/bei-stack/dc-image/Dockerfile ]; then
	rm ${BASEDIR}/build/bei-stack/dc-image/Dockerfile
fi
sed -e "s|<DOCKER_UID>|${USER_ID}|g" ${BASEDIR}/build/bei-stack/dc-image/Dockerfile_template > ${BASEDIR}/build/bei-stack/dc-image/Dockerfile

echo "mbatch-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/bei-stack/mbatch-image/Dockerfile ]; then
	rm ${BASEDIR}/build/bei-stack/mbatch-image/Dockerfile
fi
sed -e "s|<USERID>|${USER_ID}|g" ${BASEDIR}/build/bei-stack/mbatch-image/Dockerfile_template > ${BASEDIR}/build/bei-stack/mbatch-image/Dockerfile

echo "docker-compose_template.yml"
if [ -e ${BASEDIR}/build/bei-stack/docker-compose.yml ]; then
	 rm ${BASEDIR}/build/bei-stack/docker-compose.yml
fi
sed -e "s|<BEV-PORT>|${BEV_PORT}|g" -e "s|<BEI-PORT>|${BEI_PORT}|g" -e "s|<SUBNET>|${SUBNET}|g" -e "s|<ENVIRON>|${ENVIRON}|g" -e "s|<BEI-IMAGETXT>|${BEI_IMAGETXT}|g" -e "s|<BEI-DESIREDTAG>|${BEI_DESIREDTAG}|g" -e "s|<GDC-IMAGETXT>|${GDC_IMAGETXT}|g" -e "s|<GDC-DESIREDTAG>|${GDC_DESIREDTAG}|g" -e "s|<MBATCH-IMAGETXT>|${MBATCH_IMAGETXT}|g" -e "s|<MBATCH-DESIREDTAG>|${MBATCH_DESIREDTAG}|g" -e "s|<BEV-IMAGETXT>|${BEV_IMAGETXT}|g" -e "s|<BEV-DESIREDTAG>|${BEV_DESIREDTAG}|g" -e "s|<PROP-DIR>|${PROP_DIR}|g" -e "s|<JOB-OUTPUT-DIR>|${JOB_OUTPUT_DIR}|g" -e "s|<WEBSITE-DIR>|${WEBSITE_DIR}|g" -e "s|<UTIL-DIR>|${UTIL_DIR}|g" ${BASEDIR}/build/bei-stack/docker-compose_template.yml > ${BASEDIR}/build/bei-stack/docker-compose.yml

echo "finish sedFiles"
