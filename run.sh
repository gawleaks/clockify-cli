#!/usr/bin/env bash
set -euo pipefail

JAR_NAME="clockify-cli-standalone.jar"
INSTALL_DIR="$HOME/.clockify"

java -jar "$INSTALL_DIR/$JAR_NAME" "$@"
