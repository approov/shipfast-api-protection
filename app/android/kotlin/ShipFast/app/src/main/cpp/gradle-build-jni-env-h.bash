#!/bin/bash

set -eu

Main() {
    local _path="./app/src/main/cpp"
    local _root_path=$(pwd)
    local _env_file="${_root_path}/../../../../.env"
    local _jni_env_file="${_path}/.jni.env.h"

    source "${_env_file}"

    printf "\nSTART CREATION OF: ${_jni_env_file}\n"

    cat <<- EOF > "${_jni_env_file}"
#ifndef SHIPFAST_DEMO_STAGE
#define SHIPFAST_DEMO_STAGE "${SHIPFAST_DEMO_STAGE? Missing env var: SHIPFAST_DEMO_STAGE}"
#endif

#ifndef SHIPFAST_API_KEY
#define SHIPFAST_API_KEY "${SHIPFAST_API_KEY? Missing env var: SHIPFAST_API_KEY}"
#endif

#ifndef SHIPFAST_API_BASE_URL
#define SHIPFAST_API_BASE_URL "${SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL? Missing env var: SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL}://${SHIPFAST_PUBLIC_DOMAIN? Missing env var: SHIPFAST_PUBLIC_DOMAIN}"
#endif

#ifndef DRIVER_LATITUDE
#define DRIVER_LATITUDE "${DRIVER_LATITUDE? Missing env var: DRIVER_LATITUDE}"
#endif

#ifndef DRIVER_LONGITUDE
#define DRIVER_LONGITUDE "${DRIVER_LONGITUDE? Missing env var: DRIVER_LONGITUDE}"
#endif

#ifndef AUTH0_CLIENT_ID
#define AUTH0_CLIENT_ID "${AUTH0_CLIENT_ID? Missing env var: AUTH0_CLIENT_ID}"
#endif
EOF

    printf "\nEND CREATION OF: ${_jni_env_file}\n\n"
}

Main "${@}"
