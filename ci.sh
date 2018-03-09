#!/bin/bash

cd $(dirname $0)

function build {
    pushd $1
    ./gradlew clean build
    ret=$?
    if [ $ret -ne 0 ]; then
        exit $ret
    fi
    popd
}

build webmvc
build webflux

exit
