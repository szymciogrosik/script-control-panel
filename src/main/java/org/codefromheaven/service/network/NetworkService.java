package org.codefromheaven.service.network;

import javafx.scene.control.Alert;
import org.codefromheaven.controller.PopupController;

public class NetworkService {

    private static Boolean networkPresent;

    private NetworkService() {
    }

    public static void setNetworkPresent() {
        networkPresent = true;
    }

    public static void setNetworkNotPresent() {
        networkPresent = false;
    }

    public static boolean isNetworkUnknown() {
        return networkPresent == null;
    }

    public static boolean isNetworkPresent() {
        return networkPresent != null && networkPresent;
    }

    public static void showPopupNetworkNotPresent() {
        PopupController.showPopup("Network not found, try check the updates later.",
                                  Alert.AlertType.ERROR);
    }

}
