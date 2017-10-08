/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        shipfaster.js
 * Original:    Created on 6 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The rogue ShipFast 'ShipFaster' web server's JQuery logic script.
 *****************************************************************************/

var shipments = {}

 $(document).ready(function() {
    $("body").css({"background-image":"url('images/skull.png')"})
    $(".jumbotron").css({"background-image":"url('images/fire.jpg')",
        "-webkit-background-size":"cover",
        "-moz-background-size:":"cover",
        "-o-background-size:":"cover",
        "background-size:":"cover",
        "color":"yellow"})
    
    $("#server-url-input").val("http://127.0.0.1:3000")
    $("#shipfast-api-key-input").val("QXBwcm9vdidzIHRvdGFsbHkgYXdlc29tZSEh")
    $("#location-latitude-input").val("55.944614")
    $("#location-longitude-input").val("-3.181431")
    $("#location-blast-radius-input").val("15.0")
    $("#location-blast-step-input").val("0.5")
})

$("#send-bitcoin-button").click(function() {
    alert("Pay me the money!")
})

$("#search-shipments-button").click(function(event) {
    event.preventDefault()
    searchForShipments()
})

function searchForShipments() {
    updateProgressBar(0)
    shipments = {}

    var shipFastServerURL = $("#server-url-input").val()
    var shipFastAPIKey = $("#shipfast-api-key-input").val()
    var userAuthToken = $("#user-auth-token").val()
    var latitude = $("#location-latitude-input").val()
    var longitude = $("#location-longitude-input").val()
    var locationBlastRadius = $("#location-blast-radius-input").val()
    var locationBlastStep = $("#location-blast-step-input").val()

    var halfLBR = parseFloat(locationBlastRadius) / 2.0
    var locStep = parseFloat(locationBlastStep)
    var latStart = parseFloat(latitude) - halfLBR
    var latEnd = parseFloat(latitude) + halfLBR
    var lonStart = parseFloat(longitude) - halfLBR
    var lonEnd = parseFloat(longitude) + halfLBR

    var progress = 0
    var totalProgress = Math.pow(parseFloat(locationBlastRadius) / locStep, 2)
    for (lat = latStart; lat <= latEnd; lat += locStep) {
        for (lon = lonStart; lon <= lonEnd; lon += locStep) {
            $.ajax({
                url: shipFastServerURL + "/shipments/nearest_shipment",
                headers: {
                    "SF-API_KEY" : shipFastAPIKey,
                    "Authorization" : "Bearer " + userAuthToken,
                    "SF-Latitude" : lat.toString(),
                    "SF-Longitude" : lon.toString()
                },
                method: "GET",
                success: function(json) {
                    var shipmentID = json["id"]
                    var shipmentPickupLatitude = json["pickupLatitude"]
                    var shipmentPickupLongitude = json["pickupLongitude"]
                    json["pickupDistance"] = Math.round(distanceInMiles(parseFloat(shipmentPickupLatitude), parseFloat(shipmentPickupLongitude),
                        parseFloat(latitude), parseFloat(longitude))).toString()
                    shipments[shipmentID] = json
                    addShipmentsToResults()
                    updateProgressBar(Math.min(Math.round((progress++ / totalProgress) * 100), 100))
                }
            })
        }
    }
}

function updateProgressBar(progress) {
    $(".progress-bar").attr("aria-valuenow", progress).attr("style", "width: " + progress + "%")
    $("#progress-bar-text").text(progress + "% Complete")
}

function distanceInMiles(lat1, lon1, lat2, lon2) {
    var R = 3961
    var dLat = deg2rad(lat2-lat1)
    var dLon = deg2rad(lon2-lon1)
    var a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(dLon/2) * Math.sin(dLon/2)
    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
    var d = R * c;
    return d;
}
  
function deg2rad(deg) {
    return deg * (Math.PI/180)
}

function addShipmentsToResults() {
    var resultsTableBody = $("#results-table-body")
    resultsTableBody.empty()
    Object.entries(shipments).forEach(
        ([shipmentID, json]) => {
            var shipmentID = json["id"]
            var shipmentName = json["description"]
            var shipmentGratuity = "$" + json["gratuity"]
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
                alert("Grabbing shipment " + event.target.id)
            })
        }
    )
}

function grabShipment(shipmentID) {
    var shipFastServerURL = $("#server-url-input").val()
    var shipFastAPIKey = $("#shipfast-api-key-input").val()
    var userAuthToken = $("#user-auth-token").val()
    var latitude = $("#location-latitude-input").val()
    var longitude = $("#location-longitude-input").val()

    $.ajax({
        url: shipFastServerURL + "/shipments/update_state/" + shipmentID,
        headers: {
            "SF-API_KEY" : shipFastAPIKey,
            "Authorization" : "Bearer " + userAuthToken,
            "SF-Latitude" : lat.toString(),
            "SF-Longitude" : lon.toString(),
            "SF-STATE" : "1"
        },
        method: "POST",
        success: function(json) {
            searchForShipments()
        }
    })
}