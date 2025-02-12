#!/bin/bash

set -e

DIR=data

REPORT="${DIR}/report.txt"
HOST="$(hostname)"

GIT="$(git rev-parse --short HEAD)"

LOGFILE="${DIR}/logs.txt"

HEADER="host commit file experiment ALG HEU GEN NSERV NREP USERS REQUESTS SEED STEPS STITER K LAMB time ttt heuM heuT" 


if [ ! -f "$REPORT" ]; then
    mkdir -p $DIR
    echo "$HEADER" >"$REPORT"
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
    grep "$2" "$1" | tr : = | tr -d ' ' | cut -d= -f 2 | tail -n 1
}

function gen_report() {
    FILE_PREFIX="${1:-ex}"
    ALG='HC?'
    HEU='Max?'
    GEN='1?'
    SEED=rand

    STEPS='500000?'
    STITER='100?'
    K='20?'
    LAMB='0.005?'

    HOST="${HOST}?"
    GIT="${GIT}?"

    local MAX
    local TOTAL

    echo "$HEADER"

    for OUT_FILE_I in "$DIR/$FILE_PREFIX"*.txt; do

        NAME_="$(echo $OUT_FILE_I | sed 's#.*/\([^-]*\)-.*#\1#')"

        NSERV=$(get_var "$OUT_FILE_I" nserv)
        NREP=$(get_var "$OUT_FILE_I" nrep)
        USERS=$(get_var "$OUT_FILE_I" users)
        REQUESTS=$(get_var "$OUT_FILE_I" '^requests')

        HEU_=$(get_var "$OUT_FILE_I" '^heu')
        HEU_=${HEU_:-$HEU}
        ALG_=$(get_var "$OUT_FILE_I" '^algo')
        ALG_=${ALG_:-$ALG}

        HOST_=$(get_var "$OUT_FILE_I" '^host')
        HOST_=${HOST_:-$HOST}
        GIT_=$(get_var "$OUT_FILE_I" '^commit')
        GIT_=${GIT_:-$GIT}

        SEEDS=$(get_var "$OUT_FILE_I" '^seeds')
        SEEDR=$(get_var "$OUT_FILE_I" '^seedr')

        TTT=$(get_var "$OUT_FILE_I" '^Tiempo')
        MAX=$(get_var "$OUT_FILE_I" 'Max')
        TOTAL=$(get_var "$OUT_FILE_I" 'Total')

        STEPS_=$(get_var "$OUT_FILE_I" '^steps')
        STEPS_=${STEPS_:-$STEPS}
        STITER_=$(get_var "$OUT_FILE_I" '^stiter ')
        STITER_=${STITER_:-$STITER}
        K_=$(get_var "$OUT_FILE_I" '^k ')
        K_=${K_:-$K}
        LAMB_=$(get_var "$OUT_FILE_I" '^lamb')
        LAMB_=${LAMB_:-$LAMB}

        GEN_=$(get_var "$OUT_FILE_I" '^findSmallest')
        [[ $GEN_ = true ]] && GEN_="1"
        [[ $GEN_ = false ]] && GEN_="0"
        GEN_=${GEN_:-$GEN}

        ARGS="$ALG_ $HEU_ $GEN_ $NSERV $NREP $USERS $REQUESTS ${SEED}(${SEEDS},${SEEDR})"
        ARGS="$ARGS $STEPS_ $STITER_ $K_ $LAMB_"

        runtime="$(get_time "$OUT_FILE_I")"

        echo "$HOST_ $GIT_ $OUT_FILE_I $NAME_ $ARGS ${runtime} $TTT $MAX $TOTAL"
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

    STEPS=${STEPS:-500000}
    STITER=${STITER:-100}
    K=${K:-20}
    LAMB=${LAMB:-0.005}

    OUTPUT="${OUTPUT:-experiment}"
    OUT_FILE="$(mktemp -u "$OUTPUT-XXXX")"

    ARGS="$ALG $HEU $GEN $NSERV $NREP $USERS $REQUESTS $SEED"

    SA="$STEPS $STITER $K $LAMB"

    local MAX
    local TOTAL

    echo "################################################################################"
    echo "# $HOST Running $REP repetitions with args $ARGS:"
    echo "################################################################################"

    for (( i = 0; i < REP; i++ )); do
        OUT_FILE_I="${OUT_FILE}.$i.txt"
        echo -n " Running repetition $i >${OUT_FILE_I}: ..."

        echo -e "host = $HOST\ncommit = $GIT\n" >"${OUT_FILE_I}"
        ./gradlew run --info --args "$ARGS $SA" >>"${OUT_FILE_I}" 2>&1

        TTT=$(get_var "$OUT_FILE_I" '^Tiempo')
        MAX=$(get_var "$OUT_FILE_I" 'Max')
        TOTAL=$(get_var "$OUT_FILE_I" 'Total')

        SEEDS=$(get_var "$OUT_FILE_I" '^seeds')
        SEEDR=$(get_var "$OUT_FILE_I" '^seedr')

        runtime="$(get_time "$OUT_FILE_I")"

        echo -e "\r  - DONE repetition $i >${OUT_FILE_I} in ${runtime} seconds"
        echo "$HOST $GIT $OUT_FILE_I $NAME ${ARGS}($SEEDS,$SEEDR) $SA ${runtime} $TTT $MAX $TOTAL" >>"$REPORT"
    done
}

function ex4_s() {
    NAME="${FUNCNAME[0]}"
    OUTPUT_BASE="${DIR}/${NAME}"
    SEED="rand"
    NSERV=${2:-50}
    local INC=50
    local MAX=${1:-1000}
    echo "NSERV from $NSERV -> $MAX (+$INC)"
    while (( NSERV <= MAX )); do
        OUTPUT="${OUTPUT_BASE}-${NSERV}"
        run_experiment
        NSERV=$((NSERV+INC))
    done
}

function ex4_u() {
    NAME="${FUNCNAME[0]}"
    OUTPUT_BASE="${DIR}/${NAME}"
    SEED="rand"
    USERS=${2:-100}
    local INC=100
    local MAX=${1:-1000}
    echo "USERS from $USERS -> $MAX (+$INC)"
    while (( USERS <= MAX )); do
        OUTPUT="${OUTPUT_BASE}-${USERS}"
        run_experiment
        USERS=$((USERS+INC))
    done
}

function ex7() {
    NAME="${FUNCNAME[0]}"
    OUTPUT_BASE="${DIR}/${NAME}"
    SEED="rand"
    NREP=5
    local INC=5
    local MAX=${1:-25}
    echo "NREP from $NREP -> $MAX (+$INC)"
    while (( NREP <= MAX )); do
        OUTPUT="${OUTPUT_BASE}-${NREP}"
        run_experiment
        NREP=$((NREP+INC))
    done
}

function ex2() {
    NAME="${FUNCNAME[0]}"
    GEN=0
    OUTPUT="${DIR}/${NAME}-${GEN}"
    run_experiment
    GEN=1
    OUTPUT="${DIR}/${NAME}-${GEN}"
    run_experiment
}

function ex4() {
    ex4_s "$@"
    ex4_u "$@"
}

function ex3() {
    NAME="${FUNCNAME[0]}"
    ALG="SA"
    
    STEPS=${1:-500000}
    STITER=${2:-100}
    K=${3:-20}
    LAMB=${4:-0.005}

    SA="$STEPS-$STITER-$K-$LAMB"

    OUTPUT="${DIR}/${NAME}-${SA}"
    run_experiment
}

function ex3o() {
    NAME="${FUNCNAME[0]}"
    ALG="SA"
    
    STEPS=${1:-500000}
    STITER=${2:-100}
    K=${3:-20}
    LAMB=${4:-0.005}

    SA="$STEPS-$STITER-$K-$LAMB"


    REP=${REP:-10}

    ALG=${ALG:-HC}
    HEU=${HEU:-Max}
    GEN=${GEN:-1}

    NSERV=${NSERV:-50}
    NREP=${NREP:-5}
    USERS=${USERS:-200}
    REQUESTS=${REQUESTS:-5}

    SEED=${SEED:-1234}

    STEPS=${STEPS:-500000}
    STITER=${STITER:-100}
    K=${K:-20}
    LAMB=${LAMB:-0.005}

    ARGS="$ALG $HEU $GEN $NSERV $NREP $USERS $REQUESTS $SEED"

    SA="$STEPS $STITER $K $LAMB"

    echo step heuM heuT
    SHOW_STEPS=1 ./gradlew run --info --args "$ARGS $SA" 2>&1 | grep '^STEP' | cut -d' ' -f2,3,4

}


main "$@" | tee -a "$LOGFILE"
