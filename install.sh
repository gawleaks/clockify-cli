#!/usr/bin/env bash
set -euo pipefail

PWD=$(pwd)
LOCAL_JAR_FILE=$PWD/target/uberjar/clockify-cli-standalone.jar
INSTALL_DIR=$HOME/.clockify


echo 'Checking requirements...'

if ! java -version >/dev/null; then
	echo "WARNING: Java is not available? Please install Java and try again."
fi

echo "Installing clockify-cli..."

if [ -f "$LOCAL_JAR_FILE" ]; then
	echo "Local uberjar exists, skipping pull from remote."
	install "$LOCAL_JAR_FILE" "$INSTALL_DIR"
else
	echo "$LOCAL_JAR_FILE does not exist, pulling latest release from Github."
	#curl -L https://github.com/platogo/atoss-cli/releases/latest/download/atoss-cli-standalone.jar >atoss-cli-standalone.jar
	#install clockify-cli-standalone.jar $INSTALL_DIR
fi

echo "Installing wrapper..."

cp -f "$PWD/run.sh" "${INSTALL_DIR}/clockify/clockify-cli"

echo "Done! You can try running `clockify-cli --help` now."