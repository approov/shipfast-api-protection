const bad_request = function(log_identifier, message = "Bad Request!") {
  return {error: message, request_id: log_identifier.request_id}
}

const invalid_request = function(log_identifier, message = "Invalid Request!") {
  return {error: message, request_id: log_identifier.request_id}
}

const failed_request = function(log_identifier, message = "Unknown error!!!") {
  return {error: message, request_id: log_identifier.request_id}
}

module.exports = {
  bad_request,
  invalid_request,
  failed_request
}
