#!/bin/bash

print_error() {
  echo " _____   ____    ____     ___    ____    _   _   _ "
  echo "| ____| |  _ \  |  _ \   / _ \  |  _ \  | | | | | |"
  echo "|  _|   | |_) | | |_) | | | | | | |_) | | | | | | |"
  echo "| |___  |  _ <  |  _ <  | |_| | |  _ <  |_| |_| |_|"
  echo "|_____| |_| \_\ |_| \_\  \___/  |_| \_\ (_) (_) (_)"
}

# Function to check if Java 17+ is installed
check_java() {
 if type -p java >/dev/null 2>&1; then
   echo "Java executable found in PATH."
   _java=java
 elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
   echo "Java executable found in JAVA_HOME."
   _java="$JAVA_HOME/bin/java"
 else
   print_error
   echo "Java is not installed."
   return 1
 fi

 version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
 if [[ "$version" > "17" || "$version" == "17" ]]; then
   echo "Java version is $version which is 17 or higher."
   return 0
 else
   print_error
   echo "Java version is $version which is less than 17."
   return 1
 fi
}

# Function to check if Git Bash is installed
check_git_bash() {
 if git --version 2>&1 | grep -q "git version"; then
   echo "Git Bash is installed."
   return 0
 else
   print_error
   echo "Git Bash is not installed."
   return 1
 fi
}

# Function to download the latest release of script-control-panel.jar
download_latest_release() {
 latest_tag=$(curl -s https://api.github.com/repos/szymciogrosik/script-control-panel/releases/latest | grep "tag_name" | cut -d '"' -f 4)
 if [[ -n "$latest_tag" ]]; then
   latest_url="https://github.com/szymciogrosik/script-control-panel/releases/download/$latest_tag/script-control-panel.jar"
   echo "Downloading the latest release of script-control-panel.jar from $latest_url"
   curl -L -o script-control-panel.jar "$latest_url"
   if [[ $? -eq 0 ]]; then
     echo "Downloaded script-control-panel.jar successfully."
     return 0
   else
     print_error
     echo "Failed to download script-control-panel.jar."
     return 1
   fi
 else
   print_error
   echo "Could not find the latest release tag."
   return 1
 fi
}

# Main script execution
if check_java && check_git_bash; then
 if download_latest_release; then
   echo "Running script-control-panel.jar"
   java -jar script-control-panel.jar &
   echo "Process started. Closing the bash window."
   exit 0
 fi
fi

echo 'Press any button to exit...'
read -n 1 -s
