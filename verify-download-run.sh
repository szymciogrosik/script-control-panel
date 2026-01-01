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

# Function to check if ScriptControlPanel already downloaded
check_downloaded() {
  if [ -f "$SCRIPT_CONTROL_PANEL_EXE" ]; then
    echo "File $SCRIPT_CONTROL_PANEL_EXE is already downloaded."
    return 0
  else
    echo "File $SCRIPT_CONTROL_PANEL_EXE is not downloaded."
    return 1
  fi
}

# Function to download the latest release of ScriptControlPanel
download_latest_release() {
  latest_tag=$(curl -s https://api.github.com/repos/szymciogrosik/script-control-panel/releases/latest | grep "tag_name" | cut -d '"' -f 4)
  if [[ -n "$latest_tag" ]]; then
    latest_url="https://github.com/szymciogrosik/script-control-panel/releases/download/$latest_tag/$SCRIPT_CONTROL_PANEL_ZIP"
    echo "Downloading the latest release of $SCRIPT_CONTROL_PANEL_ZIP from $latest_url"
    curl -L -o $SCRIPT_CONTROL_PANEL_ZIP "$latest_url"
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

unzip_script_control_panel() {

}

# Main script execution
if check_git_bash; then
  if check_downloaded || download_latest_release; then
    unzip_script_control_panel
    echo "Running $SCRIPT_CONTROL_PANEL_EXE"
    run_script_control_panel
    echo "Process started. Closing the bash window."
    sleep 1
    exit 0
  fi
fi

wait_for_button_pressed
