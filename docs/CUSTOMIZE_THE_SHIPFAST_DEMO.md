# CUSTOMIZE THE SHIPFAST DEMO

When presenting the demo we may want to adapt it to the location of the customer, like center the map on its city, and use it's local currency and metric system.

#### Custom location

Get from Google maps the coordinates for your preferred location and set them in the following env vars:

```
DRIVER_LATITUDE=51.535472
DRIVER_LONGITUDE=-0.104971
```

> **NOTE**: After you start the Android emulator you will need to go to settings and them this same coordinates as the default ones for the device.


#### Custom Currency and Metric System

Adjust the following env vars according to your needs:

```
CURRENCY_SYMBOL="£"
DISTANCE_IN_MILES=true
```

All other defaults in the `.env` file are fine to run the demo.
