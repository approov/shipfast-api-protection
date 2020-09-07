# ShipFast - Advanced Usage

This guide is for anyone that wants to build all the components used in the Shipfast blog series from scratch. This is not normally necessary as you can follow the blog series using the services and apps deployed and maintained by the Approov team, as described in the [blog series](https://blog.approov.io/tag/a-series-shipfast) and in the top level [README](/README.md#the-repository-structure). You will need to follow these instructions if you take up the challenge to pentest Approov, presented at the end of the [final blog post](https://blog.approov.io/practical-api-security-walkthrough-part-4) of the series.

The setup steps can be summarized as follows:

* [Install dependencies](/docs/ADVANCED_USAGE.md#install-dependencies): Docker, Docker Compose, Git, Approov CLI. We capture as many build and deployment dependencies/steps in docker images so we can simplify the overall process and reduce the number of steps we have to document here
* [Approov Free Trial](/docs/ADVANCED_USAGE.md#approov-free-trial) - Sign up for a free trial Approov account
* [Google Maps](/docs/ADVANCED_USAGE.md#google-maps) - Obtain Google Maps API Key
* [DNS records](/docs/ADVANCED_USAGE.md#dns-records) - Configure a sub-domain to be used as the base domain for creating the ShipFast and ShipRaider endpoints. We need web-accessible end-points, so that the Traefik edge-router can automate the process of obtaining and managing LetsEncrypt certs to protect communications between the ShipFast API and the app. It also enables us to use Approov's dynamic certificate pinning which simplifies the management of pinned TLS connections in apps.
* [Free Auth0 Account](/docs/ADVANCED_USAGE.md#free-auth0-account) - Sign up and configure a free trial Auth0 account
* [ShipFast Configuration](/docs/ADVANCED_USAGE.md#shipfast-configuration) - Clone and configure this repository for building in your environment
* [ShipFast App APKs](/docs/ADVANCED_USAGE.md#shipfast-app-apks) - Run the scripts to build the ShipFast APKs. A different APK is generated for each blog post; they can all be installed on a device at the same time
* [Backend Servers Setup](/docs/ADVANCED_USAGE.md#backend-servers-setup) - Configure a box to serve the ShipFast API and the ShipRaider website and deploy it

This process will probably take about four hours, although it will take less if you are already familiar with some of the steps.

## Install Dependencies

Please install the required dependencies using instructions appropriate for your host platform

* [Docker](https://docs.docker.com/install/)
* [Docker Compose](https://docs.docker.com/compose/install/) `>= 2.1`
* [Git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
* [Approov CLI](https://approov.io/docs/latest/approov-installation/#approov-tool)

## Approov Free Trial

Please sign up for a free trial [here](https://approov.io/signup). There is a small set of questions and you will need a business email address. Once you have been onboarded, which can take up to 24hrs, you will receive a set of management tokens which you need in order to use the Approov CLI.

## Google Maps

The ShipFast app uses Google maps to display locations. Please follow [these instructions](https://developers.google.com/maps/documentation/android-sdk/get-api-key) to get your API key; you will need a Google account.

## DNS Records

In our setup, the ShipFast API and ShipRaider website will be deployed in docker containers behind a Traefik edge-router that will manage TLS termination and traffic forwarding. The easiest way to achieve this is to create a new subdomain, from a domain you already own, and then define a wildcard route for that subdomain that will target the box running the services. How you change the DNS configuration will depend on the DNS provider for your chosen domain.

You cannot add the Address records to your DNS configuration until you have deployed the ShipFast services, but you need the DNS names you will use to configure Auth0. So, at this stage, please choose the subdomain you will use and then proceed with Auth0 configuration.

For example, if you controlled the domain, `example.com` and you chose the subdomain `server`, you will later need to add the Address record `*.server.example.com` targeting the server running the shipfast backend examples.

## Free Auth0 Account

The ShipFast app uses Auth0 to provide OAUTH2 user authentication. Please sign up for a free Auth0 account on [their website](https://auth0.com).

### Configuring Auth0

Once you have an account, please follow these steps to configure it for ShipFast. *You will need to replace `<<YOUR-ACCOUNT>>` with your Auth0 account name and `<<YOUR-DOMAIN>>` with the domain you chose in the previous step*:

1. Create a new Native Client in the Auth0 dashboard and name it "ShipFast"
2. Take a note of your Auth0 Domain and Client ID as these will be required later to configure the build.
3. In the "Allowed Callback URLs" field, enter:

    ```txt
    demo://com.criticalblue.shipfast/android,
    demo://<<YOUR-ACCOUNT>>.auth0.com/android/com.criticalblue.shipfast.api_key/callback,
    demo://<<YOUR-ACCOUNT>>.auth0.com/android/com.criticalblue.shipfast.static_hmac/callback,
    demo://<<YOUR-ACCOUNT>>.auth0.com/android/com.criticalblue.shipfast.dynamic_hmac/callback,
    demo://<<YOUR-ACCOUNT>>.auth0.com/android/com.criticalblue.shipfast.approov/callback,
    https://shipfast.<<YOUR-DOMAIN>>,
    https://api-key.shipraider.<<YOUR-DOMAIN>>,
    https://static-hmac.shipraider.<<YOUR-DOMAIN>>,
    https://dynamic-hmac.shipraider.<<YOUR-DOMAIN>>,
    https://approov.shipraider.<<YOUR-DOMAIN>>
    ```

4. Auth0 should already be pre-configured to include Google and GitHub social
accounts allowing you to log in to ShipFast with those, but go ahead and add
more if you wish.

## ShipFast Configuration

The ShipFast build is mainly configured by modifying the `.env` file in the root of the repository. Then there are bash scripts to build the  different components: ShipFast App APKs, the ShipFast API server, and the ShipRaider Web server. These all depend on the same `.env` file.

### Clone the ShipFast Repository

```txt
git clone https://github.com/approov/shipfast-api-protection.git && cd shipfast-api-protection
```

### Copy the Example Env File

The example env file, included in the repository file has placeholder values that need to be modified before it can be used to build the components successfully.

```txt
cp .env.example .env
```

### Approov Setup

Approov is added to all Apps, although it isn't used until the last blog article.

#### Download the Approov SDK

```txt
approov sdk -getLibrary ./app/android/kotlin/ShipFast/approov/approov.aar
```

#### Set the Initial Config for the Approov SDK

```txt
approov sdk -getConfig initial-config.txt
```

Now edit the `.env` file and add the contents of the file `initial-config.txt` to the var:

```txt
APPROOV_INITIAL_CONFIG=initial-approov-config-here
```

#### Approov Secret

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

The ShipFast API requires an API Key to gain access. For a new build we need to generate one and add it to the `.env` file:

```txt
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

Now add the domain for the ShipFast API and the ShipRaider website to the `.env` file, again replacing `<<YOUR-DOMAIN>>` with the domain you chose in the DNS configuration step:

```txt
SHIPFAST_PUBLIC_DOMAIN=shipfast.<<YOUR-DOMAIN>>
SHIPRAIDER_PUBLIC_DOMAIN=shipraider.<<YOUR-DOMAIN>>
```

### Optional Customization

There is a small amount of customization supported when the apps are built

#### Custom location

If you are using an emulator, you can configure the default GoogleMaps location that is presented by the app when it starts. (If you are using a real device, then the app will display your current location.)

Add your preferred starting location to the `.env` file. *You will also need to go to the settings for the Android emulator and set the defaults there to use the same coordinates.*

```txt
DRIVER_LATITUDE=51.535472
DRIVER_LONGITUDE=-0.104971
```

#### Custom Currency and Metric System

If you wish, you can adjust the currency symbol and whether to use Miles or Kilometers:

```txt
CURRENCY_SYMBOL="£"
DISTANCE_IN_MILES=true
```

## ShipFast App APKs

The ShipFast APKs are built from a docker container that includes all the necessary dependencies. The different steps are triggered by running the supplied bash script with different parameters.

### Build the Docker Image

```txt
./apk docker-build
```

### Create the Key Store to Sign the APKs

```txt
./apk create-keystore
```

You will need to add the keystore password to the app's properties file, `./app/android/kotlin/ShipFast/local.properties`:

```txt
android.private.key.password=YOUR_PASSWORD_HERE
android.keystore.password=YOUR_PASSWORD_HERE
```

### Gradle Build for the APKs

To build the APKs for all blog posts at once, run the following command:

```txt
./apk gradle build
```

> **NOTE:** The bash script wrapper supports gradle option forwarding. For example, if you run into problems, try the following: `./apk gradle build --stacktrace`.
> **NOTE:** The different product flavours can be found in the [build.gradle](https://github.com/approov/shipfast-api-protection/blob/dev-shipfast-improved_top-level-readme/app/android/kotlin/ShipFast/app/build.gradle#L69) file.

## Backend Servers Setup

The server setup relies on Traefik and Docker. Traefik uses LetsEncrypt certificates to secure its TLS connections. You will need to have a server that is accessible through DNS to complete this part of deployment

### Install and Setup Traefik

To easily deploy your own online server just follow one of these guides:

* [AWS EC2 Traefik Setup](https://github.com/approov/aws-ec2-traefik-setup)
* [Debian Traefik Setup](https://github.com/approov/debian-traefik-setup)

Once the servers are deployed please make them reachable using the domains you decided on earlier by adding the appropriate DNS Address records.

### Install and Setup ShipFast and ShipRaider servers

You will need to run the instructions for this section on the box running the Traefik server you setup in the previous step.

Make a folder to hold files associated with the shipfast deployment. We have used a folder named `demo` and clone the shipfast repository into that. All subsequent instructions should be run from inside the repository.

```txt
mkdir ~/demo
git clone https://github.com/approov/shipfast-api-protection.git ~/demo/shipfast-api-protection
cd ~/demo/shipfast-api-protection
```

#### Copy the Env File from your Computer

From your local computer run:

```txt
scp .env <<MY-USER>>@<<MY-SERVER>>:~/demo/shipfast-api-protection
```

Confirm it exists in the online server:

```txt
ls -a | grep .env -
```

output should be something like the following:

```txt
.env
.env.example
```

#### Building the Docker Images

```txt
./shipfast build servers
```

### Deploy the ShipFast API Server and ShipRaider Web Servers

Bring up with:

```txt
./shipfast up servers
```

Tail the logs with:

```txt
./shipfast logs --follow
```

or with:

```txt
./shipfast logs --follow api
```

Restart with:

```txt
./shipfast restart servers
```

Bring the services down with:

```txt
./shipfast down servers
```

> **NOTE:** you can handle just the API server or the Web servers by replacing `servers` with `api` or `web`, like `./shipfast restart api`.

### Accessing the Online Servers

The top level README of this repository contains the urls for the versioned APIs:

* [ShipFast API Server](/README.md#shipfast-api)
* [ShipRaider Web Servers](/README.md#shipraider-web-interface)

## TROUBLESHOOTING

### Environment values not reflected in the servers

* Every time you update the `.env` file you need to restart the servers with `./shipfast restart servers`.

### Not Getting Active Shipments in the Mobile App

This usually happens when you are running the APK for the final blog post, `APPROOV_APP_AUTH_PROTECTION`, but the APK has not been registered with the Approov service. Please use the Approov CLI tool to:

* Register the APK.
* Add the API server domain.

After performing the above actions you will need to restart the mobile app in order to get a new Approov token.

### ShipRaider not Getting Shipments or just a few of them

* Make sure you use the same login for the App and for the ShipRaider website
* If you tweak the location sweep radius and/or the location sweep step you may end up with fewer shipments, or no shipments at all.
* When using the mobile app on a real device you need to click the "Find My Location" button on ShipRaider.
* When using the mobile app on an emulator please ensure that:

  * in the emulator settings the default location is set to be as near as possible to the coordinates in the `.env` file: the `DRIVER_LATITUDE` and `DRIVER_LONGITUDE`variables
  * do not click the "Find My Location" button
  * the coordinates shown in the web interface match the ones you set in the emulator settings
