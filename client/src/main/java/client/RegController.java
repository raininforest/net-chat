package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;

import java.awt.*;

public class RegController {

    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public TextField nicknameField;
    @FXML
    public TextArea textArea;

    private Controller controller;

    public void tryToReg(ActionEvent actionEvent) {
        controller.tryToReg(loginField.getText().trim(),
                passwordField.getText().trim(),
                nicknameField.getText().trim());
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void addMsgToTextArea(String msg){
        textArea.appendText(msg + "\n");
    }
}
