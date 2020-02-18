#!/bin/bash

set -eu

PROJECT_NAME=${1? Missing Project name.}
PROJECT_HOST_DIR=${2? Missing Project host dir.}
VERSION=${3? Missing Android studio version.}

mkdir -v -p \
    "${PROJECT_HOST_DIR}"/android-studio/${VERSION}/"${PROJECT_NAME}"/Android \
    "${PROJECT_HOST_DIR}"/android-studio/${VERSION}/"${PROJECT_NAME}"/.AndroidStudio${VERSION} \
    "${PROJECT_HOST_DIR}"/android-studio/${VERSION}/"${PROJECT_NAME}"/.android \
    "${PROJECT_HOST_DIR}"/android-studio/${VERSION}/"${PROJECT_NAME}"/.gradle \
    "${PROJECT_HOST_DIR}"/android-studio/${VERSION}/"${PROJECT_NAME}"/.java \
