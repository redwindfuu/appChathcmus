/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.Server.Source;

import com.Data.FileInfo;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.alexpanov.net.FreePortFinder;

import java.rmi.server.SocketSecurityException;
/**
 *
 * @author ASUS
 */
public class ServerRoom implements Runnable{
        private HashMap<String, PrintWriter> connectedClient = new HashMap<>();
        private HashMap<String, Socket> connectedFile = new HashMap<>();
	private static final int MAX_CONNECTED = 10;
	public int PORT;
	public String nameRoom;
	private ServerSocket server;
        
//        public static void main(String[] args) {
//            new ServerRoom(8082).start(true);
//        }
        
        @Override
        public void run() {
            start();
        }

	// Start of Client Handler
	private class ClientHandler implements Runnable {
		private Socket socket;
		private PrintWriter out;
		private BufferedReader in;
		private String name;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run(){
			
				System.out.println("Client connected: " + socket.getPort());
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
				out = new PrintWriter(socket.getOutputStream(), true);
				for(;;) {
					name = in.readLine();
					if (name == null) {
						return;
					}
                                        System.out.println(" ten la : "+name);
					
					if (!name.isEmpty() && !connectedClient.keySet().contains(name)) break;
					else out.println("INVALIDNAME");
					
				}
                                out.println(nameRoom);
				out.println("Welcome to the chat group, " + name.toUpperCase() + "!");
				broadcastMessage("[SYSTEM MESSAGE] " + name.toUpperCase() + " has joined.");
                                synchronized(connectedClient){
				connectedClient.put(name, out);
                                }
                                synchronized(connectedFile){
                                connectedFile.put(name,socket);
                                }
				String message;
                                out.println("You may join the chat now...");
				while ((message = in.readLine()) != null) {
					if (!message.isEmpty()) {
						if (message.equals("/quit")) break;
                                                if(message.equals("->File")){
                                                broadcastMessage_1("->File", name);
                                                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                                               
                                                FileInfo fileInfo = (FileInfo) ois.readObject();
                                                System.out.println(fileInfo.getFilename()+ " " );
                                                    for (Map.Entry<String, Socket> entry : connectedFile.entrySet()) {
                                                        if(entry.getKey() != name){
                                                        ObjectOutputStream oos = new ObjectOutputStream(entry.getValue().getOutputStream());
                                                        oos.writeObject("OK");
                                                        oos.flush();
                                                        oos.writeObject(fileInfo);
                                                        oos.flush();
                                                        }
                                                    }                                  
                                                broadcastMessage(" Đã giử file : " +fileInfo.getFilename() +" size "+fileInfo.getDataBytes().length +" byte ");
                                                }
                                                else {
                                                    broadcastMessage(message);}
                                                message ="";
					}
				}
			} catch (Exception e) {
                                    System.out.println(e);
			} finally {
				if (name != null) {
                                         System.out.println(name + " is leaving");
					connectedClient.remove(name);
                                    try {
                                        connectedFile.get(name).close();
                                    } catch (IOException ex) {
                                        Logger.getLogger(ServerRoom.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                        connectedFile.remove(name);
					broadcastMessage(name + " has left");
				}
                                if(connectedClient.isEmpty()){
                                    try {
                                        if(server != null)
                                            server.close();
                                    } catch (IOException ex) {
                                        Logger.getLogger(ServerRoom.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                  
                                }
                                
			}
		}

	}
        
         private boolean createFile(FileInfo fileInfo) throws IOException {
        BufferedOutputStream bos = null;
         
        try {
            if (fileInfo != null) {
                File fileReceive = new File(fileInfo.getDestinationDirectory() 
                        + fileInfo.getFilename());
                bos = new BufferedOutputStream(
                        new FileOutputStream(fileReceive));
                // write file content
                bos.write(fileInfo.getDataBytes());
                bos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
             if (bos != null) {
                bos.close();
            }
        }
        return true;
    }
        
        
        
        
        
	private void broadcastMessage(String message) {
		for (PrintWriter p: connectedClient.values()) {
			p.println(message);
                        p.flush();
		}
	}
        private void broadcastMessage_1(String message,String name) {
		for (PrintWriter p: connectedClient.values()) {
                    if(!connectedClient.get(name).equals(p))
			p.println(message);
                        p.flush();
		}
	}

        
        
        
        private class ServerHandle implements Runnable{
                private ServerSocket server; 
                private int port;   
                

                public ServerHandle(ServerSocket soc , int po) {
                    this.server = soc;
                    port = po;
                }


                @Override
                public void run() {//To change body of generated methods, choose Tools | Templates.

                    try {
                        this.server = new ServerSocket(port);
                        System.out.println("Server started on port: " + port);
                        System.out.println("Now listening for connections...");

                        for(;;) {
                            if (connectedClient.size() <= MAX_CONNECTED){
                                Thread newClient = new Thread(new ClientHandler(server.accept()));
                                newClient.start();
                            }
                        }
                        }
                        catch (Exception e) {

                                        System.out.println("\nError occured: \n");
                                        e.printStackTrace();
                                        System.out.println("\nExiting...");
                        try {
                            stop();
                        } catch (IOException ex) {
                            Logger.getLogger(ServerRoom.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        }

                }
                public void stop() throws IOException {
                        if (!server.isClosed()) server.close();
                }

        }
        
	public void start() {
		new Thread(new ServerHandle(server , PORT)).start();
	}
	
	private int getRandomPort() {
		int port = FreePortFinder.findFreeLocalPort();
		PORT = port;
		return port;
	}
    public ServerSocket getserver(){
        return server;
    }
    public ServerRoom(int port , String nameR) {
    
        PORT = port;
        nameRoom = nameR;
    }
    
}
