#!/bin/bash

SCRIPT_CONTROL_PANEL_JAR_FILE="script-control-panel.jar"

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

# Function to get the value of JAVA_PATH from a specified JSON file
get_java_path_variable_for_file() {
  local json_file="$1"
  local java_path=$(awk '
        BEGIN { RS="{"; FS="," }
        /"key": "JAVA_PATH"/ {
          for (i = 1; i <= NF; i++) {
            if ($i ~ /"value":/) {
              gsub(/.*"value": *"/, "", $i)
              gsub(/".*/, "", $i)
              print $i
              exit 0
            }
          }
        }
        ' "$json_file")
  echo "$java_path"
}

# Function to check if Java 17+ is installed
check_java() {
  local java_custom_path="$(get_java_path_variable)"
  if [ -n "$java_custom_path" ]; then
    echo "Found Java custom path: '$java_custom_path'"
    return 0
  else
    echo "Java custom path was not found, checking PATH"
  fi

  if type -p java >/dev/null 2>&1; then
    echo "Java executable found in PATH."
    _java=java
  elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    echo "Java executable found in JAVA_HOME."
    _java="$JAVA_HOME/bin/java"
  else
    print_error_message "Java is not installed."
    return 1
  fi

  version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
  if [[ "$version" > "17" || "$version" == "17" ]]; then
    echo "Java version is $version which is 17 or higher."
    return 0
  else
    print_error_message "Java version is $version which is less than 17."
    return 1
  fi
}

# Function to check if Git Bash is installed
check_git_bash() {
  if git --version 2>&1 | grep -q "git version"; then
    echo "Git Bash is installed."
    return 0
  else
    print_error_message "Git Bash is not installed."
    return 1
  fi
}

# Function to check if script-control-panel.jar is already downloaded
check_downloaded() {
  if [ -f "$SCRIPT_CONTROL_PANEL_JAR_FILE" ]; then
    echo "File $SCRIPT_CONTROL_PANEL_JAR_FILE is already downloaded."
    return 0
  else
    echo "File $SCRIPT_CONTROL_PANEL_JAR_FILE is not downloaded."
    return 1
  fi
}

# Function to download the latest release of script-control-panel.jar
download_latest_release() {
  latest_tag=$(curl -s https://api.github.com/repos/szymciogrosik/script-control-panel/releases/latest | grep "tag_name" | cut -d '"' -f 4)
  if [[ -n "$latest_tag" ]]; then
    latest_url="https://github.com/szymciogrosik/script-control-panel/releases/download/$latest_tag/$SCRIPT_CONTROL_PANEL_JAR_FILE"
    echo "Downloading the latest release of $SCRIPT_CONTROL_PANEL_JAR_FILE from $latest_url"
    curl -L -o $SCRIPT_CONTROL_PANEL_JAR_FILE "$latest_url"
    if [[ $? -eq 0 ]]; then
      echo "Downloaded $SCRIPT_CONTROL_PANEL_JAR_FILE successfully."
      return 0
    else
      print_error_message "Failed to download $SCRIPT_CONTROL_PANEL_JAR_FILE."
      return 1
    fi
  else
    print_error_message "Could not find the latest release tag."
    print_error_message "It looks like too many people trying to download this app, please try again after 1 hour."
    return 1
  fi
}

# Function to get the value of JAVA_PATH, checking multiple files
get_java_path_variable() {
  local my_own_settings_file="config/my_own_settings.json"
  local settings_file="config/default_settings.json"

  local java_path=$(get_java_path_variable_for_file "$my_own_settings_file")

  if [ -z "$java_path" ]; then
    java_path=$(get_java_path_variable_for_file "$settings_file")
  fi

  echo "$java_path"
}

# Function to run script-control-panel.jar
run_script_control_panel() {
  local java_path="$(get_java_path_variable)"
  if [ -n "$java_path" ]; then
    local java_dir_path="$java_path\\java.exe"
    echo "Executing JAR with custom Java path: '$java_dir_path'"
    nohup "$java_dir_path" -jar "$SCRIPT_CONTROL_PANEL_JAR_FILE" > start_logs.log 2>&1 &
  else
    echo "Executing JAR with standard JAVA_HOME path: '$JAVA_HOME'"
    nohup java -jar "$SCRIPT_CONTROL_PANEL_JAR_FILE" > start_logs.log 2>&1 &
  fi
}

# Main script execution
if check_java && check_git_bash; then
  if check_downloaded || download_latest_release; then
    echo "Running $SCRIPT_CONTROL_PANEL_JAR_FILE"
    run_script_control_panel
    echo "Process started. Closing the bash window."
    sleep 1
    exit 0
  fi
fi

echo 'Press any button to exit...'
read -n 1 -s
