# SHIPFAST DEMO ON DOCKER

The blog post about Shipfast demo can be found [here](https://blog.approov.io/tag/a-series-shipfast).


# SETUP

## Requirements

Your computer needs to have installed:

* Docker
* Docker Compose `>= 2.1`
* Git


## Clone Repository

```bash
git clone git@github.com:approov/shipfast-api-protection.git && cd shipfast-api-protection
```

## Approov CLI Tool

To use the Shipfast demo we need to install the [Approov CLI tool](https://approov.io/docs/v2.0/approov-cli-tool-reference/) by following the [installation documentation](https://approov.io/docs/v2.0/approov-installation/).

> **ALERT:** Do not forget to set the Approov development token in your environment, as per instructions on the docs.


## Environment

Let's use the `.env.example` file as starting point for our `.env` file:

```bash
cp server/.env.example server/.env
```
and

```bash
cp .env.example .env
```

Now we need to add values for:

* APPROOV_TOKEN_SECRET - follow the [Appoov docs](https://approov.io/docs/v2.0/approov-usage-documentation/#token-secret-extraction) to get the Appoov secret.
* AUTH0_DOMAIN - you can get one from your [Auth0 Dashboard](https://manage.auth0.com/dashboard)

All other defaults in the`.env` file are fine to run the demo.


## Build the Docker Stack

Build docker image:

```bash
./shipfast build shipfast-demo
```

## Run the Demo

Start demo:

```bash
./shipfast start
```

### Android Studio

* Disable Instant Run on Android studio.
* Create a mobile device in the emulator.
* Start the app in the emulator mobile device:
    + In the editor terminal, use the bash script to add the Proxy certificate to the emulator.
    + Enable high accuracy location in the emulator mobile device settings.
    + Add manually the London geo location to the emulator Location settings.


## Stack Commands

The bash script `./shipfast` is a wrapper around `sudo docker-compose`, thus any
docker compose command will work, but for the developer convenience it includes
some short-cuts commands that will perform all heavy lifting for us.

To start the Shipfast demo:

```bash
./shipfast start
```

To restart the Shipfast demo:

```bash
./shipfast restart
```

To stop the Shipfast demo:

```bash
./shipfast stop
```

To register the Android APK with the Approov Cloud Service:

```bash
./shipfast register apk
```
