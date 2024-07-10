#!/bin/bash

function print_success_image() {
  echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
  echo ""
  echo "        .-\".'.                      .--.            _..._    "
  echo "      .' .'                     .'    \\       .-\"\"  __ \"\"-. "
  echo "     /  /                     .'       : --..:__.-\"\"  \"\"-. \\ "
  echo "    :  :                     /         ;.d$$    sbp_.-\"\"-:_: "
  echo "    ;  :                    : ._       :P .-.   ,\"TP        "
  echo "    :   \\                    \\  T--...-; : d\$b  :d\$b        "
  echo "     \\   \`.                   \\  \`..'    ; \$ \$  ;\$ \$        "
  echo "      \`.   \"-.                 ).        : T\$P  :T\$P        "
  echo "        \\..---^..             /           \`-'    \`._\`._     "
  echo "       .'        \"-.       .-\"                     T\$\$\$b    "
  echo "      /             \"-._.-\"               ._        '^' ;   "
  echo "     :                                    \\.\`.         /    "
  echo "     ;                                -.   \\ \`.\"-._.-'-'     "
  echo "    :                                 .'\   \\ \\ \\ \\         "
  echo "    ;  ;                             /:  \\   \\ \\ . ;        "
  echo "   :   :                            ,  ;  \`.  \`.;  :        "
  echo "   ;    \\        ;                     ;    \"-._:  ;        "
  echo "  :      \`.      :                     :         \\/         "
  echo "  ;       /\"-.    ;                    :                    "
  echo " :       /    \"-. :                  : ;                    "
  echo " :     .'        T-;                 ; ;                    "
  echo " ;    :          ; ;                /  :                    "
  echo " ;    ;          : :              .'    ;                   "
  echo ":    :            ;:         _..-\"\\     :                   "
  echo ":     \\           : ;       /      \\     ;                  "
  echo ";    . '.         '-;      /        ;    :                  "
  echo ";  \\  ; :           :     :         :    '-.                "
  echo "'.._L.:-'           :     ;    SJG   ;    . \`.              "
  echo "                     ;    :          :  \\  ; :              "
  echo "                     :    '-..       '.._L.:-'              "
  echo "                      ;     , \`.                          "
  echo "                      :   \\  ; :                          "
  echo "                      '..__L.:-'                          "
  echo ""
  echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
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

function print_info_about_closing_window_soon() {
  for i in {10..1}; do
   echo "Window will be closed in $i seconds"
   sleep 1
  done
}

function wait_for_pressing_key() {
  echo "Press any button to exit..."
  read
}

# Define directories and filenames
CURRENT_DIR="$(pwd)"
OLD_APP_NAME="script_control_panel.jar"
NEW_APP_NAME="new_script_control_panel.jar"

# Check if $NEW_APP_NAME exists in the current directory
if [ ! -f "$CURRENT_DIR/$NEW_APP_NAME" ]; then
 echo "Error: $CURRENT_DIR/$NEW_APP_NAME not found!"
 print_error_image
 wait_for_pressing_key
 exit 1
fi

# Move one level up from the current directory
cd ..

# Define the target directory after moving up
TARGET_DIR="$(pwd)"

# Replace the old JAR with the new one
mv -f "$CURRENT_DIR/$NEW_APP_NAME" "$TARGET_DIR/$OLD_APP_NAME"
if [ $? -ne 0 ]; then
 echo "Error: Failed to move $CURRENT_DIR/$NEW_APP_NAME to $TARGET_DIR/$OLD_APP_NAME!"
 print_error_image
 wait_for_pressing_key
 exit 1
fi

# Restart the application
nohup java -jar "$TARGET_DIR/$OLD_APP_NAME" > "$TARGET_DIR/tmp/app_update.log" 2>&1 &
if [ $? -ne 0 ]; then
 echo "Error: Failed to start the application!"
 print_error_image
 wait_for_pressing_key
 exit 1
fi

echo "------------------------------------------------------"
echo "Application updated successfully!"
echo "------------------------------------------------------"
print_success_image
print_info_about_closing_window_soon