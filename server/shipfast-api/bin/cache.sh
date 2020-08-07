#!/bin/sh

set -eu

Call_API() {
  local _api_version=${1? Missing the API version to delete the cache for.}
  local _authorization_token=${2? Missing the Authorization token to delete the cache for.}

  # Add an empty line
  echo

  local result=$(
    curl \
      -i \
      --location \
      --request POST "${SHIPFAST_PUBLIC_DOMAIN}/admin/${_api_version}/cache/delete" \
      --header "CLI-API-KEY: ${SHIPFAST_CLI_API_KEY}" \
      --header "Authorization: Bearer ${_authorization_token}" \
    )

  printf "\n${result}\n"
}

Main() {

  if [ -f ./.env ]; then
    . ./.env
  fi

  for input in "${@}"; do
    case "${input}" in
      delete )
        shift 1
        Call_API "${@}"
        exit $?
        ;;

      * )
        printf "\nUnknown Cache command: ${input}\n"
        exit $?
        ;;
    esac
  done

}

Main "${@}"
