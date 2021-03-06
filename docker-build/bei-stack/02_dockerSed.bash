#!/bin/bash

echo "START 02_dockerSed"

echo "BEA_VERSION_TIMESTAMP"

set -e

BASE_DIR=$1

# release version, such as BEA_VERSION_TIMESTAMP
RELEASE=${2}
# user id, such as 2002
USER_ID=${3}

# outside port for BEI Tomcat, such as 8080
BEI_PORT=${4}
# outside port for BEV Tomcat, such as 8080
BEV_PORT=${5}
# subnet to use for compose stack, such as 128.1.1.1/24. Use 0 for "no subnet"
SUBNET=${6}
# environment, usually dvlp, stag, or prod
ENVIRON=${7}
START_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/10p_startPROD.bash
STOP_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/20p_stopPROD.bash
UPCHECK_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/30p_checkPROD.bash
if [ "${ENVIRON}" == "stag" ]; then
	START_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/10s_startSTAG.bash
	STOP_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/20s_stopSTAG.bash
	UPCHECK_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/30s_checkSTAG.bash
fi
if [ "${ENVIRON}" == "dvlp" ]; then
	START_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/10d_startDVLP.bash
	STOP_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/20d_stopDVLP.bash
	UPCHECK_SCRIPT=/deploy-dir-part-of-stack/${RELEASE}/30d_checkDVLP.bash
fi
if [ "${ENVIRON}" == "hub" ]; then
	START_SCRIPT=-
	STOP_SCRIPT=-
	UPCHECK_SCRIPT=-
fi

# local paths point to local setup for DVLP, STAG, or PROD
LOCAL_PATH_ENV=${8}
### file with properties and job list for this installation
PROP_DIR=${LOCAL_PATH_ENV}/PROPS
### directory for output from BEI, GDCDownload, and MBatch Results
JOB_OUTPUT_DIR=${LOCAL_PATH_ENV}/OUTPUT
### directory for the Batch Effects Website
WEBSITE_DIR=${LOCAL_PATH_ENV}/WEBSITE
### read-only directory for util files
UTIL_DIR=${LOCAL_PATH_ENV}/UTIL

# URL and tag to use as image name for BEI image, such as mdabcb/smw_image:DAP_BEA_VERSION_TIMESTAMP
BEI_IMAGEURL=${9}
# URL and tag to use as image name for MBatch image, such as mdabcb/smw_image:DAP_BEA_VERSION_TIMESTAMP
MBATCH_IMAGEURL=${10}
# URL and tag to use as image name for GDC Download image, such as mdabcb/smw_image:DAP_BEA_VERSION_TIMESTAMP
GDC_IMAGEURL=${11}
# URL and tag to use as image name for BEV image, such as mdabcb/smw_image:DAP_BEA_VERSION_TIMESTAMP
BEV_IMAGEURL=${12}

OUTSIDE_CONFIGPATH=${13}
ZIPTMPPATH=${14}

# if SUBNET is not equal to 0, then also remove the #SUBNET comments so the IPAM gets used
IPAM_COMMENT=#SUBNET
if [ "${SUBNET}" != "0" ]; then
	# replace comment with nothing, to activate IPAM settings
	IPAM_COMMENT=
fi

echo "create Dockerfile from Dockerfile_template"

rm -f ${BASE_DIR}/bei-image/Dockerfile
sed -e "s|<RELEASE_VERSION>|${RELEASE}|g" \
    -e "s|<USERID>|${USER_ID}|g" \
    -e "s|<LOG_DIR>|${JOB_OUTPUT_DIR}|g" \
    -e "s|<START_SCRIPT>|${START_SCRIPT}|g" \
    -e "s|<STOP_SCRIPT>|${STOP_SCRIPT}|g" \
    -e "s|<UPCHECK_SCRIPT>|${UPCHECK_SCRIPT}|g" \
    ${BASE_DIR}/bei-image/Dockerfile_template > ${BASE_DIR}/bei-image/Dockerfile

rm -f ${BASE_DIR}/bev-image/Dockerfile
sed -e "s|<RELEASE_VERSION>|${RELEASE}|g" \
    -e "s|<USERID>|${USER_ID}|g" \
    -e "s|<LOG_DIR>|${JOB_OUTPUT_DIR}|g" \
    -e "s|<START_SCRIPT>|${START_SCRIPT}|g" \
    -e "s|<STOP_SCRIPT>|${STOP_SCRIPT}|g" \
    -e "s|<UPCHECK_SCRIPT>|${UPCHECK_SCRIPT}|g" \
    ${BASE_DIR}/bev-image/Dockerfile_template > ${BASE_DIR}/bev-image/Dockerfile

rm -f ${BASE_DIR}/dc-image/Dockerfile
sed -e "s|<RELEASE_VERSION>|${RELEASE}|g" \
    -e "s|<USERID>|${USER_ID}|g" \
    -e "s|<LOG_DIR>|${JOB_OUTPUT_DIR}|g" \
    -e "s|<START_SCRIPT>|${START_SCRIPT}|g" \
    -e "s|<STOP_SCRIPT>|${STOP_SCRIPT}|g" \
    -e "s|<UPCHECK_SCRIPT>|${UPCHECK_SCRIPT}|g" \
    ${BASE_DIR}/dc-image/Dockerfile_template > ${BASE_DIR}/dc-image/Dockerfile

rm -f ${BASE_DIR}/mbatch-image/Dockerfile
sed -e "s|<RELEASE_VERSION>|${RELEASE}|g" \
    -e "s|<USERID>|${USER_ID}|g" \
    -e "s|<LOG_DIR>|${JOB_OUTPUT_DIR}|g" \
    -e "s|<START_SCRIPT>|${START_SCRIPT}|g" \
    -e "s|<STOP_SCRIPT>|${STOP_SCRIPT}|g" \
    -e "s|<UPCHECK_SCRIPT>|${UPCHECK_SCRIPT}|g" \
    ${BASE_DIR}/mbatch-image/Dockerfile_template > ${BASE_DIR}/mbatch-image/Dockerfile

echo "create docker-compose.yml from docker-compose_template.yml"

rm -f ${BASE_DIR}/docker-compose.yml
sed -e "s|<SUBNET>|${SUBNET}|g" \
    -e "s|<ENVIRON>|${ENVIRON}|g" \
    -e "s|<BEI-IMAGEURL>|${BEI_IMAGEURL}|g" \
    -e "s|<BEI-PORT>|${BEI_PORT}|g" \
    -e "s|<MBATCH-IMAGEURL>|${MBATCH_IMAGEURL}|g" \
    -e "s|<GDC-IMAGEURL>|${GDC_IMAGEURL}|g" \
    -e "s|<BEV-IMAGEURL>|${BEV_IMAGEURL}|g" \
    -e "s|<BEV-PORT>|${BEV_PORT}|g" \
    -e "s|<PROP-DIR>|${PROP_DIR}|g" \
    -e "s|<JOB-OUTPUT-DIR>|${JOB_OUTPUT_DIR}|g" \
    -e "s|<WEBSITE-DIR>|${WEBSITE_DIR}|g" \
    -e "s|<UTIL-DIR>|${UTIL_DIR}|g" \
    -e "s|#SUBNET|${IPAM_COMMENT}|g" \
    -e "s|<IMAGEURL>|${IMAGE_URL}|g" \
    -e "s|<CONFIGPATH>|${OUTSIDE_CONFIGPATH}|g" \
    -e "s|<ZIPTMPPATH>|${ZIPTMPPATH}|g" \
    ${BASE_DIR}/docker-compose_template.yml > ${BASE_DIR}/docker-compose.yml

# then build with docker-compose -f docker-compose.yml build --force-rm --no-cache

echo "FINISH 02_dockerSed"

