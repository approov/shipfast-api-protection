/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        rogue-web-server.js
 * Original:    Created on 6 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The rogue ShipFast 'ShipRaider' web server.
 *****************************************************************************/

const express = require('express')
const app = express()
const cors = require('cors')
const https = require('https')
const fs = require('fs')
const config = require('./demo-configuration').config
const chalk = require('chalk')

// Auto detection of colour support does not work always, thus we need to
// enforce it to support 256 colors.
const ctx = new chalk.Instance({level: 2})
const info = ctx.bold.blue

app.use(express.static('public'))

app.use(cors())
app.options('*', cors())

app.set('view engine', 'ejs')
app.set('views', './views')

app.get('/', function(req, res) {
  res.render('pages/index', {
    CURRENT_DEMO_STAGE_NAME: config.CURRENT_DEMO_STAGE_NAME,
    BOOTSTRAP_COLOR_CLASS: config.BOOTSTRAP_COLOR_CLASS,
    SHIPFAST_API_BASE_URL: config.SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL + "://" + config.SHIPFAST_PUBLIC_DOMAIN,
    SHIPFAST_API_DEMO_STAGE_URL: config.SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL + "://" + config.SHIPFAST_PUBLIC_DOMAIN + "/" + config.SHIPFAST_API_VERSION,
    SHIPFAST_API_VERSION: config.SHIPFAST_API_VERSION,
    DEMO_STAGES: config.DEMO_STAGES,
    CURRENT_DEMO_STAGE: config.CURRENT_DEMO_STAGE,
    API_KEY: config.SHIPFAST_API_KEY,
    DRIVER_LATITUDE: config.DRIVER_LATITUDE,
    DRIVER_LONGITUDE: config.DRIVER_LONGITUDE,
    AUTH0_DOMAIN: config.AUTH0_DOMAIN,
    AUTH0_CLIENT_ID: config.AUTH0_CLIENT_ID,
    HMAC_SECRET: config.SHIPFAST_API_HMAC_SECRET
  })
})

if (config.SHIPRAIDER_HTTP_PROTOCOL === "https") {

    console.log("Host Name: " + config.SHIPRAIDER_SERVER_HOSTNAME)

  // Load the certificatie and key data for our server to be hosted over HTTPS
  let serverOptions = {
    key: fs.readFileSync(config.NODE_SSL_DIR + config.SHIPRAIDER_SERVER_HOSTNAME + ".key"),
    cert: fs.readFileSync(config.NODE_SSL_DIR + config.SHIPRAIDER_SERVER_HOSTNAME + ".pem"),
    requestCert: false,
    rejectUnauthorized: false
  }

  // Create and run the HTTPS server
  https.createServer(serverOptions, app).listen(HTTPS_PORT, function() {
    const URL = config.SHIPRAIDER_HTTP_PROTOCOL + '://' + config.SHIPRAIDER_SERVER_HOSTNAME + ":" + config.SHIPRAIDER_HTTPS_PORT
    console.log(info("\nSecure ShipRaider Rogue Web server listening on " + URL + "\n"))
  })

} else {
    app.listen(config.SHIPRAIDER_HTTP_PORT, function () {
      const URL = config.SHIPRAIDER_HTTP_PROTOCOL + '://' + config.SHIPRAIDER_SERVER_HOSTNAME + ":" + config.SHIPRAIDER_HTTP_PORT
      console.log(info("\nShipRaider server ready on " + URL + "\n"))
    })
}
