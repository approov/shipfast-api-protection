const chalk = require('chalk')

// Auto detection of colour support does not work always, thus we need to
// enforce it to support 256 colors.
const ctx = new chalk.Instance({level: 2})

const success = function(message) {
    console.log(ctx.keyword('green').bold(message))
}

const error = function(message) {
    console.log(ctx.keyword('red')(message))
}

const fatalError = function(message) {
    console.log(ctx.keyword('red').bold(message))
}

const warning = function(message) {
    console.log(ctx.keyword('orange')(message))
}

const info = function(message) {
    console.log(ctx.keyword('lightblue')(message))
}

const debug = function(message) {
    console.debug(message)
}

const raw = function(message) {
    console.log(message)
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
