#!/bin/bash

set -eu

touch api-server.js && echo > ~/api.log && tail -f ~/api.log
