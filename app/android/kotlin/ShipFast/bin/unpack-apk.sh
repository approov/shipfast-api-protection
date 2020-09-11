#!/bin/sh

set -eu

Main()
{
  local _app_dir="${1:-${PWD}}"

  apktool --force --output "${_app_dir}"/.local/apktool/decoded-apk decode "${_app_dir}"/app/build/outputs/apk/release/app-release.apk

  printf "\n\n---> APK decoded into: ${_app_dir}/.local/apktool/decoded-apk\n\n"
}

Main
