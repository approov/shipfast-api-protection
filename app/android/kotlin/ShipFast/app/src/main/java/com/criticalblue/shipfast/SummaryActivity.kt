/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        SummaryActivity.kt
 * Original:    Created on 2 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The shipment summary activity.
 *****************************************************************************/

package com.criticalblue.shipfast

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import com.criticalblue.shipfast.api.RestAPI
import com.criticalblue.shipfast.config.API_REQUEST_ATTEMPTS
import com.criticalblue.shipfast.config.API_REQUEST_RETRY_SLEEP_MILLESECONDS
import com.criticalblue.shipfast.dto.Shipment
import com.criticalblue.shipfast.utils.ViewShow


/**
 * The Summary activity class.
 */
class SummaryActivity : AppCompatActivity() {

    /** The progress bar */
    private lateinit var updateSummaryProgressBar: ProgressBar

    /** The 'delivered shipments' list view */
    private lateinit var deliveredShipmentsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_summary)
        title = "Delivered Shipments"

        updateSummaryProgressBar = findViewById(R.id.updateSummaryProgressBar)
        deliveredShipmentsListView = findViewById(R.id.deliveredShipmentsListView)
        updateDeliveredShipments(API_REQUEST_ATTEMPTS)
    }

    /**
     * Update the current delivered shipments list view by requesting data from the server.
     */
    private fun updateDeliveredShipments(remainingRetries: Int) {

        if (remainingRetries <= 0) {

            stopProgress()

            runOnUiThread {
                ViewShow.warning(findViewById(R.id.shipmentState), "Unable to fetch the delivered shipments.")
            }

            return
        } else {
            Thread.sleep(API_REQUEST_RETRY_SLEEP_MILLESECONDS.toLong())
        }

        startProgress()

        RestAPI.requestDeliveredShipments(this@SummaryActivity) { shipmentsResponse ->

            stopProgress()

            if (shipmentsResponse.isOk()) {

                if (shipmentsResponse.hasNoData()) {
                    runOnUiThread {
                        ViewShow.warning(findViewById(R.id.shipmentState), "No Delivered Shipments Available!")
                    }
                }

                runOnUiThread {
                    deliveredShipmentsListView.adapter = DeliveredShipmentsAdapter(this@SummaryActivity,
                            R.layout.listview_shipment, shipmentsResponse.get())
                }

                return@requestDeliveredShipments
            }

            if (shipmentsResponse.hasApproovTransientError()) {
                Log.i(TAG, shipmentsResponse.errorMessage())
                runOnUiThread {
                    ViewShow.error(findViewById(R.id.shipmentState), shipmentsResponse.errorMessage())
                }
                updateDeliveredShipments(remainingRetries - 1)
                return@requestDeliveredShipments
            }

            if (shipmentsResponse.hasApproovFatalError()) {
                Log.i(TAG, shipmentsResponse.errorMessage())
                runOnUiThread {
                    ViewShow.error(findViewById(R.id.shipmentState), shipmentsResponse.errorMessage())
                }
                return@requestDeliveredShipments
            }

            if (shipmentsResponse.isNotOk()) {
                Log.i(TAG, shipmentsResponse.errorMessage())
                runOnUiThread {
                    ViewShow.info(findViewById(R.id.shipmentState), "Retrying to fetch the delivered shipments.")
                }
                updateDeliveredShipments(remainingRetries - 1)
                return@requestDeliveredShipments
            }
        }
    }

    /**
     * Start showing progress.
     */
    private fun startProgress() {
        runOnUiThread {
            updateSummaryProgressBar.visibility = View.VISIBLE
        }
    }

    /**
     * Stop showing progress.
     */
    private fun stopProgress() {
        runOnUiThread {
            updateSummaryProgressBar.visibility = View.INVISIBLE
        }
    }
}

/**
 * The adapter for the Delivered Shipments list view.
 */
class DeliveredShipmentsAdapter(context: Context, resource: Int, private val shipments: List<Shipment>)
    : ArrayAdapter<Shipment>(context, resource, shipments)  {

    override fun getView(position: Int, convertView: android.view.View?, parent: ViewGroup): android.view.View {

        // Get the shipment for the list view row index
        val shipment = shipments[position]

        // Get the various list view row views
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.listview_shipment, parent, false)
        val descriptionTextView = rowView.findViewById<TextView>(R.id.delShipDescriptionTextView)
        val gratuityTextView = rowView.findViewById<TextView>(R.id.delShipGratuityTextView)
        val pickupTextView = rowView.findViewById<TextView>(R.id.delShipPickupTextView)
        val deliverTextView = rowView.findViewById<TextView>(R.id.delShipDeliverTextView)

        // Update the text for the list view row views
        descriptionTextView.text = shipment.description
        gratuityTextView.text = "${shipment.gratuity}"
        pickupTextView.text = shipment.pickupName
        deliverTextView.text = shipment.deliveryName

        return rowView
    }
}