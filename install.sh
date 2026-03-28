#!/bin/bash

SCRIPT_CONTROL_PANEL_ZIP="ScriptControlPanel-Win.zip"
SCRIPT_CONTROL_PANEL_EXE="ScriptControlPanel.exe"

print_error_ascii() {
  echo " _____   ____    ____     ___    ____    _   _   _ "
  echo "| ____| |  _ \  |  _ \   / _ \  |  _ \  | | | | | |"
  echo "|  _|   | |_) | | |_) | | | | | | |_) | | | | | | |"
  echo "| |___  |  _ <  |  _ <  | |_| | |  _ <  |_| |_| |_|"
  echo "|_____| |_| \_\ |_| \_\  \___/  |_| \_\ (_) (_) (_)"
}

print_line_separator() {
  echo "----------------------------------------------------"
}

print_find_more_info() {
  echo "More information about required installed software you can find under link:"
  echo "https://github.com/szymciogrosik/script-control-panel/wiki"
}

print_error_message() {
  errorMessage="$1"
  print_error_ascii
  print_line_separator
  echo "Original error:"
  echo "$errorMessage"
  print_line_separator
  print_find_more_info
  print_line_separator
}

wait_for_button_pressed() {
  echo 'Press any button to exit...'
  read -n 1 -s
}

# Function to check if Git Bash is installed
check_git_bash() {
  if git --version 2>&1 | grep -q "git version"; then
    echo "Git Bash is installed."
    return 0
  else
    print_error_message "Git Bash is not installed."
    wait_for_button_pressed
    return 1
  fi
}

# Function to unzip the downloaded file
unzip_script_control_panel() {
  echo "Unzipping $SCRIPT_CONTROL_PANEL_ZIP..."

  # Check if unzip is installed
  if ! command -v unzip &> /dev/null; then
      print_error_message "'unzip' command not found. Please install unzip or use Git Bash."
      wait_for_button_pressed
      return 1
  fi

  # -o overwrites existing files without prompting
  if unzip -o "$SCRIPT_CONTROL_PANEL_ZIP"; then
      echo "Unzipped successfully."
      return 0
  else
      print_error_message "Failed to unzip $SCRIPT_CONTROL_PANEL_ZIP"
      wait_for_button_pressed
      return 1
  fi
}

# Function to download the latest release of ScriptControlPanel
download_latest_release() {
  # Copy initial version instead downloading if present
  if [ -f "initial/$SCRIPT_CONTROL_PANEL_ZIP" ]; then
      echo "Found existing initial version. Copying..."
      cp "initial/$SCRIPT_CONTROL_PANEL_ZIP" "$SCRIPT_CONTROL_PANEL_ZIP"
      return 0
  fi

  echo "Checking for latest release..."

  latest_tag=$(curl -s https://api.github.com/repos/szymciogrosik/script-control-panel/releases/latest | grep "tag_name" | cut -d '"' -f 4)

  if [[ -n "$latest_tag" ]]; then
    latest_url="https://github.com/szymciogrosik/script-control-panel/releases/download/$latest_tag/$SCRIPT_CONTROL_PANEL_ZIP"

    echo "Downloading the latest release of $SCRIPT_CONTROL_PANEL_ZIP from $latest_url"
    curl -L -o "$SCRIPT_CONTROL_PANEL_ZIP" "$latest_url"

    if [[ $? -eq 0 ]]; then
      echo "Downloaded $SCRIPT_CONTROL_PANEL_ZIP successfully."
      return 0
    else
      print_error_message "Failed to download $SCRIPT_CONTROL_PANEL_ZIP."
      wait_for_button_pressed
      return 1
    fi
  else
    print_error_message "Could not find the latest release tag."
    print_error_message "It looks like too many people trying to download this app, please try again after 1 hour."
    wait_for_button_pressed
    return 1
  fi
}

# Function to cleanup tmp directories
cleanup_tmp_directories() {
  echo "Cleaning up temporary directories..."

  # Check and remove 'tmp'
  if [ -d "tmp" ]; then
    rm -rf tmp
  fi

  # Check and remove 'app'
  if [ -d "app" ]; then
    rm -rf app
  fi

  # Check and remove 'runtime'
  if [ -d "runtime" ]; then
    rm -rf runtime
  fi
}

# Main script execution
if check_git_bash; then
  # Removed logic that skips download if file exists.
  # Now forcing download_latest_release to enable update/replace behavior.
  if download_latest_release; then
    cleanup_tmp_directories
    # Try to unzip
    if unzip_script_control_panel; then
        rm "$SCRIPT_CONTROL_PANEL_ZIP"
        print_line_separator
        print_line_separator
        print_line_separator
        echo "Installed successfully the Script Control Panel App!"
        print_line_separator
        echo "Double press ScriptControlPanel.exe to start!"
        print_line_separator
        print_line_separator
        print_line_separator
    fi
  fi
fi

wait_for_button_pressed