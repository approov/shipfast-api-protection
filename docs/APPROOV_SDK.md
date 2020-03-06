# APPROOV SDK


## APPROOV SDK INTEGRATION

Following [this](https://approov.io/docs/v2.0/approov-usage-documentation/#sdk-integration) instructions in the Approov docs.

#### Download the Approov SDK library with:

```
approov sdk -getLibrary app/android/kotlin/ShipFast/approov/approov.aar
```

#### Build Gradle

Open the file `app/android/kotlin/Shipfast/app/build.gradle`.

Add on the `android` section:

```
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

Add on the `dependencies` section:

```
implementation 'com.squareup.okhttp3:okhttp:3.14.2'
```

#### Manifest File

Add to to file `app/android/kotlin/Shipfast/app/src/main/AndroidManifest.xml`:

```
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.INTERNET" />
```

## APPROOV SDK CONFIGURATION

Following [this](https://approov.io/docs/v2.0/approov-usage-documentation/#sdk-configuration) instructions in the Approov docs.

#### Download the initial config for the Approov SDK library with:

```
approov sdk -getConfig app/src/main/approov-initial.config
```

## APPROOV SDK INITIALIZATION

Following [this](https://approov.io/docs/v2.0/approov-usage-documentation/#sdk-initialization) instructions in the Approov docs.

