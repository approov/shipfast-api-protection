#!/bin/bash

set -eu

CA_PEM_FILE="${1?Missing path to certificate file}"

if [ -f "${CA_PEM_FILE}" ]
    then
        printf "\n>>> ADDING A CERTIFICATE TO ANDROID STUDIO <<<\n"

        # https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000094584-IDEA-Ultimate-2016-3-4-throwing-unable-to-find-valid-certification-path-to-requested-target-when-trying-to-refresh-gradle
        cd /opt/android-studio/jre/jre/lib/security && \
        printf "changeit\nyes\n" | keytool -keystore cacerts -importcert -alias ProxyCertificate -file "${CA_PEM_FILE}" && \

        printf "\n >>> CERTICATE ADDED SUCCESEFULY<<<\n"

    else
        printf "\n >>> FATAL ERROR: Certificate not found in path ${CA_PEM_FILE} <<<\n"
fi

