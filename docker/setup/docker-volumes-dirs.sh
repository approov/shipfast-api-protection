#!/bin/bash

set -eu

PROJECT_HOST_DIR=${1?}

mkdir -v -p \
    ~/.docker-android-studio/${PROJECT_HOST_DIR}/Android \
    ~/.docker-android-studio/${PROJECT_HOST_DIR}/.AndroidStudio3.2 \
    ~/.docker-android-studio/${PROJECT_HOST_DIR}/.android \
    ~/.docker-android-studio/${PROJECT_HOST_DIR}/.gradle \
    ~/.docker-android-studio/${PROJECT_HOST_DIR}/.java \
