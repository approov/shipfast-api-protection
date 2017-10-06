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

    /** The 'shipped shipments' list view */
    private lateinit var shippedShipmentsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        title = "Shipped Shipments"

        updateSummaryProgressBar = findViewById(R.id.updateSummaryProgressBar)
        shippedShipmentsListView = findViewById(R.id.shippedShipmentsListView)
        updateShippedShipments()
    }

    /**
     * Update the current shipped shipments list view by requesting data from the server.
     */
    private fun updateShippedShipments() {

        startProgress()
        requestShippedShipments(this@SummaryActivity, { _, shipments ->
            stopProgress()
            runOnUiThread {
                shippedShipmentsListView.adapter = ArrayAdapter(this@SummaryActivity, R.layout.listview_shipment, shipments)
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
