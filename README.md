# ShipFast API Protection Walkthrough

Welcome! This repository is part of [this series](https://blog.approov.io/tag/a-series-shipfast) of Blog posts on practical API security techniques.

This demo will walk you through the process of defending against various exploits in a mobile application to gain access to data on a remote server allowing real users of the system to gain an unfair business advantage at the expense of the company.

## REQUIREMENTS

The Shipfast demo has three components: Shipfast API, Shipraider Web and Android Studio. In order to use dynamic certificate pinning, the Shipfast API needs to run in an online server. Once we already have an online server, we will use it to also run the ShipRaider web interface. Android Studio will run from your computer, inside a Docker container.

### For Your Computer and Online Server:

* [Docker](https://docs.docker.com/install/).
* [Docker Compose](https://docs.docker.com/compose/install/) `>= 2.1`.
* [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).


## SETTING UP THE DEMO

Both Android Studio, Shipfast API and Shipraider Web will depend on the `.env` file, therefore the online servers for Shipfast and Shipraider **MUST** use the same `.env` file used in your computer.

### Clone the Shipfast Repository

```bash
git clone https://github.com/approov/shipfast-api-protection.git && cd shipfast-api-protection
```

### The Env File

We will use a `.env` file in the root of this project to drive the configuration of the demo across the mobile app, Shipfast API and Shipraider Web.

```
cp .env.example .env
```

Through the rest of this setup we will ask you several times to edit this file and replace some placeholder with their real world values.

### The Shipfast API Key

We need one to add into the `.env` file, and we can generate one with:

```
openssl rand -hex 64 | base64 | tr -d '\n'; echo
```

or with:

```
strings /dev/urandom | head -n 256 | openssl dgst -sha256
```

Now edit the `.env` file and add it:

```
SHIPFAST_API_KEY=your-api-key-here
```

### Google Maps API Key

A Google Maps API key, which you can get from the [Google Cloud Platform Console](https://developers.google.com/maps/documentation/android-api/signup), and that you will need to add into the `.env` file:

```
ANDROID_GEO_API_KEY=your-google-maps-api-key-here
```

### Free AUTH0 Account

A free Auth0 account, which you can get from https://auth0.com.

#### Configuring Auth0 in their Dashboard

1. Create a new Native Client in the Auth0 dashboard and name it "ShipFast"
2. Take careful note of your Auth0 Domain and Client ID as these will be
required to add later into the `.env` file.
3. In the "Allowed Callback URLs" field, enter
```
demo://YOUR-ACCOUNT.auth0.com/android/com.criticalblue.shipfast/callback, http://127.0.0.1
```
replacing *YOUR-ACCOUNT* with your Auth0 account name

4. Auth0 should already be pre-configured to include Google and GitHub social
accounts allowing you to log in to ShipFast with those, but go ahead and add
more if you wish.

#### Configuring Auth0 in the .env File

Now edit the `.env` file and add replace the placehoders for:

```
AUTH0_DOMAIN=your-domain-for-auth0
AUTH0_CLIENT_ID=your-auth0-client-id
```

### The Shipfast and Shipraider Domains

The Shipfast mobile app will need to communicate with the Shipfast API and for that to happen we need to set `SHIPFAST_PUBLIC_DOMAIN` in the `.env` file.

```
SHIPFAST_PUBLIC_DOMAIN=shipfast.dev.example.com
```

During the demo the ShipRaider Web will be used to attack the ShipFast API, therefore we also need to set `SHIPRAIDER_PUBLIC_DOMAIN`:

```
SHIPRAIDER_PUBLIC_DOMAIN=shipraider.dev.example.com
```

### Optional Customization

If you want to customize the Shipfast demo to match your current location, currency and metric system, then follow [this instructions](/docs/CUSTOMIZE_THE_SHIPFAST_DEMO.md).


### Building the Docker Image for Android Studio

```
./shipfast build editor
```

### Running Android Studio from a Docker Container

```
./shipfast up editor
```

The first time the Android Studio is open, its a fresh installation of it from scratch, therefore you will be prompted several times to configure the editor as usual.

Now you can use the AVD manager to create a mobile device, so that you can run Shipfast in the emulator.

Afterwards you need to open the Shipfast project, and:

* Build the project, and be prepared for some more downloads.
* Start the Shipfast app in the emulator:
    + Enable high accuracy location in the emulator mobile device settings.
    + In the emulator settings, set the location to the same driver coordinates you have on the `.env` file.

> **NOTE:** Before you can play with the Shipfast app on the emulator you need to setup the online server for the Shipfast API.

### Setup the Online Server

Using a VPS provider or a Cloud Provider, just spin the cheapest server they allow, because after you finish this demo you will throw it away, therefore it will cost you only a few cents.

You will also need to create a sub domain on a domain you own, that you will point to this new server, like `dev.example.com`. You can also throw it away after you are done with this demo.

#### Update the New Brand Server

Assuming that you have a new brand server you should have now a shell as the `root` user, thus let's get it up to date with:

```
apt update && apt -y upgrade
```

#### Create Unprivileged User

We will not run the demo as `root`, because it's a best security practice to not run as `root`.

Add a new user with:

```
adduser developer
```
> **NOTE**: Type you password and reply to all other questions with just hitting `enter`.

Add the user to `sudo` with:

```
usermod -aG sudo developer
```

Switch to the `developer` user with:

```
su - developer
```

#### Clone the Shipfast Repository

We need `git` for this:

```
sudo apt install -y git
```

Now we can clone the repo with:

```bash
git clone https://github.com/approov/shipfast-api-protection.git && cd shipfast-api-protection
```

#### Server Setup

This will install Docker, Docker Compose, and Traefik running on a Docker container.

This setup will use a `docker-compose.yml` file to setup a [Treafik](https://docs.traefik.io/) reverse proxy on port `80` and `443` for all docker containers running in the same host machine, therefore you cannot setup this in an existing server.

> **NOTE:** Traefik is being used to automated the process of using `https` for the Shipfast API, because it will auto generate the TLS certificates, meaning zero effort from you to have `https`.

##### Traefik `.env` file

```
cp ./traefik/.env.example ./traefik/.env
```

Now edit `./traefik/.env` to update the place-holder values with your own values:

```
nano ./traefik/.env
```

##### Run the Setup

```
sudo ./bin/setup-online-server.sh
```

#### Shipfast API and Shipraider Web Setup

##### Copy the Env File from your Computer

From your local computer run:

```
scp .env user@my-online-server-ip-or-domain:/home/developer
/shipfast-api-protection
```

Confirm it exists in the online server:

```
$ ls -a | grep .env -
.env
.env.example
```

#### Building the Docker Images

```
./shipfast build servers
```

#### Running the Shipfast API

Bring up with:

```
./shipfast up api
```

Restart with:

```
./shipfast restart api
```

Tail the logs with:

```
./shipfast logs api
```

Bring down with:

```
./shipfast down api
```

#### Running the Shipraider Web

Bring up with:

```
./shipfast up web
```

Restart with:

```
./shipfast restart web
```

Tail the logs with:

```
./shipfast logs web
```

Bring down with:

```
./shipfast down web
```

## TROUBLESHOOTING

### Environment values not reflected in the demo

Every time you update the `.env` file you need to restart the editor, ShipFast API and the ShipRaider Web.

### Not Gettting Shipments in the Mobile App

Please ensure that the emulator as the default location set to be as near as possible of the coordinates in environment variables `DRIVER_LATITUDE` and `DRIVER_LONGITUDE`.

### ShipRaider Not Getting Shipments or just a couple of them

Ensure that the driver coordinates in the web interface are as close as possible from the ones in the environment variables `DRIVER_LATITUDE` and `DRIVER_LONGITUDE`.
