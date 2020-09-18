#!/bin/bash
echo "start runJavaGDC.bash"

baseDir=`pwd`
echo baseDir=${baseDir}

# 64G
memsize=$1
beiURL=$2
# anything but 'once' means repeat forever
repeat=$3
echo memsize=${memsize}
echo beiURL=${beiURL}
echo repeat=${repeat}

jarFiles=${baseDir}/GDCAPI.jar:${baseDir}/gson-2.8.6.jar:${baseDir}/commons-codec-1.14.jar:${baseDir}/commons-io-2.6.jar:${baseDir}/commons-compress-1.19.jar
echo jarFiles=${jarFiles}

# this will look for PROCESS.TXT to find subdir in /BEI/OUTPUT/<jobid> to process
# remove PROCESS.TXT and replace with SUCCCESS.TXT or FAILED.TXT on completion
java -Xmx${memsize} -Xms${memsize} -cp "$jarFiles" edu.mda.bcb.gdc.api.GDCStack /BEI/OUTPUT ${beiURL} ${repeat}

echo "finish runJavaGDC.bash"
