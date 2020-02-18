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
const jquery = require('jquery')
const cors = require('cors')
const serverPath = __dirname + '/'
const https = require('https')
const fs = require('fs')
const config = require('./demo-configuration').config
const chalk = require('chalk')

// Auto detection of colour support does not work always, thus we need to
// enforce it to support 256 colors.
const ctx = new chalk.constructor({level: 2})
const error = ctx.bold.red
const warning = ctx.bold.yellow
const info = ctx.bold.blue
const debug = ctx.bold.cyan

// needs to be hex colours, otherwise the contrast in the android terminal is very bad.
const success = ctx.bgHex('#008000').bold.hex('#ffffff')
const fatalError = ctx.bgHex('#ff0000').bold.hex('#ffffff')

app.use(express.static('web'))

app.use(cors())
app.options('*', cors())

app.set('view engine', 'ejs')
app.set('views', './web/views')

app.get('/', function(req, res) {
  res.render('pages/index', {
    SERVER_URL: config.SHIPFAST_PUBLIC_DOMAIN_HTTP_PROTOCOL + "://" + config.SHIPFAST_PUBLIC_DOMAIN,
    API_KEY: config.SHIPFAST_API_KEY,
    DRIVER_LATITUDE: config.DRIVER_LATITUDE,
    DRIVER_LONGITUDE: config.DRIVER_LONGITUDE,
    DEMO_STAGE_API_KEY_PROTECTION: config.SHIPFAST_DEMO_STAGE,
    AUTH0_DOMAIN: config.AUTH0_DOMAIN,
    AUTH0_CLIENT_ID: config.AUTH0_CLIENT_ID,
    HMAC_SECRET: config.SHIPFAST_API_HMAC_SECRET
  })
})

if (config.SHIPRAIDER_HTTP_PROTOCOL === "https") {

    console.log("Host Name: " + config.SHIPRAIDER_SERVER_HOSTNAME)

  // Load the certificate and key data for our server to be hosted over HTTPS
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
