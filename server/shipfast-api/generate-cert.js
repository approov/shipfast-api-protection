/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        generate-cert.js
 * Original:    Created on 24 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 * 
 * A utility for generating self-signed certificates for use in ShipFast.
 *****************************************************************************/

const selfsigned = require('selfsigned')
const fs = require('fs')
const exec = require('child_process').exec

const HOST_NAME = '10.0.2.2'
const KEY_FILE = HOST_NAME + ".key"
const CERT_FILE = HOST_NAME + ".pem"

var attrs = [{
    name: 'commonName',
    value: HOST_NAME,
    name: 'subjectAltName',
    value: HOST_NAME + ",localhost,127.0.0.1"
}]
selfsigned.generate(attrs, {
    days: 365
}, callback)

function callback(error, pems) {
    if (error) {
        console.log("Failed to generate certificate and keypair: " + error)
    }
    else {
        fs.writeFile(KEY_FILE, pems.private, 'utf8')
        console.log("Generated private key in " + KEY_FILE)
        
        fs.writeFile(CERT_FILE, pems.cert, 'utf8')
        console.log("Generated certificate in " + CERT_FILE)

        exec("openssl x509 -inform PEM -outform DER -in " + CERT_FILE + " -out " + HOST_NAME + ".crt",
                function (error, stdout, stderr) {
            if (error) {
                console.log("Failed to convert certificate: " + error)
            }
            else {
                console.log("Converted PEM certificate to DER certificate")
            }
        })
    }
}
