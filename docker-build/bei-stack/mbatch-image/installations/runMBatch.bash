#!/bin/bash
echo "start runMBatch.bash"

beiURL=$1
echo beiURL=${beiURL}

echo "runRproc.bash in 15 seconds 2017-11-20-1200"
sleep 15
./runRproc.bash ${beiURL}

echo "finish runMBatch.bash"

