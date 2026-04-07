package org.codefromheaven.helpers;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;

public class WindowsAppManager {

    public static void initializeUniqueAUMID() {
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }

        try (Arena arena = Arena.ofConfined()) {
            // Load shell32.dll into the confined arena
            SymbolLookup shell32 = SymbolLookup.libraryLookup("shell32.dll", arena);
            MemorySegment functionAddress = shell32.find("SetCurrentProcessExplicitAppUserModelID")
                    .orElseThrow(() -> new IllegalStateException("Missing SetCurrentProcessExplicitAppUserModelID in shell32.dll"));

            // Define the C function signature: HRESULT (int) function(PCWSTR (pointer))
            MethodHandle setAppId = Linker.nativeLinker().downcallHandle(
                    functionAddress,
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
            );

            // Append \0 for C-style null termination required by Windows API
            String appId = "Org.CodeFromHeaven.ScriptControlPanel.1\0";
            
            // PCWSTR is a pointer to a null-terminated 16-bit Unicode string
            // Windows typically uses UTF-16LE for its wide strings (PCWSTR)
            MemorySegment nativeString = arena.allocateFrom(appId, StandardCharsets.UTF_16LE);

            // Exact invocation bypassing JNI overhead
            int hresult = (int) setAppId.invokeExact(nativeString);
            
            if (hresult != 0) {
                System.err.println("Failed to set AUMID. HRESULT: " + hresult);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
