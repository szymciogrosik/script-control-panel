#!/bin/bash

# Wait for the application to close
sleep 5

# Check if tmp_script_control_panel.jar exists
if [ ! -f tmp_script_control_panel.jar ]; then
    echo "Error: tmp_script_control_panel.jar not found!"
    exit 1
fi

# Replace the old JAR with the new one
mv -f tmp_script_control_panel.jar script_control_panel.jar
if [ $? -ne 0 ]; then
    echo "Error: Failed to move tmp_script_control_panel.jar to script_control_panel.jar!"
    exit 1
fi

# Restart the application
nohup java -jar script_control_panel.jar > app.log 2>&1 &
if [ $? -ne 0 ]; then
    echo "Error: Failed to start the application!"
    exit 1
fi

echo "Application restarted successfully. Check app.log for details."

echo "Press any button to exit..."
read
