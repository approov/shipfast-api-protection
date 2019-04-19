# SHIPFAST DEMO ON DOCKER

The blog post about Shipfast demo can be found [here](https://blog.approov.io/tag/a-series-shipfast).


# REQUIREMENTS

Your computer needs to have installed:

* Docker
* Docker Compose `>= 2.1`
* Git


# INSTALL

Clone repository:

```bash
git clone git@github.com:approov/shipfast-api-protection.git && cd shipfast-api-protection
```


# ENVIRONMENT CONFIGURATION

An Approov [trial subscription](https://info.approov.io/demo) is necessary to get a [registration access token](https://approov.io/docs/starthere.html#app-registration) and the [Approov Token Secret](https://approov.io/docs/starthere.html#setup).

After receiving the email with the token:

```bash
./shipfast register access-token paste-here-the-registration-access-token
```

Let's use the `.env.example` file as starting point for our `.env` file:

```bash
cp .env.example .env
```

Now we need to add values for:

* APPROOV_TOKEN_SECRET - see how to get it [here](https://approov.io/docs/starthere.html#setup)
* SHIP_FAST_AUTH0_DOMAIN - you can get one from [here](https://auth0.com/)

All other defaults in the`.env` file are fine to run the demo.


# SETUP

Build docker image:

```bash
./shipfast build shipfast-demo
```

Start demo:

```bash
/shipfast start
```

## Android Studio

* Disable Instant Run on Android studio.
* Create a mobile device in the emulator.
* Start the app in the emulator mobile device:
    + In the editor terminal, use the bash script to add the Proxy certificate to the emulator.
    + Enable high accuracy location in the emulator mobile device settings.
    + Add manually the London geo location to the emulator Location settings.


# COMMANDS

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
