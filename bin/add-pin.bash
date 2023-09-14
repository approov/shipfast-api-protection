#!/bin/bash

set -eu

# INVOKE:
#  ./bin/add-pin.bash <new-pin> <domain> <approov-management-token>
#
# ./bin/add-pin.bash \
#   qXHiE7hFX2Kj4ZCtnr8u8yffl8w9CTv6kE0U5j0o1BB= \
#   fake-mitm-attack.shipfast-api.example.com \
#   ~/tokens/administration.tok
#
Main() {

    local NEW_PIN=${1? Missing the new pin to add.}
    local DOMAIN=${2? Missing Domain to add the new pin.}
    local APPROOV_MANAGEMENT_TOKEN_PATH=${3? Missing path to the Approov management token.}

    local CURRENT_PINS_JSON_FILE="current_pins.json"
    local NEW_PINS_JSON_FILE="new_pins.json"

    approov api "${APPROOV_MANAGEMENT_TOKEN_PATH}" -getAll "${CURRENT_PINS_JSON_FILE}"

    printf "\nCURRENT PINS:\n"
    cat "${CURRENT_PINS_JSON_FILE}"
    echo

    node > "${NEW_PINS_JSON_FILE}" <<EOF
    //Read data
    var data = require('./${CURRENT_PINS_JSON_FILE}');

    data["${DOMAIN}"].push("${NEW_PIN}");

    //Output data
    console.log(JSON.stringify(data, null, 4));
EOF

    printf "\nNEW PINS:\n"
    cat "${NEW_PINS_JSON_FILE}"
    echo

    approov api "${APPROOV_MANAGEMENT_TOKEN_PATH}" -setAll "${NEW_PINS_JSON_FILE}"
}

Main ${@}
