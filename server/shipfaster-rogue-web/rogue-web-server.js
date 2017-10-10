/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        rogue-web-server.js
 * Original:    Created on 6 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The rogue ShipFast 'ShipFaster' web server.
 *****************************************************************************/

const express = require('express')
const app = express()
const jquery = require('jquery')
const cors = require('cors')

const serverPath = __dirname + '/'

app.use(express.static('web'))

app.use('/login', function(req, res) {
  res.send('hi')
})

app.use('*', function(req, res) {
  res.send('Error 404: Not Found!')
})

app.use(cors())

app.listen(80, function () {
  console.log('ShipFaster ready!')
})
