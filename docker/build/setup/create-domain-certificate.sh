#!/bin/bash

set -eu

###
# inspired https://fabianlee.org/2018/02/17/ubuntu-creating-a-trusted-ca-and-san-certificate-using-openssl-on-ubuntu/
###


DOMAIN="${1:-example.com}"
ROOT_CA_KEY="${2?Missing Name for root certificate KEY file}"
ROOT_CA_PEM="${3?Missing Name for root certificate PEM file}"

DOMAIN_CA_KEY="${DOMAIN}.key"
DOMAIN_CA_CSR="${DOMAIN}.csr"
DOMAIN_CA_CRT="${DOMAIN}.crt"
DOMAIN_CA_TXT="${DOMAIN}.txt"
CONFIG_FILE="${DOMAIN}.cnf"


printf "\n>>> MERGINGING CONFIGURATION FROM ${DOMAIN_CA_TXT} INTO ${CONFIG_FILE} <<<\n"
cat openssl.cnf ${DOMAIN_CA_TXT} > ${CONFIG_FILE}


printf "\n>>> GENERATING KEY FOR DOMAIN CERTIFICATE: ${DOMAIN_CA_KEY} <<<\n"

# generate the private/public RSA key pair for the domain
openssl genrsa -out ${DOMAIN_CA_KEY} 4096

printf "\n>>> GENERATING CSR FOR DOMAIN CERTIFICATE: ${DOMAIN_CA_CSR} <<<\n"

# create the server certificate signing request:
openssl req \
    -subj "/CN=${DOMAIN}" \
    -extensions v3_req \
    -sha256 \
    -new \
    -key ${DOMAIN_CA_KEY} \
    -out ${DOMAIN_CA_CSR}

printf "\n>>> GENERATING CRT FOR DOMAIN CERTIFICATE: ${DOMAIN_CA_CRT} <<<\n"

# generate the server certificate using the: server signing request, the CA signing key, and CA cert.
openssl x509 \
            -req \
            -extensions v3_req \
            -days 3650 \
            -sha256 \
            -in ${DOMAIN_CA_CSR} \
            -CA ${ROOT_CA_PEM} \
            -CAkey ${ROOT_CA_KEY} \
            -CAcreateserial \
            -out ${DOMAIN_CA_CRT} \
            -extfile ${CONFIG_FILE}

# verifies the certificate
openssl x509 -in ${DOMAIN_CA_CRT} -text -noout > ${DOMAIN}.txt

printf "\n >>> CERTIFICATE CREATED FOR DOMAIN: ${DOMAIN} <<<\n"
