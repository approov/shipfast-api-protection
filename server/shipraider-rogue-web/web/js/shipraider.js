/*****************************************************************************
 * Project:     ShipRaider API Protection (Server)
 * File:        shipraider.js
 * Original:    Created on 6 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The rogue ShipFast 'ShipRaider' web server's JQuery logic script.
 *****************************************************************************/

// TOOD: Auto generate this file when in a docker container in order to the .env values

//The enumeration of various stages of the demo.
const DEMO_STAGE = {
    // The demo which uses basic protection by way of API key specified in the app manifest
    API_KEY_PROTECTION: 0,
    // The demo which introduces API request signing by HMAC using a static secret in code
    HMAC_STATIC_SECRET_PROTECTION: 1,
    // The demo which introduces API request signing by HMAC using a dynamic secret in code
    HMAC_DYNAMIC_SECRET_PROTECTION: 2
}

var shipments = {}

$("#send-bitcoin-button").click(function() {
    alert("Pay me the money!")
})

var lock = new Auth0Lock(AUTH0_CLIENT_ID, AUTH0_DOMAIN, {
    auth: {
        redirectUrl: window.url,
        responseType: "id_token",
        redirect: true
    },
    oidcConformant: false,
    autoclose: true,
    allowSignUp: false,
    theme: {
        logo: 'images/auth0lock.png'
    }
})
lock.on("authenticated", function(authResult) {
    $("#user-auth-token-input").val(authResult.idToken)
})

$("#login-button").click(function() {
    lock.show()
})

$("#search-shipments-button").click(function(event) {
    event.preventDefault()
    searchForShipments()
    updateProgressBar(100)
})

function searchForShipments() {
    updateProgressBar(0)
    shipments = {}

    var shipFastServerURL = $("#server-url-input").val()
    var shipFastAPIKey = $("#shipfast-api-key-input").val()
    var userAuthToken = $("#user-auth-token-input").val()
    var driver_latitude = $("#location-latitude-input").val()
    var driver_longitude = $("#location-longitude-input").val()
    var locationSweepRadius = $("#location-sweep-radius-input").val()
    var locationSweepStep = $("#location-sweep-step-input").val()

    var halfLBR = parseFloat(locationSweepRadius) / 2.0
    var locStep = parseFloat(locationSweepStep)
    var latStart = parseFloat(driver_latitude) - halfLBR
    var latEnd = parseFloat(driver_latitude) + halfLBR
    var lonStart = parseFloat(driver_longitude) - halfLBR
    var lonEnd = parseFloat(driver_longitude) + halfLBR

    var fetchNearestShipment = function(latVal, lonVal) {
        var url = shipFastServerURL + "/shipments/nearest_shipment"
        var auth = "Bearer " + userAuthToken

        $.ajax({
            url: url,
            headers: {
                "API-KEY" : shipFastAPIKey,
                "Authorization" : auth,
                "HMAC" : computeHMAC(url, auth),
                "DRIVER-LATITUDE" : latVal.toString(),
                "DRIVER-LONGITUDE" : lonVal.toString()
            },
            method: "GET",
            timeout: 5000,
            success: function(json) {
                var shipmentID = json["id"]
                var shipmentPickupLatitude = json["pickupLatitude"]
                var shipmentPickupLongitude = json["pickupLongitude"]
                shipments[shipmentID] = json
                addShipmentsToResults()
                updateProgressBar(Math.min(Math.round((progress / totalProgress) * 100), 100))
                progress++
            }
        })
    }

    fetchNearestShipment(parseFloat(driver_latitude), parseFloat(driver_longitude))

    var progress = 0
    var count = 0
    var totalProgress = Math.pow(parseFloat(locationSweepRadius) / locStep, 2)

    for (var lat = latStart; lat <= latEnd; lat += locStep) {
        for (var lon = lonStart; lon <= lonEnd; lon += locStep) {
            if (count++ > 25) {
                totalProgress = 1
                return
            }
            fetchNearestShipment(driver_latitude, lon)
        }
    }
}

function updateProgressBar(progress) {
    $(".progress-bar").attr("aria-valuenow", progress).attr("style", "width: " + progress + "%")
    $("#progress-bar-text").text(progress + "% Complete")
}

function addShipmentsToResults() {
    var resultsTableBody = $("#results-table-body")
    resultsTableBody.empty()
    Object.entries(shipments).forEach(
        ([shipmentID, json]) => {
            var shipmentID = json["id"]
            var shipmentName = json["description"]
            var shipmentGratuity = json["gratuity"]
            var shipmentPickup = json["pickupName"]
            var shipmentPickupDistance = json["pickupDistance"]
            var shipmentDelivery = json["deliveryName"]
            var grabShipmentButton = "<button type='button' class='btn btn-default' id='shipment-" + shipmentID + "'>Grab It!</button>"
            resultsTableBody.append(
                  "<tr>"
                + "<th scope='row'>" + shipmentID + "</th>"
                + "<td>" + shipmentName + "</td>"
                + "<td>" + shipmentGratuity + "</td>"
                + "<td>" + shipmentPickup + "</td>"
                + "<td>" + shipmentPickupDistance + "</td>"
                + "<td>" + shipmentDelivery + "</td>"
                + "<td>" + grabShipmentButton + "</td>"
                + "</tr>")

            $("#shipment-" + shipmentID).click(function(event) {
                event.preventDefault()
                grabShipment(shipmentID)
            })
        }
    )
}

function grabShipment(shipmentID) {
    var shipFastServerURL = $("#server-url-input").val()
    var shipFastAPIKey = $("#shipfast-api-key-input").val()
    var userAuthToken = $("#user-auth-token-input").val()
    var driver_latitude = $("#location-latitude-input").val()
    var driver_longitude = $("#location-longitude-input").val()

    var url = shipFastServerURL + "/shipments/update_state/" + shipmentID
    var auth = "Bearer " + userAuthToken
    $.ajax({
        url: url,
        headers: {
            "API-KEY" : shipFastAPIKey,
            "Authorization" : auth,
            "HMAC" : computeHMAC(url, auth),
            "DRIVER-LATITUDE" : driver_latitude,
            "DRIVER-LONGITUDE" : driver_longitude,
            "SHIPMENT-STATE" : "1"
        },
        method: "POST",
        timeout: 5000,
        success: function(json) {
            alert("You got shipment ID" + shipmentID + " - check the app and enjoy the extra cash!\n\n@crackmaapi - don't forget da bitcoin pls")
            searchForShipments()
        },
        error: function(xhr) {
            alert("Man, it didn't work this time!")
        }
    })
}

function computeHMAC(url, idToken) {
    if (currentDemoStage == DEMO_STAGE.HMAC_STATIC_SECRET_PROTECTION
            || currentDemoStage == DEMO_STAGE.HMAC_DYNAMIC_SECRET_PROTECTION)  {
        var hmacSecret
        if (currentDemoStage == DEMO_STAGE.HMAC_STATIC_SECRET_PROTECTION) {
            // Just use the static secret in the HMAC for this demo stage
            hmacSecret = HMAC_SECRET
        }
        else if (currentDemoStage == DEMO_STAGE.HMAC_DYNAMIC_SECRET_PROTECTION) {
            // Obfuscate the static secret to produce a dynamic secret to
            // use in the HMAC for this demo stage
            var staticSecret = HMAC_SECRET
            var dynamicSecret = CryptoJS.enc.Base64.parse(staticSecret)
            var shipFastAPIKey = CryptoJS.enc.Utf8.parse($("#shipfast-api-key-input").val())
            for (var i = 0; i < Math.min(dynamicSecret.words.length, shipFastAPIKey.words.length); i++) {
                dynamicSecret.words[i] ^= shipFastAPIKey.words[i]
            }
            dynamicSecret = CryptoJS.enc.Base64.stringify(dynamicSecret)
            hmacSecret = dynamicSecret
        }

        if (hmacSecret) {
            var parser = document.createElement('a')
            parser.href = url
            var msg = parser.protocol.substring(0, parser.protocol.length - 1)
                + parser.hostname + parser.pathname + idToken
            var hmac = CryptoJS.HmacSHA256(msg, CryptoJS.enc.Base64.parse(hmacSecret)).toString(CryptoJS.enc.Hex)
            return hmac
        }
    }
    return null
}
