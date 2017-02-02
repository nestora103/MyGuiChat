package ru.geekbrains.java2.dz.dz7.gubenkoDM.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientService extends JFrame {

    JTextField jtf;
    JTextArea jta;

    final String SERVER_ADDR = "localhost";
    final int SERVER_PORT = 8189;
    Socket sock;
    DataInputStream in;
    DataOutputStream out;

    public ClientService() {
        setBounds(600, 300, 500, 500);
        setTitle("Client");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jta = new JTextArea();
        jta.setEditable(false);
        jta.setLineWrap(true);
        JScrollPane jsp = new JScrollPane(jta);
        add(jsp, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel userInterfPanel=new JPanel(new BorderLayout());

        JPanel addNewUserPanel=new JPanel(new GridLayout());
        JTextField jtfLoginForAdd = new JTextField("Login");
        JTextField jtfPassForAdd = new JTextField("Password");
        JTextField jtfNickForAdd = new JTextField("Nickname");
        JButton jbAddUser = new JButton("Add user");
        addNewUserPanel.add(jtfLoginForAdd);
        addNewUserPanel.add(jtfPassForAdd);
        addNewUserPanel.add(jtfNickForAdd);
        addNewUserPanel.add(jbAddUser);

        userInterfPanel.add(addNewUserPanel, BorderLayout.NORTH);

        JPanel setPassPanel=new JPanel(new GridLayout());
        JTextField jtfLoginForSet = new JTextField("Login");
        JTextField jtfNewPass = new JTextField("New password");
        JButton jbSet = new JButton("Set pass");
        setPassPanel.add(jtfLoginForSet);
        setPassPanel.add(jtfNewPass);
        setPassPanel.add(jbSet);

        userInterfPanel.add(setPassPanel, BorderLayout.CENTER);

        JPanel authPanel = new JPanel(new GridLayout());
        JTextField jtfLogin = new JTextField("Login");
        JTextField jtfPass = new JTextField("Password");
        JButton jbAuth = new JButton("Auth");
        authPanel.add(jtfLogin);
        authPanel.add(jtfPass);
        authPanel.add(jbAuth);

        userInterfPanel.add(authPanel, BorderLayout.SOUTH);


        //предустановленный текст логина
        jtfLogin.setToolTipText("Login");


        add(userInterfPanel, BorderLayout.NORTH);
        //add(authPanel, BorderLayout.NORTH);


        //обработка кнопки добавления нового пользователя
        jbAddUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewUser("add\t" + jtfLoginForAdd.getText() + "\t" + jtfPassForAdd.getText()+ "\t" + jtfNickForAdd.getText());
            }
        });

        //обработка кнопки установки нового пароля у существующего пользователя
        jbSet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNewPass("setPass\t" + jtfLoginForSet.getText() + "\t" + jtfNewPass.getText());
            }
        });

        //обработка кнопки авторизации
        jbAuth.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connect("auth\t" + jtfLogin.getText() + "\t" + jtfPass.getText());
            }
        });

        add(bottomPanel, BorderLayout.SOUTH);
        JButton jbSend = new JButton("SEND");
        bottomPanel.add(jbSend, BorderLayout.EAST);
        jtf = new JTextField();
        bottomPanel.add(jtf, BorderLayout.CENTER);

        jbSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!jtf.getText().trim().isEmpty()) {
                    sendMsg();
                    jtf.grabFocus();
                }
            }
        });

        jtf.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMsg();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                try {
                    out.writeUTF("end");
                    out.flush();
                    out.close();
                    in.close();
                } catch (IOException exc) {
                } finally {
                    try {
                        sock.close();
                    } catch (IOException ex) {
                    }
                }
            }
        });

        setVisible(true);
    }


    /**
     * Метод добавления нового пользователя в базу
     * @param cmd спец строка с логином и паролем пользователя для добавления
     */

    public void addNewUser(String cmd){
        //проверим производилась ли работа с сервером до нажатия на кнопку. закрыт ли сокет собственно?
        if (sock==null){
            try {
                sock = new Socket(SERVER_ADDR, SERVER_PORT);
                in = new DataInputStream(sock.getInputStream());
                out = new DataOutputStream(sock.getOutputStream());
                //out.writeUTF(cmd);
                //out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try{
            out.writeUTF(cmd);
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String w = in.readUTF();
                        if (w != null) {
                            if (w.equalsIgnoreCase("end session")) break;
                            jta.append(w);
                            jta.append("\n");
                            jta.setCaretPosition(jta.getDocument().getLength());
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }


    /**
     * Метод установки нового пароля у переданного пользователя
     * @param cmd спец строка с логином и паролем пользователя для установки нового пароля
     */

    public void setNewPass(String cmd){
        //проверим производилась ли работа с сервером до нажатия на кнопку. закрыт ли сокет собственно?
        if (sock==null){ //isClosed()
            try {
                sock = new Socket(SERVER_ADDR, SERVER_PORT);
                in = new DataInputStream(sock.getInputStream());
                out = new DataOutputStream(sock.getOutputStream());
                //out.writeUTF(cmd);
                //out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        try{
            out.writeUTF(cmd);
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String w = in.readUTF();
                        if (w != null) {
                            if (w.equalsIgnoreCase("end session")) break;
                            jta.append(w);
                            jta.append("\n");
                            jta.setCaretPosition(jta.getDocument().getLength());
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                }
            }
        }).start();
    }


    /**
     * Метод авторизации пользователя
     * @param cmd спец строка с логином и паролем пользователя для авторизации
     */

    public void connect(String cmd) {

        //проверим производилась ли работа с сервером до нажатия на кнопку. закрыт ли сокет собственно?
        if (sock==null){
            try {
                sock = new Socket(SERVER_ADDR, SERVER_PORT);
                in = new DataInputStream(sock.getInputStream());
                out = new DataOutputStream(sock.getOutputStream());
                //out.writeUTF(cmd);
                //out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };

        try{
            out.writeUTF(cmd);
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String w = in.readUTF();
                        if (w != null) {
                            if (w.equalsIgnoreCase("end session")) break;
                            jta.append(w);
                            jta.append("\n");
                            jta.setCaretPosition(jta.getDocument().getLength());
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                }
            }
        }).start();



        /*try {
            sock = new Socket(SERVER_ADDR, SERVER_PORT);
            in = new DataInputStream(sock.getInputStream());
            out = new DataOutputStream(sock.getOutputStream());
            out.writeUTF(cmd);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String w = in.readUTF();
                        if (w != null) {
                            if (w.equalsIgnoreCase("end session")) break;
                            jta.append(w);
                            jta.append("\n");
                            jta.setCaretPosition(jta.getDocument().getLength());
                        }
                        Thread.sleep(100);
                    }
                } catch (Exception e) {
                }
            }
        }).start(); */


    }

    /**
     *  Метод по отправке сообщения на сервер
     */

    public void sendMsg() {
        try {
            String a = jtf.getText();
            out.writeUTF(a);
            out.flush();
            jtf.setText("");
        } catch (IOException e) {
            System.out.println("Send msg error");
        }
    }
}
