package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public HBox authorizationPanel;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox messagePanel;
    @FXML
    public TextField loginField;
    @FXML
    public TextArea textArea;
    @FXML
    public TextField messageTextField;
    @FXML
    public ListView<String> clientList;

    private final String WINDOW_TITLE = "Chat";
    private final String IP_ADRESS = "localhost";
    private final int PORT = 8189;
    private final int COUNT_OF_HISTORY_TO_DISPLAY = 10;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private boolean authentificated;
    private String nickname;
    private String login;

    private Stage stage;
    private Stage regStage;
    private RegController regController;

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()){
            connection();
        }
        try {
            login = loginField.getText().trim().toLowerCase();
            System.out.println("Сообщение авторизации для сервера: " + (String.format("/auth %s %s", login,
                    passwordField.getText().trim())));
            out.writeUTF(String.format("/auth %s %s", login,
                    passwordField.getText().trim()));
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ActionEvent actionEvent) {
        try {
            out.writeUTF(messageTextField.getText());
            messageTextField.clear();
            messageTextField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setAuthentificated(boolean authentificated){
        this.authentificated = authentificated;
        authorizationPanel.setVisible(!authentificated);
        authorizationPanel.setManaged(!authentificated);
        messagePanel.setVisible(authentificated);
        messagePanel.setManaged(authentificated);
        clientList.setVisible(authentificated);
        clientList.setManaged(authentificated);
        if (!authentificated) {
            nickname = "";
        }
        textArea.clear();
        setTitle(nickname);
    }

    private void setTitle(String nick) {
        Platform.runLater(()->{
            ((Stage) messageTextField.getScene().getWindow()).setTitle(WINDOW_TITLE + " " + nick);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //connection();
        setAuthentificated(false);
        createRegWindow();
        Platform.runLater(()->{
            stage = (Stage) messageTextField.getScene().getWindow();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    System.out.println("Пока!");
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void connection(){
        try {
            socket = new Socket(IP_ADRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //цикл аутентификации
                        while (true) {
                            String messageFromServer = in.readUTF();
                            if(messageFromServer.startsWith("/authok")){
                                nickname = messageFromServer.split("\\s", 2)[1];
                                setAuthentificated(true);
                                break;
                            }
                            if (messageFromServer.startsWith("/regok")){
                                regController.addMsgToTextArea("Регистрация прошла успешно");
                            }
                            if (messageFromServer.startsWith("/regno")){
                                regController.addMsgToTextArea("Регистрация не удалась.\n Возможно пользователь с таким именем уже существует");
                            }
                            textArea.appendText(messageFromServer + "\n");
                        }
                        //цикл работы
                        loadHistory();
                        HistoryLog.startWritingHistory(login);
                        while (true) {
                            String messageFromServer = in.readUTF();
                            if (messageFromServer.startsWith("/")) {
                                if (messageFromServer.equals("/end")) {
                                    System.out.println("Мы отключились от сервера");
                                    break;
                                }
                                if (messageFromServer.startsWith("/clientlist")){
                                    String[] token = messageFromServer.split("\\s+");
                                    Platform.runLater(() -> {
                                        clientList.getItems().clear();
                                        for (int i = 1; i < token.length; i++) {
                                            clientList.getItems().add(token[i]);
                                        }
                                    });
                                }
                            } else {
                                textArea.appendText(messageFromServer + "\n");
                                HistoryLog.writeMessage(messageFromServer);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        HistoryLog.closeFile();
                        setAuthentificated(false);
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registration(ActionEvent actionEvent) {
        regStage.show();
    }

    public void clickClientList(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        messageTextField.setText("/w " + receiver + " ");
    }

    public void tryToReg(String login, String password, String nickname) {
        String registrationMessage = String.format("/reg %s %s %s", login, password, nickname);
        if (socket == null || socket.isClosed()) {
            connection();
        }
        try {
            out.writeUTF(registrationMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createRegWindow(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Registration");
            regStage.setScene(new Scene(root, 400, 250));
            regController = fxmlLoader.getController();
            regController.setController(this);
            regStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory(){
        List<String> historyList = HistoryLog.getLastMessages(login, COUNT_OF_HISTORY_TO_DISPLAY);
        for (String str : historyList) {
            textArea.appendText(str + "\n");
        }
    }
}
