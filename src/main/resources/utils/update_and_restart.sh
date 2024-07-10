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

# Check if tmp_script_control_panel.jar exists
if [ ! -f tmp_script_control_panel.jar ]; then
    echo "Error: tmp_script_control_panel.jar not found!"
    print_error_image
    wait_for_pressing_key
    exit 1
fi

# Replace the old JAR with the new one
mv -f tmp_script_control_panel.jar script_control_panel.jar
if [ $? -ne 0 ]; then
    echo "Error: Failed to move tmp_script_control_panel.jar to script_control_panel.jar!"
    print_error_image
    wait_for_pressing_key
    exit 1
fi

# Restart the application
nohup java -jar script_control_panel.jar > app.log 2>&1 &
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