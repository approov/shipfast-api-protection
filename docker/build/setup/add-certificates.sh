#!/bin/bash

set -eu

CA_DIR="${1?Missing certificate dir.}"
CA_PEM="${2?Missing certificate file name.}"
CA_NAME="${3?Missing certificate name.}"
CA_PEM_FILE="${CA_DIR}/${CA_PEM}"

/demo/ssl/add-proxy-certificate.sh /demo/ssl/certificates/ProxyCA.crt

if [ -f "${CA_PEM_FILE}" ]
    then
        /demo/ssl/add-certificate-to-android-studio.sh "${CA_PEM_FILE}"
        /demo/ssl/add-certificate-to-node-server.sh "${CA_PEM_FILE}"
        /demo/ssl/add-certificate-to-browser.sh "${CA_PEM_FILE}" "${CA_NAME}"
    else
        printf "\n >>> FATAL ERROR: Certificate not found in path ${CA_PEM_FILE} <<<\n"
fi
