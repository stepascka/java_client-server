package server;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

class ServerSomthing extends Thread {
    
    private Socket socket; // �����, ����� ������� ������ �������� � ��������
    private BufferedReader in; // ����� ������ �� ������
    private BufferedWriter out; // ����� ������ � �����
    
    public ServerSomthing(Socket socket) throws IOException {
        this.socket = socket;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        //TCP_server.story.printStory(out); // ����� ������ ��������� ��� �������� ������� ��������� 10
        start(); // �������� run()
    }
    
    @Override
    public void run() {
        String word;
        try {
            // ������ ��������� ������������ ���� - ��� �������
            word = in.readLine();
            try {
                out.write(word + "\n");
                out.flush(); // flush() ����� ��� ������������ ���������� ������
                // ���� ����� ����, � ������� ������ ��� �������� ����
            } catch (IOException ignored) {}
            try {
                while (true) {
                    word = in.readLine();
                    if(word.equals("stop")) {
                        this.downService(); // ��������
                        break; // ���� ������ ������ ������ - ������� �� ����� ���������
                    }
                    System.out.println("Echoing: " + word);
                    TCP_server.story.addStoryEl(word);
                    for (ServerSomthing vr : TCP_server.serverList) {
                        vr.send(word); // �������� �������� ��������� � ������������ ������� ���� ��������� ������ ���
                    }
                }
            } catch (NullPointerException ignored) {}

            
        } catch (IOException e) {
            this.downService();
        }
    }
    
    /**
     * ������� ������ ��������� ������� �� ���������� ������
     * @param msg
     */
    private void send(String msg) {
        try {
            out.write(msg + "\n");
            out.flush();
        } catch (IOException ignored) {}
        
    }
    
    /**
     * �������� �������
     * ���������� ���� ��� ���� � �������� �� ������ �����
     */
    private void downService() {
            try {
            if(!socket.isClosed()) {
                socket.close();
                in.close();
                out.close();
                for (ServerSomthing vr : TCP_server.serverList) {
                    if(vr.equals(this)) vr.interrupt();
                    TCP_server.serverList.remove(this);
                }
            }
        } catch (IOException ignored) {}
    }
}

/**
 * ����� �������� � ��������� ���������
 * ������ ���������� � ��������� 10 (��� ������) ����������
 */

class Story {
    
    private LinkedList<String> story = new LinkedList<>();
    
    public void addStoryEl(String el) { //���������� ��������� � ������

    	story.add(el);
    }
    
    public void printStory(BufferedWriter writer) { //�������� ��������������� ������ ��������� �� ������ � ����� ������ ������� �������
    	
        if(story.size() > 0) {
            try {
                writer.write("History messages" + "\n");
                for (String vr : story) {
                    writer.write(vr + "\n");
                }
                writer.write("/...." + "\n");
                writer.flush();
            } catch (IOException ignored) {}
            
        }
        
    }
}

public class TCP_server {

    public static final int PORT = 8080;
    public static LinkedList<ServerSomthing> serverList = new LinkedList<>(); // ������ ���� ����� - �����������
    // �������, ��������� ������ ������ �������
    public static Story story; // ������� ���������
    
    public static void main(String[] args) throws IOException {
    	
        ServerSocket server = new ServerSocket(PORT);
        story = new Story();
        System.out.println("Server Started");
        try {
            while (true) {
                // ����������� �� ������������� ������ ����������:
                Socket socket = server.accept();
                try {
                    serverList.add(new ServerSomthing(socket)); // �������� ����� ����������� � ������
                } catch (IOException e) {
                    // ���� ���������� ��������, ����������� �����,
                    // �����, ���� ������� ���
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}