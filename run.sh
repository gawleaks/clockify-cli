#!/usr/bin/env bash
set -euo pipefail

JAR_NAME="clockify-cli-standalone.jar"

java -jar /usr/local/bin/$JAR_NAME "$@"