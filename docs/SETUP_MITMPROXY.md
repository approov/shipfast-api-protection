# SETUP THE MITMPROXY

We will learn how to setup the [mitmproxy](https://mitmproxy.org/) to intercept the traffic between the ShipFast mobile app and ShipFast API server, so that we can use it in [part 2](https://blog.approov.io/practical-api-security-walkthrough-part-2) and [part 3](https://blog.approov.io/practical-api-security-walkthrough-part-3) of the ShipFast API Protection series.

## START MITMPROXY

```
./shipfast proxy -p 8000
```

A window will open, meaning your proxy is ready to start listening on incoming requests.

During this launch a set of mitmproxy certiciates have been generated:

```
./shipfast shell editor "ls -al /home/developer/.mitmproxy"
```

Output will look like:

```
total 36
drwxrwxr-x 2 developer developer 4096 Mar 24 12:52 .
drwxr-xr-x 1 developer developer 4096 Mar 24 16:43 ..
-rw-r--r-- 1 developer developer   31 Mar 24 15:59 command_history
-rw-r--r-- 1 developer developer 1318 Mar 24 12:28 mitmproxy-ca-cert.cer
-rw-r--r-- 1 developer developer 1140 Mar 24 12:28 mitmproxy-ca-cert.p12
-rw-r--r-- 1 developer developer 1318 Mar 24 12:28 mitmproxy-ca-cert.pem
-rw------- 1 developer developer 2529 Mar 24 12:28 mitmproxy-ca.p12
-rw------- 1 developer developer 3022 Mar 24 12:28 mitmproxy-ca.pem
-rw-r--r-- 1 developer developer  770 Mar 24 12:28 mitmproxy-dhparam.pem
```

The certificate `mitmproxy-ca.pem` will be used in the last step to add it into the User Trusted store in the the Android device.

## SETUP PROXY IN ANDROID

When using the emulator you can click in the **...** at the bottom of the side bar and then click on **Settings** and select the **Proxy** tab. Now set the host name to `localhost` and the port to `8000`.

## SETUP MITMPROXY CERTIFICATE IN ANDROID

## Enable the Network Security Config File

From Android API level 24 onwards in order to use self signed certificates we need to explicitly opt-in, and this is done in the `network_security_config.xml` file. To load this file just go to the `manifest.xml` and look for this:

```xml
<!-- MITMPROXY: ALLOW SELF SIGNED CERTIFICATE -->
<!-- Uncomment and move the next line to right after <application -->
<!-- android:networkSecurityConfig="@xml/network_security_config"-->
<application
    android:allowBackup="true"
```

and make it look like this:

```xml
<!-- MITMPROXY: ALLOW SELF SIGNED CERTIFICATE -->
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:allowBackup="true"
```

### Setup the Screen Lock

In order to be possible to add a user certificate to the trusted store it's required to have the Screen Lock enabled.

Going to **Settings > Security > Screen Lock** we chooose the option **PIN** and setup it.

### Download the Certificate

Afterwards we need to open the browser in the Android device and navigate to `http://mitm.it`, where we will click on the logo for Android in order to download the certificate. Android will show a popup refusing to install the certificate, saying that we need to do it through the **Settings**.

### Install the Certificate

Open **Settings > Security > Encryption & credentials > Install a certificate > CA certificate**, click in **Install anyways**, enter your screen lock pin, select the previous downloaded certificate and Android will now install it for you.

### Verify Certificate Installation

Go to **Settings > Security > Encryption & credentials > Trusted credentials**, click on the tab **User** and you should see it listed.
