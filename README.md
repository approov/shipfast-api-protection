# ShipFast API Protection Walkthrough

Welcome! This repository is part of [this series](https://blog.approov.io/tag/a-series-shipfast) of Blog posts on practical API security techniques. The series walks you through the process of defending a mobile API backend against various exploits which an attacker may use to gain access to the data it holds. In this demonstration scenario, the attack allows real users of the system to gain an unfair business advantage at the expense of the company.

## SETTING UP THE DEMO

Only required when you want to play around as you follow the blog post series.

Regardless of the demo stack you will choose, the following steps are required.

### Clone the Shipfast Repository

```bash
git clone https://github.com/approov/shipfast-api-protection.git && cd shipfast-api-protection
```

### The Env File

We will use a `.env` file in the root of this project to drive the configuration of the ShipFast demo.

```bash
cp .env.example .env
```

Through the rest of this setup we will ask you several times to edit this file and replace some placeholder with their real world values.

### The Shipfast API Key

The value for the `SHIPFAST_API_KEY` in the `.env` file needs to be:

```text
SHIPFAST_API_KEY=2db3be3ce3f9a96ab32bbb997a176dd0c70ad31086a88e26b5152e522d50d331
```

### The HMAC Secret

The value for the `SHIPFAST_API_HMAC_SECRET` in the `.env` file needs to be:

```text
SHIPFAST_API_HMAC_SECRET=3XqYZ17+dy1LMmTNkCqlcsNy2kJEtuD8gzZaRV53bHKc9Lu2Qh4h9fVAcsyXSBcXvaKOWyKuaa3v4uWjOXGYYg==
```

### Google Maps API Key

A Google Maps API key, which you can get from the [Google Cloud Platform Console](https://debians.google.com/maps/documentation/android-api/signup), and that you will need to add into the `.env` file:

```text
ANDROID_GEO_API_KEY=your-google-maps-api-key-here
```

### Free AUTH0 Account

A free Auth0 account, which you can get from [auth0.com](https://auth0.com).

#### Configuring Auth0 in their Dashboard

1. Create a new Native Client in the Auth0 dashboard and name it "ShipFast"
2. Take careful note of your Auth0 Domain and Client ID as these will be
required to add later into the `.env` file.
3. In the "Allowed Callback URLs" field, enter:
    `demo://YOUR-ACCOUNT.auth0.com/android/com.criticalblue.shipfast/callback, shipraider.dev.example.com, http://127.0.0.1`
    replacing *YOUR-ACCOUNT* with your Auth0 account name
4. Auth0 should already be pre-configured to include Google and GitHub social
accounts allowing you to log in to ShipFast with those, but go ahead and add
more if you wish.

#### Configuring Auth0 in the .env File

Now edit the `.env` file and add replace the placehoders for:

```text
AUTH0_DOMAIN=your-domain-for-auth0
AUTH0_CLIENT_ID=your-auth0-client-id
```

## THE DEMO STACK

To follow along the blog series and play around with the Shipfast mobile app we need to build the APK for the Shipfast app and have the ShipfFast API and the Shipraider Web interface running in an online server.

### For the Mobile App

#### From the Command Line

We recommend to build the APK with the bash scripts we provide, as per instructions from each blog in the series, because this makes easier and more consistent to follow the demo across the several demo stages, but you are free to not use this approach.

#### Using the command line from a docker container

The `apk` bash script in the root of the repo is a wrapper around a docker container with all the necessary dependencies to build the Android APK and install it in a real mobile device.

Using the docker container dispenses you from configuring your computer with the dependencies to run the demo, but we understand that not everyone has Docker installed, therefore alternatives are purposed below.

>**IMPORTANT:** ADB server in your computer needs to be stopped when using this approach, because we cannot have two ADB servers connected to the real mobile device via USB. The bash script will stop the server for you, therefore after your are done with the demo you need to restart it again with `adb start-server`.

#### Using the command line from your computer

To use the command line from your computer its necessary that all dependencies are available.

For example the Android build tools version `29.0.3` and the NDK version `21.0.6113669`. Bear in mind that is not an exhaustive list, because the dependencies to install will depend on your system, thats why we recommend to use the docker container approach.

This approach will use the bash scripts at `app/android/kotlin/ShipFast/bin`.

#### With Android Studio from a Docker Container

Some companies restrict what Android Studio version a developer may be using and/or what they can install on it, thus we offer a docker image with Android Studio. Using this approach will not affect the current Android Studio installation on your computer, and when you are done with the demo you just remove the docker container and image to remove any traces of it in your system.

See how you can do it on the section: [Running Android Studio from a Docker Container](/docs/FULL_STACK.md#running-android-studio-from-a-docker-container).

#### With your Android Studio

If at work you don't have constrains in what version of Android Studio you can use, neither what can be installed on it, then you can just use this repo as any other mobile project you are used to work in to build the APK and run it in the emulator or mobile device.

### For the Backend

The backend is made of two NodeJS servers, one for the ShipFast API, and another for the ShipRaider web interface.

#### With our Online Servers

To make it easier to follow the demo we provide the ShipFast API at `https://shipfast.demo.approov.io` and the ShipRaider web interface at [shipraider.demo.approov.io](https://shipraider.demo.approov.io).

#### With your Online Servers

If you prefer to be in control of the backend servers, then they need to be reachable from the Internet, therefore you need to deploy them into an online server. Deploying them on localhost will not allow for some of the best features of Approov to be seen in action, like the dynamic certificate pinning.

You can deploy very easily your own online servers by following one of our guides:

* [AWS EC2 Traefik Setup](https://github.com/approov/aws-ec2-traefik-setup)
* [Debian Setup](/docs/SETUP_ONLINE_DEBIAN_SERVER.md)

### The Full Stack

If you want to be in control of the full stack, then please see the instructions [here](/docs/FULL_STACK.md).

## RUNNING THE DEMO

Please follow the instructions for each stage of the demo as per instructions in [this series](https://blog.approov.io/tag/a-series-shipfast) of blog posts.

## TROUBLESHOOTING

### Environment values not reflected in the demo

* Every time you update the `.env` file you need to rebuild an install the ShipFast mobile app.

### Not Getting Active Shipments in the Mobile App

Check in the `.env` file that `SHIPFAST_DEMO_STAGE` and `SHIPFAST_API_VERSION` have the correct values as per the comments on the same file.

### ShipRaider not Getting Shipments or just a couple of them

* You need to login with same user as you have logged in the mobile app.
* When using the mobile app in a real device you need to click in the find my location button first.
* If you tweak the location sweep radius and/or the location sweep step you may get less or no shipments at all.
