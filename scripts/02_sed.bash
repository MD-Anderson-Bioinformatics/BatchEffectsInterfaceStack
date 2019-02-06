#!/bin/bash
echo "start sedFiles"

echo "2019-02-01-1035"

BASEDIR="<REPLACE>"
DESIREDTAG="<REPLACE>"
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
PROP_DIR=${BASEDIR}/docker_data/PROPS
INDICES_DIR=${BASEDIR}/docker_data/INDICES
GDC_ARCHIVES_DIR=${BASEDIR}/docker_data
STD_ARCHIVES_DIR=${BASEDIR}/docker_data
JOB_OUTPUT_DIR=${BASEDIR}/docker_data/OUTPUT
WEBSITE_DIR=${BASEDIR}/docker_data/WEBSITE
GENOMICS_FILETO_DIR=${BASEDIR}/docker_data/GENOMICS/FILETO
GENOMICS_MAPS_DIR=${BASEDIR}/docker_data/GENOMICS/MAPS

echo "docker-compose_template.yml"
if [ -e ${BASEDIR}/build/bei-stack/docker-compose.yml ]; then
	 rm ${BASEDIR}/build/bei-stack/docker-compose.yml
fi
sed -e "s|<BEV-PORT>|${BEV_PORT}|g" -e "s|<BEI-PORT>|${BEI_PORT}|g" -e "s|<SUBNET>|${SUBNET}|g" -e "s|<ENVIRON>|${ENVIRON}|g" -e "s|<BEI-IMAGETXT>|${BEI_IMAGETXT}|g" -e "s|<BEI-DESIREDTAG>|${BEI_DESIREDTAG}|g" -e "s|<GDC-IMAGETXT>|${GDC_IMAGETXT}|g" -e "s|<GDC-DESIREDTAG>|${GDC_DESIREDTAG}|g" -e "s|<MBATCH-IMAGETXT>|${MBATCH_IMAGETXT}|g" -e "s|<MBATCH-DESIREDTAG>|${MBATCH_DESIREDTAG}|g" -e "s|<BEV-IMAGETXT>|${BEV_IMAGETXT}|g" -e "s|<BEV-DESIREDTAG>|${BEV_DESIREDTAG}|g" -e "s|<PROP-DIR>|${PROP_DIR}|g" -e "s|<INDICES-DIR>|${INDICES_DIR}|g" -e "s|<GDC-ARCHIVES-DIR>|${GDC_ARCHIVES_DIR}|g" -e "s|<STD-ARCHIVES-DIR>|${STD_ARCHIVES_DIR}|g" -e "s|<JOB-OUTPUT-DIR>|${JOB_OUTPUT_DIR}|g" -e "s|<WEBSITE-DIR>|${WEBSITE_DIR}|g" -e "s|<GENOMICS-FILETO-DIR>|${GENOMICS_FILETO_DIR}|g" -e "s|<GENOMICS-MAPS-DIR>|${GENOMICS_MAPS_DIR}|g" ${BASEDIR}/build/bei-stack/docker-compose_template.yml > ${BASEDIR}/build/bei-stack/docker-compose.yml

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

echo "gdcdc-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/bei-stack/gdcdc-image/Dockerfile ]; then
	rm ${BASEDIR}/build/bei-stack/gdcdc-image/Dockerfile
fi
sed -e "s|<DOCKER_UID>|${USER_ID}|g" ${BASEDIR}/build/bei-stack/gdcdc-image/Dockerfile_template > ${BASEDIR}/build/bei-stack/gdcdc-image/Dockerfile

echo "mbatch-image/Dockerfile_template"
if [ -e ${BASEDIR}/build/bei-stack/mbatch-image/Dockerfile ]; then
	rm ${BASEDIR}/build/bei-stack/mbatch-image/Dockerfile
fi
sed -e "s|<USERID>|${USER_ID}|g" ${BASEDIR}/build/bei-stack/mbatch-image/Dockerfile_template > ${BASEDIR}/build/bei-stack/mbatch-image/Dockerfile

echo "PROPS/bei.properties_template"
if [ -e ${BASEDIR}/docker_data/PROPS/bei.properties ]; then
	rm ${BASEDIR}/docker_data/PROPS/bei.properties
fi
sed -e "s|<SERVER_TITLE>|${SERVER_TITLE}|g" -e "s|<BEI_URL>|${BEI_URL}|g" -e "s|<BEV_URL>|${BEV_URL}|g" ${BASEDIR}/docker_data/PROPS/bei.properties_template > ${BASEDIR}/docker_data/PROPS/bei.properties

echo "finish sedFiles"
