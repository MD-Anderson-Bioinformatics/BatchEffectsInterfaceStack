#!/bin/bash
echo "start runGDCDownload.bash"

# like 10G or 64G
memsize=`grep -oP '(?<=GDC_MEMSIZE: ).*' /BEI/OUTPUT/gdc.properties`
beiURL=$1
# anything but 'once' means repeat forever
repeat=once
echo memsize=${memsize}
echo beiURL=${beiURL}
echo repeat=${repeat}

echo "start loop in 15 seconds"
sleep 15
while true  
do  
  sleep 300  
  ./runJavaGDC.bash ${memsize} ${beiURL} ${repeat}
  ./runJavaMWB.bash ${memsize} ${beiURL} ${repeat}
done


echo "finish runGDCDownload.bash"

