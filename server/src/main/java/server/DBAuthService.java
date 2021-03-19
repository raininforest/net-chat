package server;

public class DBAuthService implements AuthService{
    @Override
    public String getNickNameByLoginAndPassword(String login, String password) {
        return SQLHandler.getNickNameByLoginAndPassword(login, password);
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        return SQLHandler.registration(login, password, nickname);
    }

    @Override
    public boolean changeNickName(String login, String newNick) {
        return SQLHandler.changeNickName(login, newNick);
    }
}
