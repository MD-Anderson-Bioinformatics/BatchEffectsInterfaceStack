#!/bin/bash
echo "start runMBatch.bash"

beiURL=$1
echo beiURL=${beiURL}

echo "runRproc.bash in 15 seconds BEA_VERSION_TIMESTAMP"
sleep 15
./runRproc.bash ${beiURL}

echo "finish runMBatch.bash"

