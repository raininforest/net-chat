package server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public class ClientHandler {
    private static final Logger log = LogManager.getLogger();
    private static final int TIMEOUT = 120 * 1000;

    private Server server = null;
    private Socket socket = null;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket, ExecutorService executorService) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            socket.setSoTimeout(TIMEOUT);
            executorService.execute(() -> {
                        try {
                            //цикл аутентификации
                            while (true) {
                                String messageFromClient = in.readUTF();
                                log.info("Сообщение авторизации от клиента:" + messageFromClient);
                                if (messageFromClient.startsWith("/auth")) {
                                    String[] token = messageFromClient.split("\\s");
                                    String newNick = server.getAuthService().getNickNameByLoginAndPassword(token[1], token[2]);
                                    login = token[1];
                                    if (newNick != null) {
                                        if (!server.isLoginAuthentificated(newNick)) {
                                            nickname = newNick;
                                            sendMsg("/authok " + nickname);
                                            server.subscribe(this);
                                            log.info("Клиент " + nickname + " авторизовался");
                                            break;
                                        } else {
                                            sendMsg("Данная учетная запись уже авторизована.");
                                        }
                                    } else {
                                        sendMsg("Неверный логин / пароль");
                                    }
                                }
                                if (messageFromClient.startsWith("/reg")) {
                                    String[] token = messageFromClient.split("\\s");
                                    if (token.length < 4) {
                                        continue;
                                    }
                                    boolean isRegistration = server.getAuthService()
                                            .registration(token[1], token[2], token[3]);
                                    if (isRegistration) {
                                        sendMsg("/regok");
                                    } else {
                                        sendMsg("/regno");
                                    }
                                }
                            }
                            socket.setSoTimeout(0);

                            //цикл работы
                            while (true) {
                                String messageFromClient = in.readUTF();
                                log.info("Сообщение от клиента:" + messageFromClient);
                                if (messageFromClient.startsWith("/")) {
                                    if (messageFromClient.equals("/end")) {
                                        out.writeUTF("/end");
                                        break;
                                    }
                                    if (messageFromClient.startsWith("/w")) {
                                        String[] token = messageFromClient.split("\\s");
                                        String recieverNick = token[1];
                                        StringBuilder privateMessageBuilder = new StringBuilder();
                                        for (int i = 2; i < token.length; i++) {
                                            privateMessageBuilder.append(token[i] + " ");
                                        }
                                        String privateMessage = privateMessageBuilder.toString();
                                        server.sendPrivateMessage(this, recieverNick, privateMessage);
                                    }
                                    if (messageFromClient.startsWith("/changenick")) {
                                        String[] token = messageFromClient.split("\\s");
                                        String newNick = token[1];
                                        boolean changeNickOK = server.getAuthService().changeNickName(login, newNick);
                                        if (changeNickOK) {
                                            nickname = newNick;
                                            server.broadClientList();
                                            sendMsg("Ник успешно изменен.");
                                        } else {
                                            sendMsg("Ошибка смены ника.");
                                        }
                                    }
                                } else {
                                    server.broadcastMessage(this, messageFromClient);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            log.info("Клиент отключился");
                            server.unsubscribe(this);
                            try {
                                socket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    String getNickname(){
        return nickname;
    }

    String getLogin() {
        return login;
    }
}
