#!/bin/bash

set -eu

PROJECT_HOST_DIR=${1? Missing Project host dir.}
VERSION=${2? Missing Android studio version.}

mkdir -v -p \
    ~/.docker-shipfast-demo/android-studio/${VERSION}/${PROJECT_HOST_DIR}/Android \
    ~/.docker-shipfast-demo/android-studio/${VERSION}/${PROJECT_HOST_DIR}/.AndroidStudio${VERSION} \
    ~/.docker-shipfast-demo/android-studio/${VERSION}/${PROJECT_HOST_DIR}/.android \
    ~/.docker-shipfast-demo/android-studio/${VERSION}/${PROJECT_HOST_DIR}/.gradle \
    ~/.docker-shipfast-demo/android-studio/${VERSION}/${PROJECT_HOST_DIR}/.java \
