# ShipFast API Protection Walkthrough

Welcome! This demo will walk you through the process of defending against various
exploits in a mobile application to gain access to data on a remote server allowing
real users of the system to gain an unfair business advantage at the expense of the
company.

## The Service

"ShipFast" is a shipment delivery service company who subcontract the pickup and
delivery of shipments to anyone wishing to earn some additional income. These guys
are the "Shippers". They earn a wage for the delivery of shipments, but also have the
opportunity to earn an additional bonus for some shipments which include an
associated gratuity. Not all shipments have a gratuity, and are therefore not all
equal.

ShipFast provides the actual delivery service to customers on a subscription basis.
Shippers are paid their standard wage, and any gratuity provided by customers ahead
of time is passed on, 100%, to Shippers.

ShipFast needs to run a tight ship, so they are keen to maintain optimal efficiency
in their service and therefore ensure that distances covered by Shippers to pick up
shipments from one location and deliver them to another location are kept to an
absolute minimum. Therefore, Shippers have no way to access the list of available
shipments and pick and choose: they are always given the available shipment which
is closest to their current location.

## The Exploit

There is no hijacking of user credentials here or tricking users to tap things in
the app they shouldn't. The attack is much more subtle, because it is exploited
by real trusted users of the system.

Recall in the explanation above ("The Service") that shipments are not created
equally: some have gratuity associated with them, but the Shippers have no way
to pick and choose as they simply get the nearest available shipment to their
location...or do they?

Enter: ShipRaider. And an evil pirate laugh I will resist imitating.

Since the ShipFast server API has an endpoint to acquire the nearest available
shipment given a Shipper's location data (expressed by latitude and longitude),
reverse engineering the server API tells us it is possible to drive out the
backend server data and enumerate the list of available shipments by sweeping
over a geographical area, sending multiple fake Shipper location values to the
server endpoint.

Once we can enumerate this data, we can grab the shipments in turn with the highest
associated gratuity and earn as much cash as possible, as real legitimate Shippers,
but at the expense of the ShipFast company who will be hurt by the increased
distances Shippers are travelling and the increased time it takes shipments to be
delivered. Bad for profit. Bad for customers. Bad for business. Great for us
Shippers though!

## The Battlefield

For this demonstration, where no Shippers will be hurt, we will show various stages
to defend the ShipFast mobile app and server API, and the attacks used to work
around these defences.

### Stage 1

In the first stage, we will show how ShipFast secure access to their server by
authenticating users and by providing an API key to identify what is talking to
the service. The API key will be present in the app's metadata.

We will then show how easy it is to extract this from the app statically and
use it against the server API.

### Stage 2

In the second stage, we will show how ShipFast secure access to their server API
by introducing a Keyed-Hash Message Authentication Code (HMAC) to authenticated
API requests to digitally sign them and prevent hijacking and tampering.

HMAC's use a secret and a message to produce a cryptographic signature, and
therefore tell you two things: the integrity of the message (has it been modified?)
and the authenticity of the message (is the person who signed it in possession
of the secret key?).

The secret key is embedded statically in app code, and we will show how this
can be extracted, along with the message components, statically from the app
and used against the server API.

### Stage 3

In the third stage, we will extend what we did in Stage 2 but instead of using
a static secret we will us a dynamic secret, that is, a secret which is computed
at app runtime and therefore is not known until the app is actually running, so
cannot easily be extracted statically.

We will then show how an obfuscated and digitally-signed app can be repackaged
to support debugging, then debug the app by introducing a breakpoint at the
creation of the HMAC in order to steal the dynamic secret. We will use our
knowledge from static analysis of the app in Stage 2 to guide the dynamic
analysis in Stage 3.

### Stage 4

Clearly, we need something stronger! Stepping back from the frontline of the
battlefield for a moment, and gathering the troops and the strategic plans, we
conclude that our server API must know reliably _what_ is talking to it: is it
ShipFast, or is it ShipRaider (or indeed something else). What we really need,
is a way of authenticating the running mobile application in addition to the
user and the network channel. We need something which digitally fingerprints the
app reliably and without using behavioural heuristics which aren't always accurate
and take time to warm up to genuine and rogue app behaviour, and then communicates
that digital fingerprint to our API servers so we can verify it and decide how
to respond.

## Setting up the Demo

### Things You Will need

At the risk of sounding like a flat-packed furniture instruction manual, there
are some things you will need to run the demo yourself. The good news is that
these are easy to get hold of, and are free!

1. An Auth0 account, which you can get from https://auth0.com
1. A Google Maps API key, which you can get from https://developers.google.com/maps/documentation/android-api/signup
1. Android Studio 3, which you can get from https://developer.android.com/studio
1. Node.js, which you can get from https://nodejs.org

### Configuring Auth0

1. Create a new Native Client in the Auth0 dashboard and name it "ShipFast"
1. Take careful note of your Auth0 Domain and Client ID as these will be
required for user authentication
1. In the "Allowed Callback URLs" field, enter
```
demo://YOUR-ACCOUNT.auth0.com/android/com.criticalblue.shipfast/callback, http://127.0.0.1
```
replacing YOUR-ACCOUNT with your Auth0 account name
1. Auth0 should already be pre-configured to include Google and GitHub social
accounts allowing you to log in to ShipFast with those, but go ahead and add
more if you wish

### Configuring the Demo

1. Open the file "shipfast-api-protection/server/shipfast-api/demo-configuration.js"
in your favourite text editor and change the server host name to the name of the
machine which you intend to use to host the ShipFast and ShipRaider servers:
```
config.serverHostName = "MY_SERVER_HOST_NAME"
```
1. Generate a self-signed certificate and private key so that you can host your
server over HTTPS using TLS to protect the network channel:
```
node generate-cert.js
```
1. Configure an Android Emulator to run Android 6 Marshmallow and install the
**.crt** file generated in the previous step onto the emulator
1. Ensure the Android Emulator has sufficient permission to use high accuracy
location data (Settings->Location->Mode set to "High accuracy")
1. Open the Android Studio ShipFast project in "shipfast-api-protection/app/android/kotlin"
1. In Android Studio, open the app's manifest "app/manifests/AndroidManifest.xml"
and enter your Google Maps API key:
```
<meta-data android:name="com.google.android.geo.API_KEY" android:value="PUT-YOUR-API-KEY-HERE"/>
```
1. In Android Studio, open the string resource file "app/res/values/strings.xml"
and enter your Auth0 client ID and domain:
```
<string name="com_auth0_client_id">PUT-YOUR-CLIENT-ID-HERE"</string> <string name="com_auth0_domain">PUT-YOUR-DOMAIN-HERE"</string>
```

### Running the Demo

The demo has three components: the mobile app, the ShipFast server and the
ShipRaider rogue web server.

1. Launch the ShipFast server as follows:
```
cd shipfast-api-protection/server/shipfast-api
npm install
node api-server.js
```
(the server may need to run as admin to host on port 443 as HTTPS)
1. Launch the ShipFast mobile app in Android Studio using an x86 emulator
running Android 6 Marshmallow
1. Launch the ShipRaider rogue web server as follows:
```
cd shipfast-api-protection/server/shipraider-rogue-web
npm install
node rogue-web-server.js
```

The ShipFast server generates sample shipment data, held in memory, when the
first request for the nearest shipment is made to the server. The sample
shipment pickup and delivery locations are generated around an origin point
given by the first call to fetch the nearest shipment, so if the actual client
location changes dramatically, you will probably need to restart the server to
regenerate sample data around a new origin point.

### Directory Structure

For reference, this is the structure of the git repo directory:

* **shipfast-api-protection/server/shipfast-api** - The 'genuine' ShipFast API
Node.js server
* **shipfast-api-protection/server/shipfaster-rogue-web** - The 'rogue' ShipRaider web Node.js server
* **shipfast-api-protection/app/android/kotlin** - The Android Kotlin mobile app
