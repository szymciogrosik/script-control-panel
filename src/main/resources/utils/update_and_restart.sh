#!/bin/bash

# Configuration
NEW_ZIP_NAME="ScriptControlPanel-Win.zip"
APP_EXE_NAME="ScriptControlPanel.exe"

function print_batman() {
  echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++"
  echo "           _                         _"
  echo "       _==/          i     i          \=="
  echo "     /XX/            |\___/|            \XX\\"
  echo "   /XXXX\            |XXXXX|            /XXXX\\"
  echo "  |XXXXXX\_         _XXXXXXX_         _/XXXXXX|"
  echo " XXXXXXXXXXXxxxxxxxXXXXXXXXXXXxxxxxxxXXXXXXXXXXX"
  echo "|XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX|"
  echo "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"
  echo "|XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX|"
  echo " XXXXXX/^^^^\"\XXXXXXXXXXXXXXXXXXXXX/^^^^^\XXXXXX"
  echo "  |XXX|       \XXX/^^\XXXXX/^^\XXX/       |XXX|"
  echo "    \XX\       \X/    \XXX/    \X/       /XX/"
  echo "       \"\       \"      \X/      \"       /\""
  echo "      SJG               !"
  echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++"
}

function print_error_image() {
  echo "++++++++++++++++++++++++++++++++++++++++++++++"
  echo "                                  .-."
  echo "     (___________________________()6 \`-,"
  echo "     (   ______________________   /''\""
  echo "     //\\\\                      //\\\\"
  echo "     \"\" \"\"                     \"\" \"\""
  echo "++++++++++++++++++++++++++++++++++++++++++++++"
}

function print_info_about_waiting_for_closing_application() {
  for i in {3..1}; do
    echo "Waiting for closing old application - $i seconds left"
    sleep 1
  done
}

function print_info_about_closing_window_soon() {
  for i in {5..1}; do
    echo "Window will be closed in $i seconds"
    sleep 1
  done
}

function wait_for_pressing_key() {
  echo "Press any button to exit..."
  read -n 1 -s
}

# 1. Wait for the old app to close fully
print_info_about_waiting_for_closing_application

# 2. Setup Directories
CURRENT_DIR="$(pwd)"
ZIP_FILE_PATH="$CURRENT_DIR/$NEW_ZIP_NAME"

# Check if the update ZIP exists
if [ ! -f "$ZIP_FILE_PATH" ]; then
  echo "Error: Update file $ZIP_FILE_PATH not found!"
  print_error_image
  wait_for_pressing_key
  exit 1
fi

# Move one level up (to the installation root)
cd ..
TARGET_DIR="$(pwd)"

# 3. Unzip and Overwrite
echo "Extracting update..."
# -o: overwrite existing files without prompting
# -d: destination directory
if unzip -o "$ZIP_FILE_PATH" -d "$TARGET_DIR"; then
  echo "Update extracted successfully."
else
  echo "Error: Failed to unzip files!"
  print_error_image
  wait_for_pressing_key
  exit 1
fi

# 4. Run the new Executable
echo "Starting application..."
if [ -f "$TARGET_DIR/$APP_EXE_NAME" ]; then
  # 'start' detaches the process in Windows Git Bash so this script can close
  start "" "$TARGET_DIR/$APP_EXE_NAME"
else
  echo "Error: $APP_EXE_NAME not found after extraction!"
  print_error_image
  wait_for_pressing_key
  exit 1
fi

# 5. Success and Exit
echo "------------------------------------------------------"
echo "Application updated successfully!"
echo "------------------------------------------------------"
print_batman
print_info_about_closing_window_soon
exit 0
