/*****************************************************************************
 * Project:     ShipFast API Protection (App)
 * File:        SummaryActivity.kt
 * Original:    Created on 2 Oct 2017 by Simon Rigg
 * Copyright(c) 2002 - 2017 by CriticalBlue Ltd.
 *
 * The shipment summary activity.
 *****************************************************************************/

package com.criticalblue.shipfast

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ProgressBar

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
                deliveredShipmentsListView.adapter = ArrayAdapter(this@SummaryActivity, R.layout.listview_shipment, shipments)
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
