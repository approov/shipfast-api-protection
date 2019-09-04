#!/bin/bash

set -eu

CA_PEM_FILE="${1?Missing name for certificate file}"
CA_EXTENSION="${CA_PEM_FILE##*.}"

if [ "${CA_EXTENSION}" != "pem" ]
    then
        printf "\nFATAL ERROR: Certificate must use .pem extension\n\n"
        exit 1
fi

if [ -f "${CA_PEM_FILE}" ]
    then
        printf "\n>>> ADDING A CERTIFICATE TO NODE SERVER <<<\n"

        # Add certificate to node, so that we can use npm install
        printf "cafile=${CA_PEM_FILE}" >> /root/.npmrc
        printf "cafile=${CA_PEM_FILE}" >> /home/${CONTAINER_USER}/.npmrc;

        printf "\n >>> CERTICATE ADDED SUCCESEFULY<<<\n"

    else
        printf "\n >>> NO CERTIFICATE TO ADD <<<\n"
fi

