package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final Logger log = LogManager.getLogger();
    private final int PORT = 8189;
    private final int MAX_CLIENTS_CONNECTED = 100;

    ServerSocket serverSocket = null;
    Socket socket = null;
    List<ClientHandler> clients;
    private AuthService authService;

    public Server() {
        log.info("Сервер запущен");
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CLIENTS_CONNECTED);
        clients = new Vector<>();
        if (!SQLHandler.connect()){
            log.error("Не удалось подключиться к БД");
            throw new RuntimeException("Не удалось подключиться к БД");
        }
        authService = new DBAuthService();
        try {
            serverSocket = new ServerSocket(PORT);
            log.info("Сервер ожидает соединения");
            while (true) {
                socket = serverSocket.accept();
                log.info("Установлено соединение с клиентом");
                new ClientHandler(this, socket, executorService);
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.getMessage());
        } finally {
            SQLHandler.disconnect();
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
            executorService.shutdownNow();
        }
    }

    public void subscribe(ClientHandler clientHandler){
        clients.add(clientHandler);
        broadClientList();
    }

    public void unsubscribe(ClientHandler clientHandler){
        clients.remove(clientHandler);
        broadClientList();
    }

    public AuthService getAuthService(){
        return authService;
    }

    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

    void broadcastMessage(ClientHandler sender, String msg){
        String formattedMessage = String.format("%s %s : %s", formatter.format(new Date()), sender.getNickname(), msg);
        for (ClientHandler client : clients) {
            client.sendMsg(formattedMessage);
        }
    }

    void sendPrivateMessage(ClientHandler sender, String recieverNickname, String privateMsg){
        //формат сообщения у отправителя
        String formattedPrivateMessageForReciever = String.format("private message from %s : %s", sender.getNickname(), privateMsg);
        //формат сообщения у получателя
        String formattedMessageForSender = String.format("message to %s : %s", recieverNickname, privateMsg);
        //поиск клиента с ником, которому адресовано личное сообщение
        for (ClientHandler client : clients) {
            if (client.getNickname().equals(recieverNickname)){
                client.sendMsg(formattedPrivateMessageForReciever);
                break;
            }
        }
        //отправка сообщения отправителю
        sender.sendMsg(formattedMessageForSender + "\n");
    }

    public void broadClientList() {
        StringBuilder clientListString = new StringBuilder("/clientlist ");
        for(ClientHandler c: clients){
            clientListString.append(c.getNickname()).append(" ");
        }
        String msg = clientListString.toString();
        for (ClientHandler c: clients){
            c.sendMsg(msg);
        }
    }

    public boolean isLoginAuthentificated(String login){
        for (ClientHandler client: clients) {
            if(client.getLogin().equals(login)){
                return true;
            }
        }
        return false;
    }
}
