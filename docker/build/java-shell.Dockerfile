ARG TAG=11

FROM openjdk:${TAG}

# Will not prompt for questions
ENV DEBIAN_FRONTEND=noninteractive

ARG OH_MY_ZSH_THEME="amuse"
ARG CONTAINER_USER_NAME="developer"
ARG CONTAINER_UID=1000
ARG CONTAINER_GID=1000

ARG GRADLE_VERSION=5.2.1
ARG APKTOOL_VERSION=2.4.0
ARG BUILD_TOOLS_VERSION=29.0.3

ARG DOCKER_BUILD="/docker-build"
ARG SSL_CA_DIR="/etc/ssl/certs"
ARG DOCKER_BUILD_SCRIPTS_RELEASE="0.0.1.0"

ARG PROXY_CA_NAME="ProxyCA"
ENV PROXY_CA_PEM="${PROXY_CA_NAME}.pem"

ARG ROOT_CA_DIR=/.certificates
ARG ROOT_CA_NAME="Self_Signed_Root_CA"

ENV USER=${CONTAINER_USER_NAME}
ENV HOME=/home/"${CONTAINER_USER_NAME}"
ENV CONTAINER_HOME=/home/"${CONTAINER_USER_NAME}"

ENV \
  WORKSPACE_PATH="${CONTAINER_HOME}/workspace" \
  CONTAINER_USER_NAME="${CONTAINER_USER_NAME}" \
  CONTAINER_BIN_PATH="${CONTAINER_HOME}/bin" \
  CONTAINER_UID=${CONTAINER_UID} \
  CONTAINER_GID=${CONTAINER_GID} \
  ROOT_CA_KEY="${ROOT_CA_NAME}.key" \
  ROOT_CA_PEM="${ROOT_CA_NAME}.pem" \
  PROXY_CA_FILENAME="${PROXY_CA_NAME}.crt" \
  GRADLE_HOME=/opt/gradle/gradle-"${GRADLE_VERSION}" \
  PATH=/opt/gradle/gradle-"${GRADLE_VERSION}"/bin:${PATH}

COPY ./.certificates /.certificates

RUN apt update && \
    apt -y upgrade && \

    # Install Required Dependencies
    apt -y install \
        locales \
        tzdata \
        ca-certificates \
        inotify-tools \
        build-essential \
        libnss3-tools \
        lib32stdc++6 \
        zip \
        zsh \
        curl \
        git \
        nano \
        usbutils \
        default-jdk \
        maven \
        android-tools-adb \
        android-tools-fastboot && \

    # Force installation of missing dependencies
    apt -y -f install && \

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

    echo "en_GB.UTF-8 UTF-8" > /etc/locale.gen && \
    locale-gen en_GB.UTF-8 && \
    dpkg-reconfigure locales && \

    curl -o gradle.zip -fsSL https://services.gradle.org/distributions/gradle-"${GRADLE_VERSION}"-bin.zip && \
    unzip -d /opt/gradle gradle.zip && \
    rm -f gradle.zip && \
    gradle --version && \

    curl -fsSL -o /usr/local/bin/apktool https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool && \
    curl -fsSL -o /usr/local/bin/apktool.jar https://github.com/iBotPeaches/Apktool/releases/download/v"${APKTOOL_VERSION}"/apktool_"${APKTOOL_VERSION}".jar && \
    chmod +x /usr/local/bin/apktool* && \

    "${DOCKER_BUILD}"/scripts/utils/debian/add-user-with-bin-folder.sh \
      "${CONTAINER_USER_NAME}" \
      "${CONTAINER_UID}" \
      "/usr/bin/zsh" \
      "${CONTAINER_BIN_PATH}" && \

    "${DOCKER_BUILD}"/scripts/debian/install/oh-my-zsh.sh \
      "${CONTAINER_HOME}" \
      "${OH_MY_ZSH_THEME}" || true && \

    "${DOCKER_BUILD}"/scripts/utils/create-workspace-dir.sh \
      "${WORKSPACE_PATH}" \
      "${CONTAINER_USER_NAME}" && \

    # cleaning
    rm -rvf /var/lib/apt/lists/*

USER ${CONTAINER_USER_NAME}

WORKDIR ${CONTAINER_HOME}

ENV ANDROID_HOME="${CONTAINER_HOME}"/.android-home

ENV LANG=en_GB.UTF-8 \
    LANGUAGE=en_GB:en \
    LC_ALL=en_GB.UTF-8 \
    PATH=${ANDROID_HOME}/cmdline-tools/tools/bin:$PATH

RUN \
    mkdir -p "${ANDROID_HOME}"/cmdline-tools "${CONTAINER_USER_NAME}" && \
    chown -R "${CONTAINER_USER_NAME}":"${CONTAINER_USER_NAME}" "${ANDROID_HOME}" && \
    cd "${ANDROID_HOME}"/cmdline-tools && \
    curl -fsSL https://dl.google.com/android/repository/commandlinetools-linux-6609375_latest.zip -o tools.zip && \
    unzip tools.zip && \
    rm -rf tools.zip && \
    echo "y" | sdkmanager --install "build-tools;${BUILD_TOOLS_VERSION}"

WORKDIR ${CONTAINER_HOME}/workspace

CMD ["zsh"]
