const chalk = require('chalk')

// Auto detection of colour support does not work always, thus we need to
// enforce it to support 256 colors.
const ctx = new chalk.Instance({level: 2})

const success = function(message, log_identifier) {
    consoleLog(log_identifier, message, 'green')
}

const error = function(message, log_identifier) {
    consoleLog(log_identifier, message, 'red')
}

const fatalError = function(message, log_identifier) {
    consoleLogBold(log_identifier, message, 'red')
}

const warning = function(message, log_identifier) {
    consoleLog(log_identifier, message, 'orange')
}

const info = function(message, log_identifier) {
    consoleLog(log_identifier, message, 'lightblue')
}

const debug = function(message, log_identifier) {
    console.debug("[" + log_identifier + "]", message)
}

const raw = function(message, log_identifier) {
    console.log("[" + log_identifier + "]", message)
}

const consoleLog = function(log_identifier, message, color) {
    console.log("[" + log_identifier + "]", ctx.keyword(color)(message))
}

const consoleLogBold = function(log_identifier, message, color) {
    console.log("[" + log_identifier + "]", ctx.keyword(color).bold(message))
}

module.exports = {
    success,
    error,
    fatalError,
    warning,
    info,
    debug,
    raw,
}
