#!/bin/bash

set -eu

Main() {

    local CERTIFICATE_NAME="${1:?Missing certificate name, e.g: MyCertName}"
    local CERTIFICATE_FILE="${2:?Missing path to the certificate.}"

    # Docker Container: /home/mobile/Android/Sdk/emulator/lib/ca-bundle.pem
    local CERTIFICATE_BUNDLE_FILE="${3:-/home/developer/Android/Sdk/emulator/lib/ca-bundle.pem}"

    local COUNT_LINES=$(cat ${CERTIFICATE_FILE} | wc -l)

    printf "\n>>> Adding Certificate to Android Emulator Bundle <<<\n"

    printf "\n${CERTIFICATE_NAME}" >> "${CERTIFICATE_BUNDLE_FILE}"

    printf "\n=========================\n" >> "${CERTIFICATE_BUNDLE_FILE}"

    cat "${CERTIFICATE_FILE}" >> "${CERTIFICATE_BUNDLE_FILE}"

    printf "\n\nAdded Certifcate:\n\n"

    cat "${CERTIFICATE_BUNDLE_FILE}" | grep -i -A ${COUNT_LINES} "${CERTIFICATE_NAME}"
}

Main ${@}
