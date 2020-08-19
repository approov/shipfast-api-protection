const model = require('./model')
const log = require('./utils/logging')
const express = require('express')
const router = express.Router()
const request = require('./utils/request')
const response = require('./utils/response')

router.post('/admin/:version/cache/delete', function(req, res) {
  try {
    const user_uid = request.hash_user_claim_by_api_version(req)
    const log_id = request.log_simple_identifier(user_uid, req.params.version + ': admin.js')

    if (!model.cache_has(user_uid)) {
      const message = "Cache is missing an entry for the key"
      log.warning(message + ": " + user_uid, log_id)
      res.status(400).json(response.bad_request(log_id, message))
      return
    }

    result = model.cache_delete(user_uid)

    if (result > 0) {
      const message = "Deleted the cache key"
      log.success(message + ": " + user_uid, log_id)
      res.status(200).json({success: message})
      return
    }

    const message="Failed to delete the cache"

    log.error(message + ": " + user_uid, log_id)
    res.status(400).json(response.bad_request(log_id, message))

  } catch(error) {
    log.error(error.message, req.params.version + ': admin.js')
    res.status(500).json(response.failed_request(log_id))
  }
})

module.exports = router
