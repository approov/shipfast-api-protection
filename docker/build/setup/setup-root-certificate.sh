#!/bin/bash

set -eu

###
# inspired https://fabianlee.org/2018/02/17/ubuntu-creating-a-trusted-ca-and-san-certificate-using-openssl-on-ubuntu/
###


ROOT_CA_KEY="${1?Missing Name for root certificate KEY file}"
ROOT_CA_PEM="${2?Missing Name for root certificate PEM file}"
ROOT_CA_NAME="${3?Missing Certificate Name}"
CONFIG_FILE="${4:-openssl.cnf}"

if [ ! -f ROOT_CA_PEM ]
    then
        printf "\n>>> CREATING A ROOT CERTIFICATE <<<\n"

        openssl req \
            -new \
            -newkey rsa:4096 \
            -days 3650 \
            -nodes \
            -x509 \
            -extensions v3_ca \
            -subj "/C=US/ST=CA/L=SF/O=${ROOT_CA_NAME}/CN=${ROOT_CA_NAME}" \
            -keyout ${ROOT_CA_KEY} \
            -out ${ROOT_CA_PEM} \
            -config ${CONFIG_FILE}

        printf "\n>>> ADDING ROOT CERTIFICATE TO THE TRUSTED STORE <<<\n"

        # add certificate to the trust store
        cp ${ROOT_CA_PEM} /usr/local/share/ca-certificates/self-signed-root-ca.crt
        update-ca-certificates

        # verifies the certificate
        openssl x509 -in ${ROOT_CA_PEM} -text -noout > "${ROOT_CA_NAME}.txt"

        printf "\n >>> ROOT CERTICATE CREATED SUCCESEFULY<<<\n"

    else
        printf "\n >>> ROOT CERTICATE ALREADY EXISTS <<<\n"
fi
