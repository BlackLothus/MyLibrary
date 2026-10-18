package it.unisa.mylibrary.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Alerts {
    private Alerts(){
        throw new AssertionError("Classe di utility,non va istanziata");
    }
    private static void showAlertHelper (Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void showError (String title, String message){
        showAlertHelper(Alert.AlertType.ERROR, title, message);
    }
    public static void showInfo(String title, String message) {
        showAlertHelper(Alert.AlertType.INFORMATION, title, message);
    }

    public static boolean showConfirm(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType btnOk = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnAnnulla = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnOk, btnAnnulla);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnOk;
    }
}
