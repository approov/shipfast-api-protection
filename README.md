# ShipFast API Protection Walkthrough

Welcome! This repository is part of [this series](https://blog.approov.io/tag/a-series-shipfast) of blog posts on practical API security techniques. The series walks you through the process of defending a mobile API backend against various exploits which an attacker may use to gain access to the data it holds. In this demonstration scenario, the attack allows real users of the system to gain an unfair business advantage at the expense of the company.

## The Repository Structure

This repository holds all three components that are used to describe the ShipFast story:

* [ShipFast API](/README.md#shipfast-api) - The API that we want to defend from being used by illegitimate clients.
* [ShipFast Mobile App](/README.md#shipfast-mobile-app) - The legitimate client of the ShipFast API.
* [ShipRaider Web Interface](/README.md#shipraider-web-interface) - A rogue web service, setup to abuse the ShipFast API by impersonating the ShipFast Mobile App while making its requests.

We have kept all 3 projects in the same repository and structured the code to include all the steps from the blog series progression. We hope that this makes it easier to understand as a whole.

After setting the scene in the first blog post, successive entries show how security measures may be strengthened (or bypassed) using links to the code in this GitHub repository where appropriate. The blog series may be summarized by referring to the main security method under discussion in each one:

1. [*API Keys*](https://blog.approov.io/practical-api-security-walkthrough-part-1)
2. [*Static HMAC Secret*](https://blog.approov.io/practical-api-security-walkthrough-part-2)
3. [*Dynamic HMAC Secret*](https://blog.approov.io/practical-api-security-walkthrough-part-3)
4. [*Approov Protection*](https://blog.approov.io/practical-api-security-walkthrough-part-4)

We provide freely available deployments of the two services and APKs for you to download and install, so you can work with them as you read the blog. The following sections give a brief summary of the services we have deployed, the apps we provide, where to find the associated code in this repository, and where the changes for each blog post are located.

### ShipFast API

The ShipFast API code can be found in the [server/shipfast-api](/server/shipfast-api) folder. The code is deployed in the cloud and made available at https://shipfast.demo.approov.io.

The ShipFast API is versioned from `v1` to `v4` to follow the blog story and you can access each stage using the following URLs:

* *API_KEY_PROTECTION* - https://shipfast.demo.approov.io/v1
* *HMAC_STATIC_SECRET_PROTECTION* - https://shipfast.demo.approov.io/v2
* *HMAC_DYNAMIC_SECRET_PROTECTION* - https://shipfast.demo.approov.io/v3
* *APPROOV_APP_AUTH_PROTECTION* - https://shipfast.demo.approov.io/v4

### ShipFast Mobile App

The [releases page](https://github.com/approov/shipfast-api-protection/releases) of this repository contains an APK for each stage. They are setup so that you can install all of them at once on your Android device (sorry no iOS at the moment).

The code for the apps is all in one AndroidStudio project: [app/android/kotlin/ShipFast](/app/android/kotlin/ShipFast).

We have used a different color scheme in each version of the app so you can quickly identify which one is running:

* *API_KEY_PROTECTION* - blue
* *HMAC_STATIC_SECRET_PROTECTION* - orange
* *HMAC_DYNAMIC_SECRET_PROTECTION* - red
* *APPROOV_APP_AUTH_PROTECTION* - green

The colors do not have any special meaning, but obviously, green is the best.

### ShipRaider Web Interface

The rogue web service, ShipRaider, was setup by an evil pirate to help ShipFast drivers take advantage of ShipFast customers gratuities.
The code can be found in the [server/shipraider-rogue-web](/server/shipraider-rogue-web) folder.

Each version of the website is served from a different domain:

* *API_KEY_PROTECTION* - https://api-key.shipraider.demo.approov.io
* *HMAC_STATIC_SECRET_PROTECTION* - https://static-hmac.shipraider.demo.approov.io
* *HMAC_DYNAMIC_SECRET_PROTECTION* - https://dynamic-hmac.shipraider.demo.approov.io
* *APPROOV_APP_AUTH_PROTECTION* - https://approov.shipraider.demo.approov.io

The ShipRaider website follows the same color scheme as the mobile apps to differentiate between versions.

## Code Links

Below we give a brief overview of the techniques used in the blog series to lock down the API with links to the relevant lines of code and the associated blog post.

### API Key

The most common method used by developers to identify **what** is making a request to the API server is to use a long string in the request header, most often called an `Api-Key`, [see the first blog post](https://blog.approov.io/practical-api-security-walkthrough-part-1).

API keys are very simple to implement in both the server and the client. [This app code](/app/android/kotlin/ShipFast/app/src/main/java/com/criticalblue/shipfast/api/RestAPI.kt#L211) adds the key to every request and the server validates the request with a simple header check, as shown by [this code](/server/shipfast-api/api/middleware/api-key.js#L28).

#### The First Attack

Unfortunately, bypassing the API Key protection is also easy, as it is a secret communicated on every request. The [second blog](https://blog.approov.io/practical-api-security-walkthrough-part-2) in the series starts off by showing how to extract the API key with a MitM(Man in the Middle) attack. The key is then [added to the Shipraider website](/server/shipraider-rogue-web/views/pages/index.ejs#L27) to be [used](/server/shipraider-rogue-web/public/js/shipraider.js#L51) in the requests it makes to the ShipFast API.

### Static HMAC

To improve protection, the [second blog post](https://blog.approov.io/practical-api-security-walkthrough-part-2) introduces an HMAC to digitally sign API requests and therefore prevent them from being hijacked or tampered. It is better than an API Key as the *secret part* is never explicitly sent from the client to the server and in this version it is statically embedded in the code.

The HMAC implementation is a little more elaborate than the API key implementation, but it's still simple. You can check [this code](/server/shipfast-api/api/middleware/static-hmac.js#L15) for the API server implementation, and [this code](/app/android/kotlin/ShipFast/app/src/main/java/com/criticalblue/shipfast/api/RestAPI.kt#L252) for the mobile app implementation.

#### The Second Attack

However, if the HMAC secret is hard-coded, then it is still easy for an attacker to extract. The [third blog post](https://blog.approov.io/practical-api-security-walkthrough-part-3) demonstrates this by using open source binary analysis tools to reveal the HMAC secret and the associated algorithm used to sign the requests. Once these are copied across to the [ShipRaider code](/server/shipraider-rogue-web/public/js/shipraider.js#L269) the rogue website can get up and running again.

### Dynamic HMAC

The second attack scenario revealed that using a static secret for the HMAC algorithm is a weak point. The next defense is to use a dynamic secret; one that is computed at runtime. The [third blog post](https://blog.approov.io/practical-api-security-walkthrough-part-3) explains how to combine a static secret with dynamic data to yield a dynamic secret with which to initialize the HMAC algorithm.

The implementation for the mobile app can be seen in these [lines of code](/app/android/kotlin/ShipFast/app/src/main/java/com/criticalblue/shipfast/api/RestAPI.kt#L259) while the API server equivalent can be seen [here](server/shipfast-api/api/middleware/dynamic-hmac.js#L16).

#### The Third Attack

Computing the HMAC secret at runtime makes it harder to bypass but not impossible. The attacker now needs to understand a larger section of code in order to reproduce the behavior in the ShipRaider website. The [fourth blog post](https://blog.approov.io/practical-api-security-walkthrough-part-4) lists several approaches for this, giving a more detailed example using app repackaging and the Android Studio debugger. Again, the attacker can write [equivalent code](server/shipraider-rogue-web/public/js/shipraider.js#L269) in ShipRaider to continue using the ShipFast API.

### Approov Mobile App Attestation

The [fourth blog post](https://blog.approov.io/practical-api-security-walkthrough-part-4), introduces the final security measure in the series. Mobile app attestation is the API security concept implemented in Approov. In a nutshell, Approov checks the whole app and the environment in which it runs before enabling access to the API - *the App is the key*. It gives you a high degree of confidence that your API accesses are locked-down to legitimate instances of your app. This approach is described in more detail in our [product overview](https://approov.io/product) page and in the associated [white paper](https://approov.io/download/Approov-Whitepaper-Security-Trust-Gap.pdf).

The Approov integration is as simple as it can be for mobile app developers. Add the [Approoov SDK](https://approov.io/docs/latest/approov-usage-documentation/#sdk-integration) to your build, hopefully using one of the [quickstart integration examples]](https://approov.io/docs/latest/approov-integration-examples/mobile-app/) to speed up the process and then call the SDK to obtain an Approov token to include on API requests. You can see this in the ShipFast app in [ShipFastApp.kt](/app/android/kotlin/ShipFast/app/src/main/java/com/criticalblue/shipfast/ShipFastApp.kt), search for the lines that are preceded by `// *** UNCOMMENT THE CODE BELOW FOR APPROOV ***`.

The API server integration is also simple: use one of the many [JWT libraries](https://jwt.io/) to verify the Approov token before responding to API requests. The Shipfast API uses the [express-jwt](https://www.npmjs.com/package/express-jwt) node package to verify the Approov token with the [`checkApproovToken`](/server/shipfast-api/api/approov/approov-token-check.js#L129) callback.

## Advanced Usage

The [Advanced Usage](/docs/ADVANCED_USAGE.md) document describes the build and deployment steps for each of the components that make up the ShipFast and ShipRaider services. To follow the blog series, it is normally sufficient to use the services and apps deployed and maintained by the Approov team, in which case you don't need to follow that document. However, you will need it if you attempt the optional pentesting challenge, described at the end of the [last blog post](https://blog.approov.io/practical-api-security-walkthrough-part-4).

## Useful Links

The blog series, as a whole, shows a gradual improvement in API security by ensuring that requests only come from legitimate sources. The blogs and the code in this repository are used to show how to easily circumvent some protection mechanisms that are commonly used in API development. It culminates in an Approov integration which gives the highest degree of confidence in the verified requests received by the ShipFast API. If you wish to explore the Approov solution in more depth then why not try one of the following links as a jumping off point:

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
