#!/bin/bash

set -eu

###
# https://thomas-leister.de/en/how-to-import-ca-root-certificate/
###


### Script installs root.cert.pem to certificate trust store of applications using NSS
### (e.g. Firefox, Thunderbird, Chromium)
### Mozilla uses cert8, Chromium and Chrome use cert9

###
### Requirement: apt install libnss3-tools
###

CA_PEM="${1?Missing file path for the PEM certificate}"
CA_NAME="${2?Missing Certificate Name}"
BROWSER_CONFIG_DIR="${3:-/home/mobile}"

printf "\n>>> ADDING CERTIFICATE TO BROWSERS TRUSTED STORE <<<\n"

if [ -f "${CA_PEM}" ]
    then
        printf "\n--> CERTIFICATE FILE: ${CA_PEM}\n"
        printf "\n--> CERTIFICATE NAME: ${CA_NAME}\n"
        printf "\n--> BROWSER CONFIG DIR: ${BROWSER_CONFIG_DIR}\n"

        ###
        ### For cert8 (legacy - DBM)
        ###
        for certDB in $(find ${BROWSER_CONFIG_DIR} -name "cert8.db")
        do
            certdir=$(dirname ${certDB});
            certutil -A -n "${CA_NAME}" -t "TCu,Cu,Tu" -i ${CA_PEM} -d dbm:${certdir}
        done

        ###
        ### For cert9 (SQL)
        ###
        for certDB in $(find ${BROWSER_CONFIG_DIR} -name "cert9.db")
        do
            certdir=$(dirname ${certDB});
            certutil -A -n "${CA_NAME}" -t "TCu,Cu,Tu" -i ${CA_PEM} -d sql:${certdir}
        done
    else
        printf "\n>>> CERTIFICATE FILE NOT FOUND FOR: ${CA_PEM}\n"
fi
