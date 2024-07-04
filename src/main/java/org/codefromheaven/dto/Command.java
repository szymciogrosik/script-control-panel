package org.codefromheaven.dto;

public record Command(String scriptPathVarName, boolean autoCloseConsole, String command) {

}