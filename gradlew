#!/bin/sh
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROP_FILE="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"
DIST_URL=$(sed -n 's/^distributionUrl=//p' "$PROP_FILE" | sed 's/\\\\//g')
[ -n "$DIST_URL" ] || { echo "distributionUrl missing"; exit 1; }
DIST_NAME=$(basename "$DIST_URL")
WRAPPER_DIR="${GRADLE_USER_HOME:-$HOME/.gradle}/wrapper/dists/$DIST_NAME"
INSTALL_DIR="$WRAPPER_DIR/dnp360-wrapper"
BIN="$INSTALL_DIR/bin/gradle"
if [ ! -x "$BIN" ]; then
  mkdir -p "$WRAPPER_DIR"
  TMP="$WRAPPER_DIR/$DIST_NAME"
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$DIST_URL" -o "$TMP"; else wget -q "$DIST_URL" -O "$TMP"; fi
  rm -rf "$INSTALL_DIR" "$WRAPPER_DIR/extracted"
  mkdir -p "$WRAPPER_DIR/extracted"
  unzip -q "$TMP" -d "$WRAPPER_DIR/extracted"
  ROOT=$(find "$WRAPPER_DIR/extracted" -maxdepth 1 -mindepth 1 -type d | head -n 1)
  mv "$ROOT" "$INSTALL_DIR"
fi
exec "$BIN" "$@"
