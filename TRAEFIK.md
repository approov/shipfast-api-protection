# TRAEFIK

Using [Treafik](https://docs.traefik.io/) as reverse proxy on port `80` and `443` for all docker containers running in the same host machine.

Just add a docker container with the correct Traefik labels, and once you start it, Traefik will take care of creating a LetsEncrypt certificate for the domain you specified in the label, and will take care of renewing it when the time comes.

> **NOTE**: To run Traefik with LetsEncrypt support you cannot be in your own computer, you will need to deploy it in an online server.


## INSTALL

Copy the `traefik` folder:

```
cp -r traefik /opt/traefik
```

Move into the install location:

```
cd /opt/traefik
```

Create the external docker network that will be used by Traefik to proxy the requests to the docker containers:

```
docker create network traefik
```

## RUN TRAEFIK

Starting the Traefik service:

```
docker-compose up -d traefik
```

Tailing the logs:

```
docker-compose logs -f traefik
```

Now just go back to the root of the Shipfast project and follow the [RUN_ON_DOCKER.md][/RUN_ON_DOCKER.md] to start the backends and see Traefik handling all for you.
