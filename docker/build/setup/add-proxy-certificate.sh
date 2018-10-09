#!/bin/bash

set -eu

PROXY_CA_PEM="${1?Missing name for Proxy CRT file}"

if [ -f "${PROXY_CA_PEM}" ]
    then
        printf "\n>>> ADDING A PROXY CERTIFICATE TO THE TRUSTED STORE <<<\n"

        # add certificate tpo the trust store
        cp -v ${PROXY_CA_PEM} /usr/local/share/ca-certificates
        update-ca-certificates

        # verifies the certificate
        openssl x509 -in ${PROXY_CA_PEM} -text -noout > "${PROXY_CA_PEM}.txt"

    else
        printf "\n >>> FATAL ERROR: Certificate not found in path ${PROXY_CA_PEM} <<<\n"
fi
