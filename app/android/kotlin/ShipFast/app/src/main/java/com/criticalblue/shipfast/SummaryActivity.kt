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
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView

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
        updateDeliveredShipments()
    }

    /**
     * Update the current delivered shipments list view by requesting data from the server.
     */
    private fun updateDeliveredShipments() {

        startProgress()
        requestDeliveredShipments(this@SummaryActivity, { _, shipments ->
            stopProgress()
            runOnUiThread {
                deliveredShipmentsListView.adapter = DeliveredShipmentsAdapter(this@SummaryActivity,
                        R.layout.listview_shipment, shipments ?: arrayListOf())
            }
        })
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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

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
        gratuityTextView.text = "£${shipment.gratuity}"
        pickupTextView.text = shipment.pickupName
        deliverTextView.text = shipment.deliveryName

        return rowView
    }
}