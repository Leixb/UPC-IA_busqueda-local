#!/bin/bash

REP=${REP:-10}

ALG=${ALG:-HC}
HEU=${HEU:-Max}
GEN=${GEN:-0}

NSERV=${NSERV:-50}
NREP=${NREP:-5}
USERS=${USERS:-200}
REQUESTS=${REQUESTS:-5}

SEED=${SEED:-1234}

for (( i = 0; i < REP; i++ )); do
    echo "Running experiment $i"
    time0=$(date +%s)

    ./gradlew run --quiet --args "$ALG $HEU $GEN $NSERV $NREP $USERS $REQUESTS $SEED"

    time1=$(date +%s)
    runtime=$((time1-time0))

    echo "DONE $i: time: $runtime"
done


