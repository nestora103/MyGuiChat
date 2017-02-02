package ru.geekbrains.java2.dz.dz7.gubenkoDM.server;

public class ServerStarter {

    public static void main(String[] args) {
        //пытаемся подключится к базе данных. Ее настройки вшиты внутрь, пока так.
        SQLHandler.connect();
        //запускалка сервера
        ServerService  w = new ServerService();
        //SQLHandler.disconnect();
    }
}
