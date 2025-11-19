package org.codefromheaven.service.network;

import javafx.scene.control.Alert;
import org.codefromheaven.controller.PopupController;

import org.springframework.stereotype.Service;

@Service
public class NetworkService {

    private Boolean networkPresent;

    public void setNetworkPresent() {
        networkPresent = true;
    }

    public void setNetworkNotPresent() {
        networkPresent = false;
    }

    public boolean isNetworkUnknown() {
        return networkPresent == null;
    }

    public boolean isNetworkPresent() {
        return networkPresent != null && networkPresent;
    }

    public void showPopupNetworkNotPresent() {
        PopupController.showPopup("Network not found, try check the updates later.",
                                  Alert.AlertType.ERROR);
    }

}
