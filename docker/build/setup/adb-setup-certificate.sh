#!/bin/bash

# https://stackoverflow.com/a/48814971/6454622

set -eu

CA_PEM=${1?Missing certificate file name}

cert_name=$(openssl x509 -inform PEM -subject_hash_old -in ${CA_PEM} | head -1)
cat ${CA_PEM} > $cert_name
openssl x509 -inform PEM -text -in ${CA_PEM} -out nul >> $cert_name

adb shell mount -o rw,remount,rw /system
adb push $cert_name /system/etc/security/cacerts/
adb shell mount -o ro,remount,ro /system
