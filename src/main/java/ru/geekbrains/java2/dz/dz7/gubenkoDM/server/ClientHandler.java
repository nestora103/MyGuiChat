package ru.geekbrains.java2.dz.dz7.gubenkoDM.server;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 *  Класс поток для работы с клиентом на сервере
 */

public class ClientHandler implements Runnable {
    private ServerService owner;
    private Socket s;
    private DataOutputStream out;
    private DataInputStream in;

    public String getName() {
        return name;
    }

    private String name;


    /**
     *
     * @param s ссылка на сокет текущего клиента
     * @param owner ссылка на объект сервера
     */
    public ClientHandler(Socket s, ServerService owner) {
        try {
            this.s = s;
            this.owner = owner;
            //получение входного и выходного потоков клиента
            in = new DataInputStream(s.getInputStream());
            out = new DataOutputStream(s.getOutputStream());
            name = "";
        } catch (IOException e) {
        }
    }

    /**
     * Метод для проверки нет ли такого же пользователя в сети
     * @return вернет true если есть, false если нет!
     */
    private boolean isCopyUser(String name){
        for (ClientHandler ch:ServerService.clients) {
            if (ch.getName().equals(name)){
                return true;
            }
        }
        return false;
    }


    @Override
    public void run() {
        try {
            //собственно мониторим когда клиент нам что нибудь отправит
            while (true) {
                //ждем сообщения отклиента
                String w = in.readUTF();
                //получаем в массиве логин и пароль. должны через табуляции быть заданы. а также служебную команду
                String[] n = w.split("\t");

                switch (n[0]){
                    case "add":
                        if (SQLHandler.addUser(n[1],n[2],n[3])){
                            //удачно
                            sendMsg("Новый пользователь добавлен!");
                        }else{
                            //неудачно
                            sendMsg("Добавить нового пользователя не удалось");
                        }
                        //обнулим пользовательский ввод, чтобы пропустить в дальнейшем текущую итерацию
                        w = null;
                        break;
                    case "setPass":
                        if (SQLHandler.setPass(n[1],n[2])){
                            //удачно
                            sendMsg("Пароль изменен удачно!");
                        }else{
                            //неудачно
                            sendMsg("Неудача при изменении пароля!");
                        }
                        //обнулим пользовательский ввод, чтобы пропустить в дальнейшем текущую итерацию
                        w = null;
                        break;
                    default:
                        //auth
                        //если это первий запуск то
                        // пытаемся получить имя зарегистрированного клиента
                        if (name.isEmpty()) {

                            //передаем логин и пароль на проверку, зарегистрированы ли они.
                            //на выходе получим имя пользователя который зареген
                            //если таких пользователей в базе 2 и больше то получим прочто первого в базе
                            //нет такого то null
                            String t = SQLHandler.getNickByLoginPassword(n[1], n[2]);
                            //есть такой клиент в базе, тогда запомним его
                            if (t != null) {
                                if (isCopyUser(t)){
                                    //Такой пользователь уже в чате
                                    sendMsg("Данный пользователь уже авторизирован!");
                                }else{
                                    //сообщим клиентам,что текущий пользователь авторизовался в чате
                                    owner.broadcastMsg("Пользователь ".concat(t).concat(" вошел в чат!"));
                                    name = t;
                                }

                            } else {
                                //в базе такого нет, вернем клиенту ошибку
                                sendMsg("Auth Error");
                                //удалим сокет клиент из списке рабочих сокетов сервера
                                owner.remove(this);
                                //закончим работать с этим клиентом
                                break;
                            }
                            //обнулим пользовательский ввод, чтобы пропустить в дальнейшем текущую итерацию
                            w = null;
                        }

                        // это уже следующая итерация, и пользователь хочет общаться
                        /*if (w != null) {
                            //всем активным клиентам на сервере отошлем сообщение этого клиента
                            owner.broadcastMsg(name + ": " + w);
                            //выведем это ссобщение и в консоль сервака
                            System.out.println(name + ": " + w);
                            //проверим а не хочет ли клиент закончить общение
                            if (w.equalsIgnoreCase("END")) {
                                //очень хочет
                                break;
                            }
                        }
                        //собственно задержка чтобы не грузить проц почем зря
                        Thread.sleep(100);*/
                }

                // это уже следующая итерация, и пользователь хочет общаться
                if (w != null) {
                    //всем активным клиентам на сервере отошлем сообщение этого клиента
                    owner.broadcastMsg(name + ": " + w);
                    //выведем это ссобщение и в консоль сервака
                    System.out.println(name + ": " + w);
                    //проверим а не хочет ли клиент закончить общение
                    if (w.equalsIgnoreCase("END")) {
                        //очень хочет
                        break;
                    }
                }
                //собственно задержка чтобы не грузить проц почем зря
                Thread.sleep(100);
            }

        } catch (IOException e) {
            System.out.println("Output Error");
        } catch (InterruptedException e) {
            System.out.println("Thread sleep error");
        }
        try {
            System.out.println("Client disconnected");
            if (!name.equals("")) owner.remove(this);
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод для отсылки сообщения клиенту
     * @param msg сообщение которое отошлет сервер клиенту в ответ
     */

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
            out.flush();
        } catch (IOException e) {
        }
    }
}
