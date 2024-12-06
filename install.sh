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
else
	echo "$LOCAL_JAR_FILE does not exist, pulling latest release from Github."
	curl -L https://github.com/gawliks/clockify-cli/releases/latest/download/clockify-cli-standalone.jar > $LOCAL_JAR_FILE
fi



echo "Installing script..."

mkdir -p ${INSTALL_DIR}
install "$LOCAL_JAR_FILE" "$INSTALL_DIR/clockify-cli-standalone.jar"
cp -f "$PWD/run.sh" "${INSTALL_DIR}/clockify-cli"
chmod +x "${INSTALL_DIR}/clockify-cli"

echo "Done! You can add ${INSTALL_DIR}: (export PATH=\$PATH:$INSTALL_DIR) to your path and run clockify-cli."