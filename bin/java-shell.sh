#!/bin/sh

set -eu

Show_Help() {

  printf "
APK SHELL

apk [options] <command> [arguments]


COMMANDS:

create-keystore   Creates keystore for signing the Builds
                  $ ./apk create-keystore

docker-build      Builds the docker image for the APK Shell
                  $ ./apk docker-build

find-usb-path     Find the USB path by mobile device brand/name
                  $ ./apk find-usb-path Samsung

gradle            Builds the APK for each build type and product flavor
                  $ ./apk gradle build

install           Installs the APK for a product flavor via USB in the given mobile device
                  $ ./apk install Samsung
                  $ ./apk install Samsung api_key
                  $ ./apk install Samsung static_hmac
                  $ ./apk install Samsung dynamic_hmac
                  $ ./apk install Samsung certificate_pinning
                  $ ./apk install Samsung approov

list-usb          List all USB devices
                  $ ./apk list-usb

repackage         Repackages a previously unpacked APK
                  $ ./apk repackage

shell             Get a shell inside the docker container
                  $ ./apk shell
                  $ ./apk -u root shell

stop              Stops the running docker container
                  $ ./apk stop

unpack            Unpacks the APK to readable code
                  $ ./apk unpack

"
}

Trim() {
  local string="${1? Missing string to trim!}"
  local remove="${2:-" "}"

  # Left trim
  local string="${string#"${string%%[!${remove}]*}"}"

  # Right trim
  local string="${string%"${string##*[!${remove}]}"}"

  echo -n "${string}"
}

Apk_Repackage() {
  Docker_Run "./app/android/kotlin/ShipFast/bin/repackage-apk.sh app/android/kotlin/ShipFast"
}

Apk_Unpack() {
  Docker_Run "./app/android/kotlin/ShipFast/bin/unpack-apk.sh app/android/kotlin/ShipFast"
}

Adb_Install() {
  local USB_DEVICE_PATH="$(lsusb | grep -i "${device_brand}" - | head -1 | awk '{print "/dev/bus/usb/" $2 "/" $4}')"
  local USB_DEVICE_PATH="$(Trim "${USB_DEVICE_PATH}")"
  local RUN_MODE="--privileged"

  if [ -z "${USB_DEVICE_PATH}" ]; then
    printf "\nERROR: Unable to find any device named with brand '${device_brand}'\n\n"
    return 1
  fi

  local _apk_dir="app/android/kotlin/ShipFast/app/build/outputs/apk/${product_flavour}/release/app-${product_flavour}-release.apk"

  Docker_Run "adb install -r ${_apk_dir}"
}

Create_Keystore() {
  Docker_Run "./app/android/kotlin/ShipFast/bin/create-keystore.sh app/android/kotlin/ShipFast"
}

Docker_Build() {
  sudo docker build \
    --build-arg TAG=${TAG} \
    --build-arg BUILD_TOOLS_VERSION="${BUILD_TOOLS_VERSION}" \
    --build-arg CONTAINER_USER_NAME=${CONTAINER_USER_NAME} \
    --build-arg CONTAINER_UID=${CONTAINER_USER} \
    --build-arg CONTAINER_GID=${CONTAINER_USER} \
    --build-arg GRADLE_VERSION=${GRADLE_VERSION} \
    --tag ${IMAGE_NAME}:${TAG}_${GRADLE_VERSION}_${BUILD_TOOLS_VERSION} \
    --file "${DOCKER_PATH}/${DOCKER_FILENAME}" \
    "${DOCKER_PATH}"
}

Docker_Run() {
  local _usb_device_volume=""
  local _publish_port=""
  local _env_file=

  if [ -f .env ]; then
    _env_file="--env-file .env"
  fi

  if [ -n "${USB_DEVICE_PATH}" ]; then
    # e.g: --volume /dev/bus/usb/001/005:/dev/bus/usb/001/005:ro
    local _usb_device_volume="--volume ${USB_DEVICE_PATH}${USB_DEVICE_PATH}ro"
  fi

  if [ -n "${PORT_MAP}" ]; then
    _publish_port="--publish 127.0.0.1:${PORT_MAP}"
  fi

  adb kill-server  > /dev/null 2>&1

  sudo docker run \
    --rm \
    --name "${CONTAINER_NAME}" \
    --user ${CONTAINER_USER} \
    ${RUN_MODE} \
    ${BACKGROUND_MODE} \
    ${_env_file} \
    ${_publish_port} \
    ${_usb_device_volume} \
    --volume "${PWD}":"${CONTAINER_HOME}"/workspace \
    --volume "${GRADLE_HOST_DIR}":"${CONTAINER_HOME}"/.gradle \
    --volume "${MAVEN_HOST_DIR}":"${CONTAINER_HOME}"/.m2 \
    ${IMAGE_NAME}:${TAG}_${GRADLE_VERSION}_${BUILD_TOOLS_VERSION} \
    ${@}
}

Gradle() {

  local APPROOV_SDK_PATH=./app/android/kotlin/ShipFast/approov/approov.aar

  if [ ! -f "${APPROOV_SDK_PATH}" ]; then
      printf "\n---> ERRROR: Missing the APPROOV SDK ${APPROOV_SDK_PATH} <---\n"
      printf "\n> ADD THE APPROOV SDK: approov sdk -getLibrary ${APPROOV_SDK_PATH}\n"
      printf "\n> GET THE APPROOV SDK INITIAL CONFIG: approov sdk -getConfig approov-initial.config\n"
      printf "\n> Add the content of the file approov-initial.config to the var APPROOV_INITIAL_CONFIG in the .env file\n\n"
      exit 1
  fi

  Docker_Run "./app/android/kotlin/ShipFast/bin/apk-cli.sh --app-dir app/android/kotlin/ShipFast ${@}"

  printf "\n\nInstall an APK in your mobile device with the ./apk install command:\n\n"
  ./apk | grep -B 1 -i 'apk install' -
  echo
}

List_Usb() {
  local _result="$(lsusb | grep -i "${device_brand}" -)"

  if [ -z "${_result}" ]; then
    printf "\nNo devices found. His your device connected to the usb port?\n\n"
    exit 1
  fi

  printf "\nUSB DEVICE:\n${_result}\n"

  printf "\nUSB DEVICE PATH:\n"
  echo "${_result}" | awk '{print "/dev/bus/usb/" $2 "/" $4}'
  echo
}

Main() {

  ##############################################################################
  # DEFAULTS
  ##############################################################################

    local RUN_MODE=""
    local PORT_MAP=""
    local USB_DEVICE_PATH=""
    local CONTAINER_USER=$(id -u)
    local CONTAINER_NAME="shipfast-apk"
    local CONTAINER_USER_NAME=$(id -un)
    local CONTAINER_HOME="/home/${CONTAINER_USER_NAME}"
    local TAG=11 # from 11 onwards `apt` is missing
    local IMAGE_NAME=approov/java-shell
    local DOCKER_PATH=./docker/build
    local DOCKER_FILENAME=java-shell.Dockerfile
    local BACKGROUND_MODE="-it"
    local BUILD_TOOLS_VERSION=29.0.3
    local GRADLE_VERSION=5.2.1

    local GRADLE_HOST_DIR="${PWD}"/.local/.gradle
    local MAVEN_HOST_DIR="${PWD}/.local/maven/.m2"

    local PRODUCT_FLAVOUR="api_key"

    if [ -f .apk.local.vars ]; then
      . .apk.local.vars
    fi

    mkdir -p "${MAVEN_HOST_DIR}" "${GRADLE_HOST_DIR}"


  ##############################################################################
  # EXECUTION
  ##############################################################################

    for input in "${@}"; do
      case "${input}" in
        -d | --detached )
            local BACKGROUND_MODE="--detached"
            shift 1
            ;;

        -it )
          local BACKGROUND_MODE="-it"
          shift 1
        ;;

        --build-tools-version )
          local BUILD_TOOLS_VERSION="${2? Missing the build tools version!!!}"
          shift 2
        ;;

        --gradle-version )
          local GRADLE_VERSION="${2? Missing gradle version!!!}"
          shift 2
        ;;

        -p | --publish )
          local port_map=${2? Missing host port map for the container, eg: 8080:8080 !!!}
          shift 2
          ;;

        --privileged )
          local RUN_MODE="--privileged"
          shift 1
          ;;

        -t | --tag )
          local TAG="${2? Missing tag for docker image!!!}"
          shift 2
          ;;

        -u | --user )
          local CONTAINER_USER=${2? Missing user for container!!!}
          shift 2
          ;;

        create-keystore )
          Create_Keystore
          exit $?
          ;;

        docker-build )
          Docker_Build
          exit $?
          ;;

        find-usb-path )
          local device_brand="${2? Please provide brand of your mobile device. e.g: Samsung}"
          List_Usb
          exit $?
          ;;

        gradle )
          shift 1
          Gradle "${@}"
          exit $?
          ;;

        install )
          local device_brand="${2? Please provide brand of your mobile device. e.g: Samsung}"
          local product_flavour="${3:-${PRODUCT_FLAVOUR}}"

          Adb_Install
          exit $?
          ;;

        list-usb )
          local device_brand=":"
          List_Usb
          exit $?
          ;;

        repackage )
          Apk_Repackage
          exit $?
          ;;

        shell )
          Docker_Run
          exit $?
          ;;

        stop )
          sudo docker stop "${CONTAINER_NAME}"
          exit $?
          ;;

        unpack )
          Apk_Unpack
          exit $?
          ;;

        * )
          Show_Help
          exit $?
          ;;
      esac
    done

    Show_Help
}

Main $@
