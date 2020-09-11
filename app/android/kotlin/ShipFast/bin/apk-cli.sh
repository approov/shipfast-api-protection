#!/bin/sh

set -eu

Show_Help() {
  printf "
APK BASH CLI

./apk-cli.sh [options] <command> [arguments]


COMMANDS:

build           Builds and tests an APK release with pre validations.
                $ ./apk-cli.sh build
                $ ./apk-cli.sh build --app-dir app/android/kotlin/ShipFast

assembleDebug   Assembles an APK release with pre validations.
                $ ./apk-cli.sh assembleDebug
                $ ./apk-cli.sh assembleDebug --app-dir app/android/kotlin/ShipFast

"
}

Check_Required() {
    # Are we in the APP dir?
    if [ ! -d ./app/src/main ]; then
      printf "
  FATAL ERROR:

    * Not running from the expected repo app dir: app/android/kotlin/ShipFast
    * Given argument for app dir: ${app_dir}

  Run the script from the repo root:
  $ ./app/android/kotlin/ShipFast/bin/build-release.sh app/android/kotlin/ShipFast

  Run the script from the app dir:
  $ cd app/android/kotlin/ShipFast
  $ ./bin/build-release.sh

  "
      exit 1
    fi

    if [ ! -f .local/approov.keystore.jks ]; then
      printf "\nFATAL ERROR: Missing file -> ${app_dir}/.local/approov.keystore.jks\n\n"
      exit 1
    fi

    if [ ! -f local.properties ]; then
      printf "\nFATAL ERROR: Missing file -> ${app_dir}/local.properties\n\n"
      exit 1
    fi

    if grep -i YOUR_PASSWORD_HERE local.properties; then
      printf "\nFATAL ERROR: Please replace YOUR_PASSWORD_HERE with your password for the Android Keystore at ${app_dir}/local.properties\n\n"
      exit 1
    fi

    # Can we source the .env file?
    if [ -f "${app_dir}"/.env ]; then
      . "${app_dir}"/.env
    elif [ -f ./../../../../.env ]; then
      . ./../../../../.env
    else
      printf "\nFATAL ERROR: .env file not found at the root of the repo.\n\n"
      exit 1
    fi
}

Clean_Cache() {
  # The cache here can also cause problems during a build
  rm -rf \
    ~/.gradle/caches \
    "${app_dir}"/.gradle \
    "${app_dir}"/app/.externalNativeBuild \
    "${app_dir}"/app/android \
    "${app_dir}"/app/build \
    "${app_dir}"/app/.cxx \
    "${app_dir}"/approov/build
}

Gradle_Run() {
  ./gradlew ${@}
}

Main()
{
  ##############################################################################
  # DEFAULTS
  ##############################################################################

    local app_dir="${PWD}"


  ##############################################################################
  # INPUT
  ##############################################################################

  for input in "${@}"; do
    case "${input}" in
      --ad | --app-dir )
        local app_dir="${2? Missing path to app dir.}"
        shift 2
        ;;

      -h | --help )
        Show_Help
        exit 0
        ;;

      build | assembleDebug )
        cd "${app_dir}"
        Check_Required
        Clean_Cache
        Gradle_Run ${@}
        printf "\n\nAPKs built for all you product flavors and build types at:\n"
        printf "app/android/kotlin/ShipFast/app/build/outputs/apk/{product_flavor}/{build_type}\n\n"
        exit $?
        ;;

      * )
        cd "${app_dir}"
        Gradle_Run ${@}
        exit $?
        ;;
    esac
  done

  Show_Help
}

Main ${@}
