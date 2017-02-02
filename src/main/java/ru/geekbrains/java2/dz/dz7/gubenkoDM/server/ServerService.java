package ru.geekbrains.java2.dz.dz7.gubenkoDM.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class ServerService {
    //список  активных клиентов в сети
    protected  static   ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

    /**
     *  Инициализация сервера чата
     */

    public ServerService() {
        ServerSocket server = null;
        Socket s = null;
        try {
            //создание серверного сокета на указанном порту
            server = new ServerSocket(8189);
            System.out.println("Server created. Waiting for client...");
            //ожидание подключения клиентов
            while (true) {
                //тут ждем пока клиент уже наконец подключится
                //как подключился получим его сокет для обмена с ним
                s = server.accept();
                System.out.println("Client connected");
                //создадим для клиента отдельный поток.
                //передадим сокет самого подкл. клиента и ссылку на объект нашего серва
                ClientHandler h = new ClientHandler(s, this);
                //добавим клиента в список рабочих клиентов на сервере
                clients.add(h);
                //запуск потока текущего клиента для работы с ним
                new Thread(h).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //раз мы тут то заканчиваем работу сервера
                //серверный сокет закрыли
                server.close();
                System.out.println("Server closed");
                //закрыли сокет клиента
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Метод для удаления не активных клиентов
     * @param o ссылка на клиента которого мы хотим удалить из списка активных
     */

    public void remove(ClientHandler o) {
        clients.remove(o);
    }


    /**
     * Метод по отсылке переданного сообщения всем активным клиентам
     * @param msg сообщение отправляемое всем активным клиентам
     */

    public void broadcastMsg(String msg) {
        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }
}
