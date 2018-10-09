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

const HOST_NAME = config.serverHostName
const HTTP_PORT = config.httpPort
const HTTPS_PORT = config.httpsPort

const URL = config.httpProtocol + '://' + HOST_NAME
const BASE_DIR = config.baseDir

app.use(express.static('web'))

app.use('*', function(req, res) {
  res.send('Error 404: Not Found!')
})

app.use(cors())

if (config.runSecureServer) {

    console.log("Host Name: " + HOST_NAME)

  // Load the certificate and key data for our server to be hosted over HTTPS
  var serverOptions = {
    key: fs.readFileSync(BASE_DIR + "/.ssl/" + HOST_NAME + ".key"),
    cert: fs.readFileSync(BASE_DIR + "/.ssl/" + HOST_NAME + ".pem"),
    requestCert: false,
    rejectUnauthorized: false
  }

  // Create and run the HTTPS server
  https.createServer(serverOptions, app).listen(HTTPS_PORT, function() {
    console.log(info("\nSecure ShipRaider Rogue Web server listening on " + URL + ":" + HTTPS_PORT + "\n"))
  })

} else {
    app.listen(HTTP_PORT, function () {
      console.log(info("\nShipRaider server ready on " + HOST_NAME + ":" + HTTP_PORT + "\n"))
    })
}
