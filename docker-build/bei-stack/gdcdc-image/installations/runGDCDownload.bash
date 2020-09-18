#!/bin/bash
echo "start runGDCDownload.bash"

# like 10G or 64G
memsize=`grep -oP '(?<=GDC_MEMSIZE: ).*' /BEI/OUTPUT/gdc.properties`
beiURL=$1
# anything but 'once' means repeat forever
repeat=$2
echo memsize=${memsize}
echo beiURL=${beiURL}
echo repeat=${repeat}

echo "runJavaGDC.bash in 15 seconds"
sleep 15
./runJavaGDC.bash ${memsize} ${beiURL} ${repeat}

echo "finish runGDCDownload.bash"

