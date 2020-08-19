# ShipFast API Protection Walkthrough

Welcome! This repository is part of [this series](https://blog.approov.io/tag/a-series-shipfast) of Blog posts on practical API security techniques. The series walks you through the process of defending a mobile API backend against various exploits which an attacker may use to gain access to the data it holds. In this demonstration scenario, the attack allows real users of the system to gain an unfair business advantage at the expense of the company.


## Why?

When a developer builds or use an API the attestation mechanism used to ensure that the API only serves requests to the authorized mobile apps involves an identifier(a long and random string) that it's commonly named as an API key, and that is often used along side with user authentication for that endpoints the business logic requires to know the user.

This demo purpose is to educate the developer that an API key is easily bypassed, and show how other more sophisticated alternatives can be used, like HMAC, that unfortunately is also not hard to bypass.

By the end of the demo a mobile app attestation solution is introduced to show the developers how it's possible to mitigate with an high degree of confidence all the previous techniques bypasses.


## How it Works?

This demo repository is part of [this series](https://blog.approov.io/tag/a-series-shipfast) of blog posts that goes into a great detail to explain how several techniques can be used in order to lock the mobile app to the API server.

For each technique the blog post will show how it's implemented by the mobile app and API server, and how an attacker can bypass it in order to make requests to the API server as if it was the mobile app itself. So each blog post in the series will have a section dedicated how to perform the attack and another section about how to defend against it, with code examples and links to the lines of code on this repo.

### ShipFast Mobile App

The [releases page](https://github.com/approov/shipfast-api-protection/releases) of this repo will contain APKs for each of the demo stages that you can install in your mobile device to follow along the demo.

Each APK was built from the code at [app/android/kotlin/ShipFast](/app/android/kotlin/ShipFast) with the [./apk](/apk) bash script on the root of this folder.

All APKs can coexist side by side on your mobile device, because they were built as product flavors, as you can see at the [app/android/kotlin/ShipFast/app/build.gradle](/app/android/kotlin/ShipFast/app/build.gradle#L69).

Each demo stage will have it's own color scheme on the mobile app so that you can easily identify what demo stage you are in:

* *API_KEY_PROTECTION* - blue
* *HMAC_STATIC_SECRET_PROTECTION* - orange
* *HMAC_DYNAMIC_SECRET_PROTECTION* - red
* *APPROOV_APP_AUTH_PROTECTION* - green

The colors do not have any special meaning.

### ShipFast API

Both the ShipFast mobile app ShipRaider will talk with the ShipFast API located at [server/shipfast-api](/server/shipfast-api), and we made i available at https://shipfast.demo.approov.io.

The ShipFast API is versioned from `v1` to `v4` to reflect each demo stage, and you can access directly each stage as per:

* *API_KEY_PROTECTION* - [v1](https://shipfast.demo.approov.io/v1)
* *HMAC_STATIC_SECRET_PROTECTION* - [v2](https://shipfast.demo.approov.io/v2)
* *HMAC_DYNAMIC_SECRET_PROTECTION* - [v3](https://shipfast.demo.approov.io/v3)
* *APPROOV_APP_AUTH_PROTECTION* - [v4](https://shipfast.demo.approov.io/v4)

### ShipRaider

The attacks for each demo stage are performed via a web based application that is provided by the evil pirate ShipRaider.

For your convenience we provide a version of ShipRaider for each demo stage at:

* *API_KEY_PROTECTION* - [api-key](https://api-key.shipraider.demo.approov.io)
* *HMAC_STATIC_SECRET_PROTECTION* - [static-hmac](https://static-hmac.shipraider.demo.approov.io)
* *HMAC_DYNAMIC_SECRET_PROTECTION* - [dynamic-hmac](https://dynamic-hmac.shipraider.demo.approov.io)
* *APPROOV_APP_AUTH_PROTECTION* - [approov](https://approov.shipraider.demo.approov.io)

The ShipRaider web interface also follows the same color scheme used for the ShipFast mobile app in order to facilitate pairing them when following the demo.


## The Demo Overview

Below we will briefly give an overview about each of the techniques used in the demo to lock the API server to the mobile app, with links to the relevant lines of code and respective blog post in the series.

### API Key

The most common method used by developers to identify **what** is making a request to the API server is to use a long string in the request header, that is often named as `Api-Key`.

The API key is very simple to implement in the API server and in the mobile app. The mobile just needs to add it to every request as seen in [this code](/app/android/kotlin/ShipFast/app/src/main/java/com/criticalblue/shipfast/api/RestAPI.kt#L211) and the API server will validate it with a simple check as per [this code](/server/shipfast-api/api/middleware/api-key.js#L28).

#### The First Attack

In this [blog post](https://blog.approov.io/practical-api-security-walkthrough-part-2) we walk you through the first attack scenario where we show you how easy is to extract the API key with a MitM(Man in the Middle) attack.


### Static HMAC

[HMAC(Keyed-Hash Message Authentication Code)](https://en.wikipedia.org/wiki/Hash-based_message_authentication_code) is a common method used to digitally sign API requests in order to detect when the request was hijacked/tampered.

So we will use HMAC to digitally sign the request as the first defense in order to enhance the API protection against the API key extraction outlined in the first attack scenario.

The HMAC implementation is a little more elaborated then the API key implementation, but it's still a simple one to implement in the mobile app and in the API server. You can check [this code](/server/shipfast-api/api/middleware/static-hmac.js#L15) for the mobile app implementation and [this code](/app/android/kotlin/ShipFast/app/src/main/java/com/criticalblue/shipfast/api/RestAPI.kt#L252) for the mobile app implementation.

#### The First Defence

On the same [blog post](https://blog.approov.io/practical-api-security-walkthrough-part-2) we walk you through the first attack scenario we also show you in the *The First Defence* section how to mitigate it with a static HMAC algorithm implementation. We say the HMAC is static, because it relies on an hard-coded secret to digitally sign the request, thus this secret needs to be shipped within the code of the mobile app.

#### The Second Attack

So if the HMAC secret used to digitally sign the requests is hard-coded, then it can be easily retrieved from the code by performing static binary analysis with open source tools. This same tools will also reveal the HMAC algorithm logic, thus allowing an attacker to easily spoof correctly HMAC signed requests from it's own server, and this is explained in more detail at the section *The Second Attack* on this [blog post](https://blog.approov.io/practical-api-security-walkthrough-part-3).


### Dynamic HMAC

The second attack scenario revealed us that using a static secret on as part of the HMAC algorithm was a weak point, because it allows an attacker to bypass it, thus we need a dynamic HMAC secret, one that is computed at runtime.

The implementation for the mobile app can be seen in this [lines of code](/app/android/kotlin/ShipFast/app/src/main/java/com/criticalblue/shipfast/api/RestAPI.kt#L259) while the API server equivalent can be seen [here](server/shipfast-api/api/middleware/dynamic-hmac.js#L16).

#### The Second Defense

To compute the HMAC secret at runtime we will use something on the request and merge it with the static HMAC secret to give us the dynamic HMAC secret that the HMAC algorithm will use to digitally sign the request, and we go into a lot of more detail on the section *The Second Defense* on this [blog post](https://blog.approov.io/practical-api-security-walkthrough-part-3).

#### The Third Attack

What changed from the static HMAC to the dynamic HMAC is that the secret is now computed during runtime, thus making it harder to bypass, but not impossible, because if we have reverse engineered previously the HMAC algorithm for the static HMAC implementation, we just need to do the same for the dynamic HMAC secret implementation, and the section *The Third Attack* on this [blog post](https://blog.approov.io/practical-api-security-walkthrough-part-4) as great detail about how this is achieved.


### Approov Mobile App Attestation

The mobile app attestion is a concept developed by Approov and described in detail on this [white paper](https://approov.io/download/Approov-Whitepaper-Security-Trust-Gap.pdf) or if you prefer a shorter read on our [product overview](https://approov.io/product) page. In a nutshell Approov can be described has the mechanism that will allow you to lock-down the API server to genuine instances of your mobile apps, with a very high degree of confidence.

The Approov integration is simple as it can be for the mobile app developers, that only need to write *4 lines of code* to use the [Approoov SDK](https://approov.io/docs/latest/approov-usage-documentation/#sdk-integration) from the [Approov Service](https://approov.io/docs/latest/approov-integration-examples/mobile-app/) more adequate to the HTTP stack being used by the mobile framework/platform that the mobile app is being developed for, as it can be seen in the [ShipFastApp.kt](/app/android/kotlin/ShipFast/app/src/main/java/com/criticalblue/shipfast/ShipFastApp.kt) class for the lines that are preceded by `// *** UNCOMMENT THE CODE BELOW FOR APPROOV ***`.

The backend integration is also simple and only requires to use a JWT library to verify the Approov token, and in this NodeJS backend we use the [express-jwt](https://www.npmjs.com/package/express-jwt). The minimal code to verify the Approov toke with this library can be seen on the [checkApproovToken](/server/shipfast-api/api/approov/approov-token-check.js#L129) callback at the [approov-token-check.js](server/shipfast-api/api/approov/approov-token-check.js) file.

#### The Final Defense

Approov is used to ensure that the API server know with a very high degree of confidence if it can trust in the incoming request as one originated from a genuine and untampered version of the mobile app, thus making the mobile app the only one that is now capable of sending valid requests to the API server, and the section *The Final Defense* on the last [blog post](https://blog.approov.io/practical-api-security-walkthrough-part-4) on this series as a lot of detail on why and how the Approov integration is implemented.


## Useful Links

* [Approov Free Trial](https://approov.io/signup)(no credit card needed)
* [Approov QuickStarts](https://approov.io/docs/latest/approov-integration-examples/)
* [Approov Live Demo](https://approov.io/product/demo)
* [Approov Docs](https://approov.io/docs)
* [Approov Blog](https://blog.approov.io)
* [Approov Resources](https://approov.io/resource/)
* [Approov Customer Stories](https://approov.io/customer)
* [Approov Support](https://approov.zendesk.com/hc/en-gb/requests/new)
* [About Us](https://approov.io/company)
* [Contact Us](https://approov.io/contact)
