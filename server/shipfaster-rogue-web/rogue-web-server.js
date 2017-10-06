/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        rogue-web-server.js
 * Original:    Created on 6 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * This rogue ShipFast 'ShipFaster' web server.
 *****************************************************************************/

const express = require('express')
const app = express()
const jquery = require('jquery')

const serverPath = __dirname + '/'

app.use(express.static('web'))

// app.get('/', function(req, res) {

//   res.sendFile(serverPath + 'index.html')
// })

app.use('*', function(req, res) {
  res.send('Error 404: Not Found!')
})

app.listen(80, function () {
  console.log('ShipFaster ready!')
})
