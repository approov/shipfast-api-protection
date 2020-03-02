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

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.criticalblue.shipfast.utils

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object JsonParser {
    /**
     * Safely attempt to parse the given string as a JSON object.
     *
     * @param json the JSON string
     * @return the JSON object or null
     */
    fun toJSONObject(json: String?): JSONObject? {
        try {
            return JSONObject(json)
        } catch (e: JSONException) {
            return null
        }
    }

    /**
     * Safely attempt to parse the given string as a JSON array.
     *
     * @param json the JSON string
     * @return the JSON array or null
     */
     fun toJSONArray(json: String?): JSONArray? {
        try {
            return JSONArray(json)
        } catch (e: JSONException) {
            return null
        }
    }
}
