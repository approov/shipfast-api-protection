# ShipFast - Advanced Usage

This guide is for anyone that wants to build all the components used in the Shipfast blog series from scratch. This is not normally necessary as you can follow the blog series using the services and apps deployed and maintained by the Approov team, as described in the [blog series](https://blog.approov.io/tag/a-series-shipfast) and in the top level [README](/README.md#the-repository-structure). You will need to follow these instructions if you take up the challenge to pentest Approov, presented at the end of the [final blog post](https://blog.approov.io/practical-api-security-walkthrough-part-4) of the series.

The setup steps can be summarized as follows:

* [Install dependencies](/docs/ADVANCED_USAGE.md#install-dependencies): Docker, Docker Compose, Git, Approov CLI
* [Approov Free Trial](/docs/ADVANCED_USAGE.md#approov-free-trial) - Sign up for a free trial Approov account(no credit card needed)
* [Google Maps](/docs/ADVANCED_USAGE.md#google-maps) - Obtain Google Maps API Key
* [Free Auth0 Account](/docs/ADVANCED_USAGE.md#free-auth0-account) - Sign up and configure a free trial Auth0 account
* [DNS records](/docs/ADVANCED_USAGE.md#dns-records) - Configure a sub-domain to be used as the base domain for creating the ShipFast and ShipRaider ones.
* [ShipFast Demo Setup](/docs/ADVANCED_USAGE.md#shipfast-demo-setup) - Clone this repository then configure it to be able to run all demo stages side by side
* [ShipFast App APKs](/docs/ADVANCED_USAGE.md#shipfast-app-apks) - Build the ShipFast APKs for each demo stage
* [Backend Servers Setup](/docs/ADVANCED_USAGE.md#backend-servers-setup) - Configure a box to serve the ShipFast API and the ShipRaider website and deploy it

This process will probably take around 1 hour in average. It can take less time if you already have some of the dependencies in your computer and if you already have an online server with Traefik installed. Being familiar with some of the technologies and services used will also help you go faster through this setup.

## Advanced Usage Overview

To understand how this repo is structured for the ShipFast demo you can read the detailed explanation on the top level [README](/README.md#the-repository-structure), but in an high level the ShipFast demo is made of the three components: ShipFast Mobile App, ShipFast API and the ShipRaider web interface. The advanced usage guide is a step by step for setup and deploy each of this three components, where you will learn how to run the ShipFast API and the ShipRaider web interfaces for each demo stage in their own online servers, and also how to build the ShipFast mobile app APK for each demo stage.

In order to demonstrate the use of dynamic certificate pinning the ShipFast API needs to run in an online server. Once you already have an online server you may well use it to also run the ShipRaider web interface, but you could run it from localhost.

For the backend servers you will be guided in how to deploy the ShipFast API and ShipRaider web servers with the use of Docker and Traefik. Traefik is mainly used here to provide automated LetsEncrypt certificates creation and renewal. This setup is not complex because bash scripts exist to help with the tasks, and detailed step by step instructions will be provided.

Building the APKs for the ShipFast mobile app is also made easy by using a bash script that uses a docker container to build the APK for each demo stage, and detailed step by step instructions are provided. The docker container has all the required dependencies to build the APKs so that you don't have to deal with them.


## Demo Requirements

In order to avoid changes to your operating system installed packages and to your Android Studio setup we have chosen to run everything from Docker containers, therefore both your computer and online server need to have installed:

### Install Dependencies

#### Docker

* [Docker](https://docs.docker.com/install/).
* [Docker Compose](https://docs.docker.com/compose/install/) `>= 2.1`.

#### Git

* [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).

#### Approov CLI Tool

The Approov CLI tool is necessary to perform several tasks in the last stage of the demo, when we integrate Approov into the ShipFast app. Please follow [this instructions](https://approov.io/docs/latest/approov-installation/#approov-tool) to install it for your computer operating system.

### Approov Free Trial

To use the Approov CLI tool you need a developer management token and and administration management token for the CLI tool to be able to talk with the Approov Cloud Service, thus you need to signup for a free Approov trial [here](https://approov.io/signup). No credit card needed.

### Google Maps

The ShipFast app uses Google maps, thus a [Google Maps API key](https://developers.google.com/maps/documentation/android-sdk/get-api-key) is needed, which you can get from the [Google Cloud Platform Console](https://cloud.google.com/console/google/maps-apis/overview) in order to use later when configuring the demo.

### Free AUTH0 Account

The ShipFast app uses OAUTH2 for the user authentication via the Auth0 provider, and you can get a free Auth0 account from https://auth0.com.

#### Configuring Auth0 in their Dashboard

1. Create a new Native Client in the Auth0 dashboard and name it "ShipFast"
2. Take careful note of your Auth0 Domain and Client ID as these will be
required to add later into the `.env` file.
3. In the "Allowed Callback URLs" field, enter:
    ```
    demo://com.criticalblue.shipFast/android,
    demo://YOUR-ACCOUNT.auth0.com/android/com.criticalblue.shipFast.api_key/callback,
    demo://YOUR-ACCOUNT.auth0.com/android/com.criticalblue.shipFast.static_hmac/callback,
    demo://YOUR-ACCOUNT.auth0.com/android/com.criticalblue.shipFast.dynamic_hmac/callback,
    demo://YOUR-ACCOUNT.auth0.com/android/com.criticalblue.shipFast.approov/callback,
    https://shipFast.demo.example.com,
    https://api-key.shipRaider.demo.example.com,
    https://static-hmac.shipRaider.demo.example.com,
    https://dynamic-hmac.shipRaider.demo.example.com,
    https://approov.shipRaider.demo.example.com
    ```
    > **NOTE:** Replace *YOUR-ACCOUNT* with your Auth0 account name

4. Auth0 should already be pre-configured to include Google and GitHub social
accounts allowing you to log in to ShipFast with those, but go ahead and add
more if you wish.

### DNS Records

Traefik is used in front of the docker containers for the ShipFast API and ShipRaider, therefore you need to create a subdomain in a domain you control. So let's say that you own `example.com`, then you just need to create a record with a name of your choice, let's use for example `demo`, that you must point to the IP address of the server where Traefik will be installed on. This will give us `demo.example.com` as the base domain to use across all docker containers we will put behind Traefik.

The DNS records for `demo.example.com` must have a wildcard `*` entry that points to the IP address for the Traefik server. This will allow Traefik to automatically handle any sub domain of `demo.example.com`, like `shipfast.demo.example.com`. Traefik will generate the LetsEncrypt certificates for the domain and renew them when the time comes.

## ShipFast Demo Setup

The demo setup is mainly driven by the `.env` file in the root of this repository. The bash script to build the ShipFast App APKs, the ShipFast API server and the ShipRaider Web servers will depend on the `.env` file, therefore the online servers for ShipFast and ShipRaider must use the same `.env` file that you are about to setup.

### Clone the ShipFast Repository

In the location of choice on your computer run:

```txt
git clone https://github.com/approov/shipfast-api-protection.git && cd shipfast-api-protection
```

### The Env File

We will use a `.env` file in the root of this project to drive the configuration of the demo across the mobile app, ShipFast API and ShipRaider Web.

```txt
cp .env.example .env
```

Through the rest of this setup we will ask you several times to edit this file and replace some placeholder with their real world values.

### Approov Setup

#### Approov SDK

For the last demo stage you will need the Approov SDK, thus you need to add it now to the ShipFast app, because we will build the APKs for all demo stages in one go. No code changes will be necessary, just download it and set the initial config for the Approov SDK in the .env file.

##### Download the Approov SDK

```txt
approov sdk -getLibrary ./app/android/kotlin/ShipFast/approov/approov.aar
```

##### Set the Initial Config for the Approov SDK

```txt
approov sdk -getConfig initial-config.txt
```

Now edit the `.env` file and add the content of the file `initial-config.txt` to the var:

```txt
APPROOV_INITIAL_CONFIG=initial-approov-config-here
```

### Approov Secret

The Approov secret will be used to decode the Approov tokens in the ShipFast API.

Retrieve it with:

```txt
approov secret ~/path/to/administration.token -get base64
```

Now edit the `.env` file and add the secret to the var:

```txt
APPROOV_TOKEN_SECRET=approov-secret-here
```

### The ShipFast API Key

We need one to add into the `.env` file, and we can generate it with:

```
openssl rand -hex 64 | base64 | tr -d '\n'; echo
```

or with:

```txt
strings /dev/urandom | head -n 256 | openssl dgst -sha256
```

Now edit the `.env` file and add it:

```txt
SHIPFAST_API_KEY=your-api-key-here
```

### The HMAC Secret

To generate one:

```txt
openssl rand -base64 64 | tr -d '\n'; echo
```

Now copy the output and and add it into the `.env` file:

```txt
SHIPFAST_API_HMAC_SECRET=the-hmac-secret
```

### Google Maps API Key

Now that you have one add it to the `.env` file:

```txt
ANDROID_GEO_API_KEY=your-google-maps-api-key-here
```

### Configuring Auth0 in the .env File

Now edit the `.env` file and add replace the placehoders for:

```txt
AUTH0_DOMAIN=your-domain-for-auth0
AUTH0_CLIENT_ID=your-auth0-client-id
```

### The ShipFast and ShipRaider Domains

Now the domain for the ShipFast API in the `.env` file will look like:

```txt
SHIPFAST_PUBLIC_DOMAIN=shipFast.demo.example.com
```

During the demo the ShipRaider web interface will be used to attack the ShipFast API, therefore we also need to set in the `.env` file the `SHIPRAIDER_PUBLIC_DOMAIN`, that will look like:

```txt
SHIPRAIDER_PUBLIC_DOMAIN=shipRaider.demo.example.com
```

### Optional Customization

When using the demo we may want to adapt it to your location, like center the map on you address or nearest city, and use your local currency and metric system.

#### Custom location

The custom location is only taken in account when you run the ShipFast in the emulator, otherwise in a mobile device it will use your current location.

Get from Google maps the coordinates for your preferred location and set them in the following env vars:

```txt
DRIVER_LATITUDE=51.535472
DRIVER_LONGITUDE=-0.104971
```

> **NOTE**: If you are using the Android emulator you will need to go to settings and add this same coordinates as the default ones for the emulated device.


#### Custom Currency and Metric System

Adjust the following env vars according to your needs:

```txt
CURRENCY_SYMBOL="£"
DISTANCE_IN_MILES=true
```

## ShipFast App APKs

Now that the the demo setup is finished you can build the APKs for each demo stage.

To make it easier and friction free we have a bash script that will build them inside a docker container, that already has all the necessary dependencies.


### Build the Docker Image

```txt
./apk docker-build
```

### Create the Key Store to Sign the APKs

```txt
./apk create-keystore
```

Add the keystore password to the `./app/android/kotlin/ShipFast/local.properties` file:

```txt
android.private.key.password=YOUR_PASSWORD_HERE
android.keystore.password=YOUR_PASSWORD_HERE
```

### Gradle Build for the APKs

To build the APKs for all demo stages at once you just need to ran from the root of this repository:

```txt
./apk gradle build
```

> **NOTE:** The bash script wrapper supports forwarding the gradle options, therefore you can run the command like `./apk gradle build --stacktrace` if you run into problems.

The build command will build the APK for all the four demo stages as per defined in the product flavors of the [app/android/kotlin/ShipFast/app/build.gradle](https://github.com/approov/shipfast-api-protection/blob/dev-shipFast-improved_top-level-readme/app/android/kotlin/ShipFast/app/build.gradle#L69) file.

## Backend Servers Setup

The reason why the ShipFast App needs to run against an online server is so that you can see the full potential of using Approov, like dynamic certificate pinning, that only works in an online server, because the Approov Cloud needs to be able to reach the domain for the server in order to create an hash of the certificate public key.

### Install and Setup Traefik

The online servers setup relies on Traefik and Docker, thus you can skip this section if you already have an online playground with Traefik listening on port `80` and `443` and configured to auto generated LetsEncrypted certificates.

To easily deploy your own online servers just follow one of our guides:

* [AWS EC2 Traefik Setup](https://github.com/approov/aws-ec2-traefik-setup)
* [Debian Traefik Setup](https://github.com/approov/debian-traefik-setup)

### Install and Setup ShipFast and ShipRaider server

You will need to run the instructions for this section in the Traefik server you setup in the previous step.

#### Working Directory

The ShipFast repo for this demo it's assumed to be installed in the `~/demo` folder, therefore create it:

```txt
mkdir ~/demo
```

> **NOTE:** You can use another folder location in your system, just remember to replace all occurrences of `~/demo` with your own preferred location path.

#### Clone the ShipFast Repository

From the location `~/demo` run:

```txt
git clone https://github.com/approov/shipfast-api-protection.git ~/demo/shipfast-api-protection
```

All subsequent instructions will assume that you are inside the folder `~/demo/shipfast-api-protection`:

```txt
cd ~/demo/shipfast-api-protection
```

#### Copy the Env File from your Computer

From your local computer run:

```txt
scp .env root@demo.example.com:~/demo/shipfast-api-protection
```

Confirm it exists in the online server:

```txt
ls -a | grep .env -
```

output should be like:

```txt
.env
.env.example
```

#### Building the Docker Images

```txt
./shipFast build servers
```

### Deploy the ShipFast API Server and ShipRaider Web Servers

Bring up with:

```txt
./shipFast up servers
```

Tail the logs with:

```txt
./shipFast logs --follow
```

or with:

```txt
./shipFast logs --follow api
```

Restart with:

```txt
./shipFast restart servers
```

Bring down with:

```txt
./shipFast down servers
```

> **NOTE:** you can handle just the API server or the Web servers by replacing `servers` with `api` or `web`, like `./shipFast restart api`.


### Accessing the Online Servers

The top level README of this repository contains the urls per demo stage for:

* [ShipFast API Server](/README.md#shipfast-api)
* [ShipRaider Web Servers](/README.md#shipraider-web-interface)


## TROUBLESHOOTING

### Environment values not reflected in the servers

* Every time you update the `.env` file you need to restart the servers with `./shipFast restart servers`.


### Not Getting Active Shipments in the Mobile App

This usually happens in the demo stage `APPROOV_APP_AUTH_PROTECTION` because you forgot to use the Approov CLI tool to:

* register the APK.
* add the API server domain.

After performing any of the above actions you need to restart the mobile app in order to get a new Approov token.

### ShipRaider not Getting Shipments or just a few of them

* You need to login with same user as you have logged in the mobile app.
* If you tweak the location sweep radius and/or the location sweep step you may end up to get less or no shipments at all.
* When using the mobile app in a real device you need to click in the find my location button on ShipRaider.
* When using from an emulator please ensure that:
    + in the emulator settings the default location is set to be as near as possible of the coordinates in the environment variables `DRIVER_LATITUDE` and `DRIVER_LONGITUDE`.
    + you not click in the find my location button
    + the coordinates shown in the web interface match the ones you set in the emulator settings.
