FROM ubuntu:18.04

ARG OH_MY_ZSH_THEME="amuse"
ARG CONTAINER_USER_NAME="developer"
ARG CONTAINER_UID=1000
ARG CONTAINER_GID=1000

# Inspired on:
#   → https://github.com/Purik/android-studio-docker
#   → https://github.com/Deadolus/android-studio-docker

# Removes DBUS warning that should be only seen by Developer
# https://bugs.launchpad.net/ubuntu/+source/at-spi2-core/+bug/1193236
ENV NO_AT_BRIDGE=1

# Will not prompt for questions
ENV DEBIAN_FRONTEND=noninteractive

ARG DOCKER_BUILD="/docker-build"
ARG SSL_CA_DIR="/etc/ssl/certs"
ARG DOCKER_BUILD_SCRIPTS_RELEASE="0.0.1.0"

ARG PROXY_CA_NAME="ProxyCA"
ENV PROXY_CA_PEM="${PROXY_CA_NAME}.pem"

ARG ANDROID_STUDIO_VERSION
ARG ANDROID_STUDIO_BUILD
ARG ANDROID_STUDIO_DOWNLOAD_URL="https://dl.google.com/dl/android/studio/ide-zips/${ANDROID_STUDIO_VERSION}/android-studio-ide-${ANDROID_STUDIO_BUILD}-linux.tar.gz"

ARG DOMAIN_CA_NAME="localhost"

ARG ROOT_CA_DIR=/.certificates
ARG ROOT_CA_NAME="Self_Signed_Root_CA"

ENV CONTAINER_HOME=/home/"${CONTAINER_USER_NAME}"

ENV \
  WORKSPACE_PATH="${CONTAINER_HOME}/workspace" \
  CONTAINER_USER_NAME="${CONTAINER_USER_NAME}" \
  CONTAINER_BIN_PATH="${CONTAINER_HOME}/bin" \
  CONTAINER_UID=${CONTAINER_UID} \
  CONTAINER_GID=${CONTAINER_GID} \
  ROOT_CA_KEY="${ROOT_CA_NAME}.key" \
  ROOT_CA_PEM="${ROOT_CA_NAME}.pem" \
  PROXY_CA_FILENAME="${PROXY_CA_NAME}.crt"

COPY ./.certificates /.certificates

RUN dpkg --add-architecture i386 && \
    apt update && \
    apt -y upgrade && \
    apt -y install \
      locales \
      tzdata \
      ca-certificates \
      inotify-tools \
      libnss3-tools \
      curl \
      wget \
      apt-utils \
      git \
      zsh \
      unzip \
      xorg \
      nano \
      ant \
      firefox \
      default-jdk \
      build-essential \
      dbus* \
      libcanberra-gtk* \
      libz1 \
      lib32z1 \
      libc6:i386 \
      libncurses5 \
      libncurses5:i386 \
      libstdc++6 \
      libstdc++6:i386 \
      lib32stdc++6 \
      libbz2-1.0 \
      libbz2-1.0:i386 \
      libxft2 \
      libxi6 \
      libxtst6 \
      libxrender1 \
      libfreetype6 \
      qemu-kvm \
      libvirt-bin \
      ubuntu-vm-builder \
      bridge-utils \
      libnotify4 \
      libglu1 \
      libqt5widgets5 \
      xvfb \
      android-tools-adb \
      android-tools-fastboot && \

  mkdir -p "${DOCKER_BUILD}" && \

  curl \
    -fsSl \
    -o archive.tar.gz \
    https://gitlab.com/exadra37-bash/docker/bash-scripts-for-docker-builds/-/archive/"${DOCKER_BUILD_SCRIPTS_RELEASE}"/bash-scripts-for-docker-builds-dev.tar.gz?path=scripts && \
  tar xf archive.tar.gz -C "${DOCKER_BUILD}" --strip 1 && \
  rm -vf archive.tar.gz && \

  "${DOCKER_BUILD}"/scripts/debian/install/inotify-tools.sh && \

  if [ -f "/.certificates/ProxyCA.crt" ]; then \
    "${DOCKER_BUILD}"/scripts/custom-ssl/operating-system/add-custom-authority-certificate.sh \
      "/.certificates/ProxyCA.crt" \
      "/usr/local/share/ca-certificates"; \
  fi && \

 curl "${ANDROID_STUDIO_DOWNLOAD_URL}" > /tmp/studio.tar.gz && \
    tar xf /tmp/studio.tar.gz -C /opt && \
    rm /tmp/studio.tar.gz && \
    if [ -f "/.certificates/ProxyCA.crt" ]; then \
      "${DOCKER_BUILD}"/scripts/custom-ssl/android/add-certificate-to-android-studio.sh \
        "${SSL_CA_DIR}/${PROXY_CA_PEM}"; fi && \

  locale-gen en_GB.UTF-8 && dpkg-reconfigure locales && \

  "${DOCKER_BUILD}"/scripts/utils/debian/add-user-with-bin-folder.sh \
    "${CONTAINER_USER_NAME}" \
    "${CONTAINER_UID}" \
    "/usr/bin/zsh" \
    "${CONTAINER_BIN_PATH}" && \

  "${DOCKER_BUILD}"/scripts/debian/install/oh-my-zsh.sh \
    "${CONTAINER_HOME}" \
    "${OH_MY_ZSH_THEME}" && \

  "${DOCKER_BUILD}"/scripts/utils/create-workspace-dir.sh \
    "${WORKSPACE_PATH}" \
    "${CONTAINER_USER_NAME}" && \

  echo PATH=/opt/android-studio/bin:$PATH >> ${CONTAINER_HOME}/.bashrc && \
  echo PATH=/opt/android-studio/bin:$PATH >> ${CONTAINER_HOME}/.zshrc && \

  adduser ${CONTAINER_USER_NAME} libvirt && \
  adduser ${CONTAINER_USER_NAME} kvm && \

  apt -y -f install && \
  apt -y autoremove && \
  apt-get clean && \
  apt-get purge

ENV ANDROID_EMULATOR_USE_SYSTEM_LIBS=1

ARG DISPLAY=${DISPLAY:-":0"}
ENV DISPLAY=${DISPLAY}

ENV LANG en_GB.UTF-8
ENV LANGUAGE en_GB:en
ENV LC_ALL en_GB.UTF-8

USER ${CONTAINER_USER_NAME}

WORKDIR ${CONTAINER_HOME}/workspace

CMD ["/opt/android-studio/bin/studio.sh"]
