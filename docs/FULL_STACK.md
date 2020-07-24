# ShipFast API Protection Walkthrough

Welcome! This repository is part of [this series](https://blog.approov.io/tag/a-series-shipfast) of Blog posts on practical API security techniques, that will walk you through the process of defending against various exploits in a mobile application to gain access to data on a remote server allowing real users of the system to gain an unfair business advantage at the expense of the company.

## REQUIREMENTS

We will walk you through the requirements for each of the three components of the Shipfast demo: Shipfast API, Shipraider Web and Android Studio.

In order to demonstrate the use of dynamic certificate pinning, the Shipfast API needs to run in an online server. Once we already have an online server, we will use it to also run the ShipRaider web interface.

In some workplaces developers are constrained about what Android Studio version they can use and what they can install on it, therefore we provide a Docker image that will allow you to run it from inside a Docker container, including the emulator, therefore not messing with your setup.

If your don't have this constrains for running Android Studio then feel free to follow the Shipfast demo with your own Android Studio installation.


### For Your Computer and Online Server

* [Docker](https://docs.docker.com/install/).
* [Docker Compose](https://docs.docker.com/compose/install/) `>= 2.1`.
* [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git).

> **NOTE:** This are optional requirements, because we provide the [online servers](#the-online-servers) ready to use.

## SETTING UP THE DEMO

Both Android Studio, Shipfast API and Shipraider Web will depend on the `.env` file. If you decide to run the full stack yourself, then the online servers for Shipfast and Shipraider should use the same `.env` file used in your computer for starting Android Studio.

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

### The HMAC Secret

To generate one:

```
openssl rand -base64 64 | tr -d '\n'; echo
```

Now copy the output and and add it into the `.env` file:

```
SHIPFAST_API_HMAC_SECRET=the-hmac-secret
```

### Google Maps API Key

A Google Maps API key, which you can get from the [Google Cloud Platform Console](https://debians.google.com/maps/documentation/android-api/signup), and that you will need to add into the `.env` file:

```
ANDROID_GEO_API_KEY=your-google-maps-api-key-here
```

### Free AUTH0 Account

A free Auth0 account, which you can get from https://auth0.com.

#### Configuring Auth0 in their Dashboard

1. Create a new Native Client in the Auth0 dashboard and name it "ShipFast"
2. Take careful note of your Auth0 Domain and Client ID as these will be
required to add later into the `.env` file.
3. In the "Allowed Callback URLs" field, enter:
    ```
    demo://YOUR-ACCOUNT.auth0.com/android/com.criticalblue.shipfast/callback, shipraider.dev.example.com, http://127.0.0.1
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


### The Environment

If you are running the demo with the Android Studio installed in your computer, then you need to export the vars in the `.env` file:

```
export $(grep -v '^#' .env | xargs -0)
```

Now you need to open Android Studio from the same terminal you have exported the `.env` file.

In Linux type:

```
/opt/android-studio/bin/studio.sh
```

In Windows type:

```
start "" "C:\Program Files\Android\Android Studio\bin\studio64.exe"
```

In a MAC type:

```
open -a /Applications/Android\ Studio.app
```

> **NOTE:**
>
> Exporting the env vars to an Android Studio running as a snap package will not work, because we don't have access to its environment.


### Running Android Studio from a Docker Container

Build the docker image for Android Studio:

```
./shipfast build editor
```

Now start it with:

```
./shipfast up editor
```

The first time the Android Studio is open, its a fresh installation of it from scratch, therefore you will be prompted several times to configure the editor as usual.

At some point of the build you may get this error:

```
A problem occurred configuring project ':app'.
> com.android.builder.sdk.LicenceNotAcceptedException: Failed to install the following Android SDK packages as some licences have not been accepted.
     ndk;21.0.6113669 NDK (Side by side) 21.0.6113669
  To build this project, accept the SDK license agreements and install the missing components using the Android Studio SDK Manager.
```

To fix it open `File > Settings > Appearance & Behavior > System Settings > Android Sdk`, then click in the `SDK Tools` tab, select the boxes for `NDK (Side by side), CMake` , clik `ok` to install them, then accept the licenses in order for the installation to complete.

Now it's time to go to `File > Sync Project with Gradle Files` and be prepared to wait while some more downloads take place, and to see one error in the begin of the logs:

```
No version of NDK matched the requested version 21.0.6113669. Versions available locally: 21.3.6528147
```

that in the end of sync process will show as this error message:

```
ERROR: No version of NDK matched the requested version 21.0.6113669. Versions available locally: 21.3.6528147
Install NDK '21.0.6113669' and sync project
Affected Modules: app
```

To solve it just install the new one or pick the local available version, that in the above message is `21.3.6528147` and update the `ndkVersion` entry in the `app/build.gradle` file with it. Afterwards you need to run the Gradle synchronization again, but this time you should see a successful message.

> **NOTE:** If during this process it fails with the same errors then the best is to invalidate caches and restart Android Studio, and then try to Sync the project again and double check that you have followed correctly all the above instructions. Please let us know if you have find any error not mention in this README.

With a successful Gradle sync in place is now time to use the AVD manager to create a mobile device, so that you can run Shipfast in the emulator, or instead you can skip the emulator step and run the mobile directly in your mobile device.

To get started you need to follow the demo stages steps as per the instructions in each blog post on [this series](https://blog.approov.io/tag/a-series-shipfast).

> **NOTE:** If you decided to run the full-stack remember that before you can play with the Shipfast app on the emulator you need to setup the online server for the Shipfast API, or instead just use our ready made online servers, as per the blog post instructions.

### The Online Server

We provide one at `https://shipfast.demo.approov.io` for the Shipfast API and one at https://shipraider.demo.approov.io for the Shipraider web interface.

If you prefer you can deploy very easily your own online server by following one of our guides:

* [AWS EC2 Traefik Setup](https://github.com/approov/aws-ec2-traefik-setup)
* [Debian Setup](/docs/SETUP_ONLINE_DEBIAN_SERVER.md)

The reason why the Shipfast App needs to run against an online server is so that you can see the full potential of using Approov, like dynamic certificate pinning, that only works in an online server, because the Approov Cloud needs to be able to reach the domain for the server in order to create an hash of the certificate public key.


## TROUBLESHOOTING

### Environment values not reflected in the demo

* Every time you update the `.env` file you need to restart the editor, ShipFast API and the ShipRaider Web.
* Please bear mind that if your Android Studio is running as a snap package then it will not have access o the env vars in your host, neither we can set them in snap sandbox.

### Not Getting Active Shipments in the Mobile App

Check in the `.env` file that `SHIPFAST_DEMO_STAGE` and `SHIPFAST_DEMO_STAGE=v4` have the correct values as per the comments on the same file.

### ShipRaider Not Getting Shipments or just a couple of them

* You need to login with same user as you have logged in the mobile app.
* If you tweak the location sweep radius and/or the location sweep step you may end up to get less or no shipments at all.

#### Driver coordinates

* When using the mobile app in a real device you need to click in the find my location button on Shipraider.
* When using from an emulator please ensure that in the emulator settings the default location is set to be as near as possible of the coordinates in the environment variables `DRIVER_LATITUDE` and `DRIVER_LONGITUDE`.
