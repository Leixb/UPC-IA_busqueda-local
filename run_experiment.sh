#!/bin/bash

set -e

DIR=data

REPORT="${DIR}/report.txt"
HOST="$(hostname)"

LOGFILE="${DIR}/logs.txt"

if [ ! -f "$REPORT" ]; then
    echo "host file experiment ALG HEU GEN NSERV NREP USERS REQUESTS SEED time" >"$REPORT"
fi

trap "exit" INT

function main() {
    mkdir -p "$DIR"
    "$@"
}

function get_time() {
    grep '^:run.*completed' "$1" | sed 's/.*Took \(.*\) secs\./\1/' | awk 'NF==3{print ($1*60+$3)} NF==1 {print}'
}

function get_var() {
    grep "$2" "$1" | tr : = | tr -d ' ' | cut -d= -f 2
}

function gen_report() {
    NAME=ex4_u
    ALG=HC
    HEU=Max
    GEN=1
    SEED=rand
    for OUT_FILE_I in "$DIR"/ex*.txt; do

        NSERV=$(get_var "$OUT_FILE_I" nserv)
        NREP=$(get_var "$OUT_FILE_I" nrep)
        USERS=$(get_var "$OUT_FILE_I" users)
        REQUESTS=$(get_var "$OUT_FILE_I" '^requests')
        ARGS="$ALG $HEU $GEN $NSERV $NREP $USERS $REQUESTS $SEED"

        runtime="$(get_time "$OUT_FILE_I")"

        echo "$HOST $OUT_FILE_I $NAME $ARGS ${runtime}"
    done
}

function run_experiment() {
    REP=${REP:-10}

    ALG=${ALG:-HC}
    HEU=${HEU:-Max}
    GEN=${GEN:-1}

    NSERV=${NSERV:-50}
    NREP=${NREP:-5}
    USERS=${USERS:-200}
    REQUESTS=${REQUESTS:-5}

    SEED=${SEED:-1234}
    OUTPUT="${OUTPUT:-experiment}"
    OUT_FILE="$(mktemp -u "$OUTPUT-XXXX")"

    ARGS="$ALG $HEU $GEN $NSERV $NREP $USERS $REQUESTS $SEED"

    echo "################################################################################"
    echo "# $HOST Running $REP repetitions with args $ARGS:"
    echo "################################################################################"

    for (( i = 0; i < REP; i++ )); do
        OUT_FILE_I="${OUT_FILE}.$i.txt"
        echo -n " Running repetition $i >${OUT_FILE_I}: ..."

        ./gradlew run --info --args "$ARGS" >"${OUT_FILE_I}"

        runtime="$(get_time "$OUT_FILE_I")"

        echo -e "\r  - DONE repetition $i >${OUT_FILE_I} in ${runtime} seconds"
        echo "$HOST $OUT_FILE_I $NAME $ARGS ${runtime}" >>"$REPORT"
    done
}

function ex4_s() {
    NAME=ex4_s
    OUTPUT_BASE="${DIR}/experiment4_serv"
    SEED="rand"
    NSERV=50
    USERS=200
    INC=50
    MAX=${1:-500}
    echo "NSERV from $NSERV -> $MAX (+$INC)"
    while (( NSERV <= MAX )); do
        OUTPUT="${OUTPUT_BASE}-${NSERV}"
        run_experiment
        NSERV=$((NSERV+INC))
    done
}

function ex4_u() {
    NAME=ex4_u
    OUTPUT_BASE="${DIR}/experiment4_users"
    SEED="rand"
    NSERV=50
    USERS=100
    INC=100
    MAX=${1:-700}
    echo "USERS from $USERS -> $MAX (+$INC)"
    while (( USERS <= MAX )); do
        OUTPUT="${OUTPUT_BASE}-${USERS}"
        run_experiment
        USERS=$((USERS+INC))
    done
}

main "$@" | tee -a "$LOGFILE"
