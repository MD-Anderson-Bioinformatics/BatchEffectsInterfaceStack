#!/bin/bash
echo "start runJavaGDC.bash"

baseDir=`pwd`
echo baseDir=${baseDir}

jobid=$1
echo jobid=${jobid}

jarFiles=${baseDir}/GDCAPI.jar:${baseDir}/gson-2.8.6.jar:${baseDir}/commons-codec-1.14.jar:${baseDir}/commons-io-2.6.jar:${baseDir}/commons-compress-1.19.jar
echo jarFiles=${jarFiles}

java -Xmx10g -Xms10g -cp "$jarFiles" edu.mda.bcb.gdc.api.GDCJob ${jobid}


