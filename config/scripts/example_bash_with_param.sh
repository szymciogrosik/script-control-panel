#!/bin/bash

function print_dog() {
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

print_dog

# Check if any parameters are provided
if [ $# -eq 0 ]; then
    echo "No parameters provided. Usage: ./example_bash_with_param.sh <param1> <param2> ..."
    exit 1
fi

# Print all provided parameters
echo "Provided parameters: $@"
