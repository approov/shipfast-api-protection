#!/bin/sh

set -eu

Add_Local_Property()
{
  local property="${1? Missing property name.}"
  local value="${2? Missing property value}"

  if ! grep -q "${property}" ./local.properties; then
    printf "\n${property}=${value}" >> ./local.properties
  fi
}

Main()
{
  local app_dir="${1:-${PWD}}"
  local keystore_path=.local/approov.keystore.jks

  cd "${app_dir}"
  mkdir -p .local

  if [ -z "${ANDROID_HOME}" ]; then
    printf "\n\nERROR: Variable ANDROID_HOME is not set on your environment.\n"
    exit 1
  fi

  if [ ! -f "${keystore_path}" ]; then
    keytool \
      -v \
      -genkey \
      -keystore "${keystore_path}" \
      -alias approov \
      -keyalg RSA \
      -keysize 2048 \
      -validity 10000

  else
    printf "\n---> The keystore already exists at: ${keystore_path}\n"
  fi

  printf "\n---> Adding, if not already present, properties to your ${app_dir}/local.properties file\n"

  # to avoid the error "grep: ./local.properties: No such file or directory"
  touch ./local.properties

  local line="$(grep -i ndkVersion app/build.gradle)"
  local version="${line#*\"}"
  local ndk_version="${version%*\"}"

  if [ -z "${ndk_version}" ]; then
    printf "\nFATAL ERROR: Not able to parse the ndkVersion from $PWD/app/build.gradle\n\n"
    exit 1
  fi

  Add_Local_Property "sdk.dir" "${ANDROID_HOME}/Sdk"
  Add_Local_Property "ndk.dir" "${ANDROID_HOME}/ndk/${ndk_version}"
  Add_Local_Property "android.keystore.path" "../.local/approov.keystore.jks"
  Add_Local_Property "android.private.key.alias" "approov"
  Add_Local_Property "android.keystore.password" "YOUR_PASSWORD_HERE"
  Add_Local_Property "android.private.key.password" "YOUR_PASSWORD_HERE"

  printf "\n---> Edit your ${app_dir}/local.properties file and add the password you have used when you first created the keystore.\n\n"
}

Main $@
