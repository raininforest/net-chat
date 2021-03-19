package server;

public interface AuthService {
    /**
     * Узнать ник по логину и паролю
     * @param login - логин
     * @param password - пароль
     * @return - nickname, если пользователь есть в базе, null, если нет
     */
    String getNickNameByLoginAndPassword(String login, String password);

    /**
     * Зарегистрировать пользователя
     * @param login - логин
     * @param password - пароль
     * @param nickname - ник
     * @return - результат регистрации
     */
    boolean registration(String login, String password, String nickname);

    /**
     * Изменить ник
     * @param login - логин
     * @param newNick - новый ник
     * @return
     */
    boolean changeNickName(String login, String newNick);
}
