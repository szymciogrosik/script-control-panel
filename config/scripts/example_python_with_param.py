import sys

def print_dog():
    print("+" * 68)
    print("")
    print("        .-\".'.                      .--.            _..._    ")
    print("      .' .'                     .'    \\       .-\"\"  __ \"\"-. ")
    print("     /  /                     .'       : --..:__.-\"\"  \"\"-. \\ ")
    print("    :  :                     /         ;.d$$    sbp_.-\"\"-:_: ")
    print("    ;  :                    : ._       :P .-.   ,\"TP        ")
    print("    :   \\                    \\  T--...-; : d$b  :d$b        ")
    print("     \\   `.                   \\  `..'    ; $ $  ;$ $        ")
    print("      `.   \"-.                 ).        : T$P  :T$P        ")
    print("        \\..---^..             /           `-'    `._`._     ")
    print("       .'        \"-.       .-\"                     T$$$b    ")
    print("      /             \"-._.-\"               ._        '^' ;   ")
    print("     :                                    \\.`.         /    ")
    print("     ;                                -.   \\ `.\"-._.-'-'     ")
    print("    :                                 .'\\   \\ \\ \\ \\         ")
    print("    ;  ;                             /:  \\   \\ \\ . ;        ")
    print("   :   :                            ,  ;  `.  `.;  :        ")
    print("   ;    \\        ;                     ;    \"-._:  ;        ")
    print("  :      `.      :                     :         \\/         ")
    print("  ;       /\"-.    ;                    :                    ")
    print(" :       /    \"-. :                  : ;                    ")
    print(" :     .'        T-;                 ; ;                    ")
    print(" ;    :          ; ;                /  :                    ")
    print(" ;    ;          : :              .'    ;                   ")
    print(":    :            ;:         _..-\"\\     :                   ")
    print(":     \\           : ;       /      \\     ;                  ")
    print(";    . '.         '-;      /        ;    :                  ")
    print(";  \\  ; :           :     :         :    '-.                ")
    print("'.._L.:-'           :     ;    SJG   ;    . `.              ")
    print("                     ;    :          :  \\  ; :              ")
    print("                     :    '-..       '.._L.:-'              ")
    print("                      ;     , `.                          ")
    print("                      :   \\  ; :                          ")
    print("                      '..__L.:-'                          ")
    print("")
    print("+" * 68)

def main():
    print_dog()

    if len(sys.argv) <= 1:
        print("No parameters provided. Usage: python3 example.py <param1> <param2> ...")
        sys.exit(1)

    print("Provided parameters:", ' '.join(sys.argv[1:]))

if __name__ == "__main__":
    main()
