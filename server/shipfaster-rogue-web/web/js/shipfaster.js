/*****************************************************************************
 * Project:     ShipFast API Protection (Server)
 * File:        shipfaster.js
 * Original:    Created on 6 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The rogue ShipFast 'ShipFaster' web server's JQuery logic script.
 *****************************************************************************/

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
    $("#location-blast-step-value-input").val("0.5")
})

$("#send-bitcoin-button").click(function() {
    alert("Pay me the money!")
})

var shipments = {}

$("#search-shipments-button").click(function(event) {
    event.preventDefault()
    shipments = {}

    var shipFastServerURL = $("#server-url-input").val()
    var shipFastAPIKey = $("#shipfast-api-key-input").val()
    var userAuthToken = $("#user-auth-token").val()
    var latitude = $("#location-latitude-input").val()
    var longitude = $("#location-longitude-input").val()
    var locationBlastRadius = $("#location-blast-radius-input").val()
    var locationBlastStepValue = $("#location-blast-step-value-input").val()

    // alert("ShipFast Server URL: " + shipFastServerURL
    //     + "\nShipFast API Key: " + shipFastAPIKey
    //     + "\nLocation: " + latitude + "," + longitude + "\nBlast Radius: "
    //     + locationBlastRadius)

    var halfLBR = parseFloat(locationBlastRadius) / 2.0
    var locStep = parseFloat(locationBlastStepValue)
    var latStart = parseFloat(latitude) - halfLBR
    var latEnd = parseFloat(latitude) + halfLBR
    var lonStart = parseFloat(longitude) - halfLBR
    var lonEnd = parseFloat(longitude) + halfLBR

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
                    shipments[shipmentID] = json
                    addShipmentsToResults()
                }
            })
        }
    }
    
    // addShipmentsToResults()
})

function addShipmentsToResults() {
    var resultsTableBody = $("#results-table-body")
    resultsTableBody.empty()
    Object.entries(shipments).forEach(
        ([shipmentID, json]) => {
            var shipmentID = json["id"]
            var shipmentName = json["description"]
            var shipmentGratuity = "$" + json["gratuity"]
            var shipmentPickup = json["pickupName"]
            var shipmentDelivery = json["deliveryName"]
            var grabShipmentButton = "<button type='button' class='btn btn-default' id='shipment-" + shipmentID + "'>Grab It!</button>"
            resultsTableBody.append(
                  "<tr>"
                + "<th scope='row'>" + shipmentID + "</th>"
                + "<td>" + shipmentName + "</td>"
                + "<td>" + shipmentGratuity + "</td>"
                + "<td>" + shipmentPickup + "</td>"
                + "<td>" + shipmentDelivery + "</td>"
                + "<td>" + grabShipmentButton + "</td>"
                + "</tr>")
        
            $("#shipment-" + shipmentID).click(function(event) {
                event.preventDefault()
                alert("Grabbing shipment " + event.target.id)
            })
        }
    )
}
