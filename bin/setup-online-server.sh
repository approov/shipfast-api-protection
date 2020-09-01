#!/bin/sh

set -eu

Setup_Depedencies() {
    apt update

    apt -y install \
        apt-transport-https \
        ca-certificates \
        curl \
        gnupg2 \
        software-properties-common
}

Setup_Docker() {

    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo apt-key add -

    apt-key fingerprint 0EBFCD88

    add-apt-repository \
       "deb [arch=amd64] https://download.docker.com/linux/debian \
       $(lsb_release -cs) \
       stable"

    apt update

    apt -y install docker-ce docker-ce-cli containerd.io
}

Setup_Docker_Compose() {

    local _download_url="https://github.com/docker/compose/releases/download/1.25.4/docker-compose-$(uname -s)-$(uname -m)"

    curl -L "${_download_url}" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
}

Setup_Traefik() {

    cd ./traefik

    # Traefik will not create the certificates if we don't fix the permissions
    #  for the file where it stores the LetsEncrypt certificates.
    chmod 600 acme.json

    # Creates a docker network that will be used by Traefik to proxy the requests to the docker containers:
    docker network create traefik

    docker-compose up -d traefik
}

Main() {
    Setup_Depedencies
    Setup_Docker
    Setup_Docker_Compose
    Setup_Traefik

    docker version
    echo
    docker-compose --version
    echo
    git version
    echo

    printf "\n## Restart Traefik:\n"
    printf "sudo docker-compose restart traefik\n"

    printf "\n## Start Traefik:\n"
    printf "sudo docker-compose up -d traefik\n"

    printf "\n## Destroy Traefik:\n"
    printf "sudo docker-compose down\n"

    printf "\n## Tailing the Traefik logs in realtime:"
    printf "\nsudo docker-compose logs --follow traefik\n\n"

    sudo docker-compose logs traefik

    cd -

    printf "\n---> TRAEFIK is now listening for new docker containers <---\n\n"
}

Main ${@}
