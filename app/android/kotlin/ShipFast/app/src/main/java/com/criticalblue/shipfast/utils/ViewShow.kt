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

package com.criticalblue.shipfast.utils

import android.graphics.Color
import android.view.View
import com.criticalblue.shipfast.config.SNACKBAR_DISPLAY_MILLESECONDS
import com.google.android.material.snackbar.Snackbar


/**
 * Util object to show alert messages in an Android View.
 */
object ViewShow {

    /**
     * Shows a red bar in the bottom of the screen with the error message and a button to dismiss it.
     *
     * @param view    The Android view where we want to show the error message.
     * @param message The error message to show.
     */
    fun error(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
        snackbar.setAction("DISMISS"){snackbar.dismiss()}
        snackbar.setActionTextColor(Color.WHITE)

        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(Color.RED)
        snackbar.show()
    }

    /**
     * Shows a magenta bar in the bottom of the screen with the warning message.
     *
     * @param view    The Android view where we want to show the warning message.
     * @param message The warning message to show.
     */
    fun warning(view: View, message: String) {
        showAlertMessage(view, message, Color.parseColor("#CF5300"))
    }

    /**
     * Shows a green bar in the bottom of the screen with the warning message.
     *
     * @param view    The Android view where we want to show the success message.
     * @param message The success message to show.
     */
    fun success(view: View, message: String) {
        showAlertMessage(view, message, Color.parseColor("#006400"))
    }

    /**
     * Shows a yellow bar in the bottom of the screen with the warning message.
     *
     * @param view    The Android view where we want to show the info message.
     * @param message The info message to show.
     */
    fun info(view: View, message: String) {
        showAlertMessage(view, message, Color.BLUE)
    }

    /**
     * Shows an alert message in the bottom of the given view by using a snack bar.
     *
     * @param view            The view for the activity where we want to display the message.
     * @param alertMessage    The alert message to be displayed.
     * @param backgroundColor The background color for the snack bar that will display the message.
     */
    private fun showAlertMessage(view: View, alertMessage: String, backgroundColor: Int) {
        val snackbar = Snackbar.make(view, alertMessage, SNACKBAR_DISPLAY_MILLESECONDS)
        val snackbarView = snackbar.view
        snackbarView.setBackgroundColor(backgroundColor)
        snackbar.show()
    }
}
