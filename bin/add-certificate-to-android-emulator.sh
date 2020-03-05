#!/bin/bash

set -eu

CERTIFICATE_NAME="${1:?Missing certificate name}"
CERTIFICATE_FILE="${2:?Missing certificate file path to add to the certificate bundle file.}"

# Docker Container: /home/mobile/Android/Sdk/emulator/lib/ca-bundle.pem
CERTIFICATE_BUNDLE_FILE="${3:-/home/developer/Android/Sdk/emulator/lib/ca-bundle.pem}"

printf "\n>>> Adding Certificate to Android Emulator Bundle\n<<<"

printf "\n${CERTIFICATE_NAME}" >> "${CERTIFICATE_BUNDLE_FILE}"

printf "\n=========================\n" >> "${CERTIFICATE_BUNDLE_FILE}"

cat "${CERTIFICATE_FILE}" >> "${CERTIFICATE_BUNDLE_FILE}"

printf "\n<<< Certifcate Added <<<\n"


