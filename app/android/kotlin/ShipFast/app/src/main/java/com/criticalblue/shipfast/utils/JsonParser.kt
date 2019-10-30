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