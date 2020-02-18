const dotenv = require('dotenv').config()

if (dotenv.error) {
  throw dotenv.error
}

const config = {
    AUTH0_DOMAIN: dotenv.parsed.AUTH0_DOMAIN || undefined,
    AUTH0_CLIENT_ID: dotenv.parsed.AUTH0_CLIENT_ID || undefined,
}

let missing_env_vars = ""

Object.entries(config).forEach(([key, value]) => {
    if (value === undefined) {
        missing_env_vars += key + ", "
    }
})

if (missing_env_vars !== "") {
    throw new Error("Missing Env Vars values for: " + missing_env_vars.slice(0, -2)) // removes last comma in the string
}

module.exports = config
