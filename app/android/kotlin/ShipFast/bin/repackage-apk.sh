#!/bin/sh

set -eu

Main()
{

  ##############################################################################
  # DEFAULTS
  ##############################################################################

    local app_dir="${PWD}"
    local android_build_tools_path=${HOME}/.android-home/build-tools/29.0.3


  ##############################################################################
  # INPUT
  ##############################################################################

    for input in "${@}"; do
      case "${input}" in
        --ad | --app-dir )
          local app_dir="${2? Missing path to app dir.}"
          ;;

        --bp | --build-tools-path )
          local android_build_tools_path="${2? Missing Android build tools version.}"
          ;;

        --ks | --keystore )
          keystore_file="${2? Missing path to keystore file.}"
          ;;
      esac
    done


  ##############################################################################
  # VARS
  ##############################################################################

    local keystore_file="${app_dir}"/.local/approov.keystore.jks
    local decoded_app_dir="${app_dir}"/.local/apktool/decoded-apk
    local unpacked_build_dir="${decoded_app_dir}"/build
    local signed_repackaged_apk="${decoded_app_dir}/repackaged-and-signed.apk"
    local unaligned_apk="${decoded_app_dir}"/unaligned.apk
    local aligned_apk="${decoded_app_dir}"/aligned.apk


  ##############################################################################
  # EXECUTION
  ##############################################################################

    rm -rf "${unaligned_apk}" "${aligned_apk}" "${signed_repackaged_apk}" "${unpacked_build_dir}"

    mkdir -p "${decoded_app_dir}"

    apktool b -f "${app_dir}" -o "${unaligned_apk}"

    "${android_build_tools_path}"/zipalign -v -p 4 "${unaligned_apk}" "${aligned_apk}"
    "${android_build_tools_path}"/apksigner sign --ks "${keystore_file}" --out "${signed_repackaged_apk}" "${aligned_apk}"

    rm -rf "${unaligned_apk}" "${aligned_apk}" "${unpacked_build_dir}"

    printf "\n\nINSTALL THE APK IN YOUR DEVICE WITH:\n adb install ${signed_repackaged_apk}\n\n"
}

Main $@
