package org.boblycat.abbots.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import org.boblycat.abbots.Board;
import org.boblycat.abbots.Solver;

public class Client {
    private PlainTalkParser parser;
    private PlainTalk plainTalk;
    private InputStream sockIn;
    private OutputStream sockOut;
    private int requestId;
    private Queue<List<String>> receivedMessages;
    private byte[] receiveBuf;
    
    public Client(Socket sock) throws IOException {
        Charset utf8 = Charset.forName("UTF-8");
        sockIn = new BufferedInputStream(sock.getInputStream());
        sockOut = new BufferedOutputStream(sock.getOutputStream());
        receiveBuf = new byte[1024];
        plainTalk = new PlainTalk(utf8);
        receivedMessages = new ArrayDeque<List<String>>();
        parser = new PlainTalkParser(utf8) {
            public void receivedMessage(List<String> message) {
                //System.out.println("Received: " + message);
                receivedMessages.add(message);
            }
        };
    }
    
    private void readResponse() throws IOException {
        int rlength = sockIn.read(receiveBuf);
        parser.receivedData(receiveBuf, rlength);
    }
    
    private List<String> nextResponse() throws IOException {
        List<String> received = receivedMessages.poll();
        while (received == null) {
            readResponse();
            received = receivedMessages.poll();
        }
        return received;
    }
    
    public List<String> sendAndReceive(String... message) throws IOException {
        ArrayList<String> msg = new ArrayList<String>();
        String id = String.valueOf(requestId++);
        msg.add(id);
        for (String s: message)
            msg.add(s);
        byte[] bytesToSend = plainTalk.messageBytes(msg);
        System.out.println("Sending: " + msg);
        sockOut.write(bytesToSend);
        sockOut.flush();
        while (true) {
            List<String> response = nextResponse();
            if (response.size() == 0)
                throw new ProtocolException("Received zero-length message");
            String respId = response.get(0);
            if (respId.equals("*"))
                System.out.println("Ignoring message: " + response);
            else if (respId.equals(id)) {
                if (response.size() < 2 || !response.get(1).equals("ok"))
                    throw new ProtocolException("No 'ok' in response: " + response);
                return response.subList(2, response.size());
            }
            else
                throw new ProtocolException("Unknown message: " + response);
        }
    }
    
    public Board start() throws IOException {
        List<String> resp = sendAndReceive("start");
        if (resp.size() != 1)
            throw new ProtocolException("Error getting board: " + resp);
        Board board = new Board();
        board.parse(resp.get(0));
        return board;
    }
    
    public String solve(String solution) throws IOException {
        List<String> resp = sendAndReceive("solve", solution);
        if (resp.size() != 1)
            throw new ProtocolException("Error in solve: " + resp);
        return resp.get(0);
    }
    
    public static void main(String[] args) throws IOException {
        Socket sock = new Socket("munkeliv.ath.cx", 50000);
        Client client = new Client(sock);
        client.sendAndReceive("iama", "Oracle-powered Evil Daemon");
        Board board = client.start();
        System.out.println("Got board:\n" + board);
        Solver solver = new Solver(board);
        String solution = solver.solve("");
        //System.out.println("Solution: " + solution);
        String servertime = client.solve(solution);
        System.out.println("Server time: " + servertime + " ms");
    }
}
