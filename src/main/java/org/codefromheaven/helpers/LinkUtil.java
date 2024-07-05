package org.codefromheaven.helpers;

import java.net.URI;

public class LinkUtil {

    private LinkUtil() {
    }

    public static void openPageInBrowser(String url) {
        try {
            URI u = new URI(url);
            java.awt.Desktop.getDesktop().browse(u);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open page in browser", e);
        }
    }

}
