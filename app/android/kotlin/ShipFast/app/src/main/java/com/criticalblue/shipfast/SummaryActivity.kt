/*
Copyright (C) 2020 CriticalBlue Ltd.

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.criticalblue.shipfast

import android.content.Context
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
class SummaryActivity : BaseActivity() {

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
