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

jarFiles=${baseDir}/GDCDownload.jar:${baseDir}/gson-2.7.jar:${baseDir}/commons-io-2.5.jar:${baseDir}/commons-compress-1.12.jar:${baseDir}/commons-lang3-3.4.jar:${baseDir}/commons-math3-3.6.1.jar:${baseDir}/commons-codec-1.11.jar
echo jarFiles=${jarFiles}

# this will look for PROCESS.TXT to find subdir in /BEI/OUTPUT/<jobid> to process
# remove PROCESS.TXT and replace with SUCCCESS.TXT or FAILED.TXT on completion
java -Xmx${memsize} -Xms${memsize} -cp "$jarFiles" edu.mda.bcb.gdc.download.DatasetConfig /BEI/OUTPUT ${beiURL} ${repeat}

echo "finish runJavaGDC.bash"
