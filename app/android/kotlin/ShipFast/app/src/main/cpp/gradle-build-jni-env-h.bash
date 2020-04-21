#!/bin/bash

# Copyright (C) 2020 CriticalBlue Ltd.
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of
# this software and associated documentation files (the "Software"), to deal in
# the Software without restriction, including without limitation the rights to
# use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
# the Software, and to permit persons to whom the Software is furnished to do so,
# subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
# FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
# COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
# IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
# CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

set -eu

Main() {
    local _path="./app/src/main/cpp"
    local _root_path=$(pwd)
    local _env_file="${_root_path}/../../../../.env"
    local _jni_env_file="${_path}/.jni.env.h"

    source "${_env_file}"

    local _http_protocol="${SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL? Missing env var: SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL}"
    local _url="${SHIPFAST_PUBLIC_DOMAIN? Missing env var: SHIPFAST_PUBLIC_DOMAIN}"
    local _api_version="${SHIPFAST_API_VERSION? Missing env var: SHIPFAST_API_VERSION}"

    printf "\nSTART CREATION OF: ${_jni_env_file}\n"

    cat <<- EOF > "${_jni_env_file}"
#ifndef SHIPFAST_DEMO_STAGE
#define SHIPFAST_DEMO_STAGE "${SHIPFAST_DEMO_STAGE? Missing env var: SHIPFAST_DEMO_STAGE}"
#endif

#ifndef SHIPFAST_API_KEY
#define SHIPFAST_API_KEY "${SHIPFAST_API_KEY? Missing env var: SHIPFAST_API_KEY}"
#endif

#ifndef SHIPFAST_API_HMAC_SECRET
#define SHIPFAST_API_HMAC_SECRET "${SHIPFAST_API_HMAC_SECRET? Missing env var: SHIPFAST_API_HMAC_SECRET}"
#endif

#ifndef SHIPFAST_API_BASE_URL
#define SHIPFAST_API_BASE_URL "${_http_protocol}://${_url}/${_api_version}"
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
