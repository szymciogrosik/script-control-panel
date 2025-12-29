#!/bin/bash
set -e

echo "Cleaning up previous build..."
rm -rf output

echo "Building Linux Application (Verification)..."
jpackage \
  --type app-image \
  --input target/libs \
  --main-jar script-control-panel.jar \
  --main-class org.codefromheaven.App \
  --name "ScriptControlPanel" \
  --dest output \
  --icon src/main/resources/icons/icon.png \
  --java-options "-Dfile.encoding=UTF-8"

echo "Build successful. Cleaning up..."
rm -rf output
echo "Cleanup done."
