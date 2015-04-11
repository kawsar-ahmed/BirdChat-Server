package com.socket;

import java.io.*;
import java.net.*;
import java.sql.SQLException;

import com.ui.ServerUI;

/**
 * ClientThread extends Thread class. When a new user is connected to the server
 * ,a new dedicated ClientThread is alloted for him/her . It handles incoming
 *  and outgoing messages of the associated user.
 * @author Kawsar Ahmed
 * @author Abu Raihan
 *
 */
class ClientThread extends Thread { 
	
    public SocketServer socketServer = null;
    public Socket socket = null;
    public String ID ;
    public String username = "";
    public ObjectInputStream streamIn  =  null;
    public ObjectOutputStream streamOut = null;
    public ServerUI serverUI;
    private boolean clientShouldLive = true;

    /**
     * @param _server
     * @param _socket
     */
    public ClientThread(SocketServer _server, Socket _socket){  
    	super();
    	socketServer = _server;
        socket = _socket;
        ID     = socket.getInetAddress().getHostAddress()+":"+socket.getPort();
        serverUI = _server.serverUI;
    }
    
    /**
     * Sends a message
     * @param msg
     */
    public void send(Message msg){
        try {
            streamOut.writeObject(msg);
            streamOut.flush();
        } 
        catch (IOException ex) {
            System.out.println("Exception [SocketClient : send(...)]");
        }
    }
    
    /**
     * gets the id of the client thread
     * @return
     */
    public String getID(){  
	    return ID;
    }
   
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run(){  
    	serverUI.display("Client Thread "+ ID + " running.");
        while (clientShouldLive){  
    	    try{  
                Message msg = (Message) streamIn.readObject();
                socketServer.handle(ID, msg);
            }
            catch(Exception ioe){  
            	System.out.println(ID + "- ERROR reading: " + ioe.getMessage());
            	socketServer.remove(ID);
                return;
            }
        }
    }
    
    /**
     * set the value of clientShouldLive
	 * @param clientShouldLive the clientShouldLive to set
	 */
	public void setClientShouldLive(boolean clientShouldLive) {
		this.clientShouldLive = clientShouldLive;
	}

	/**
	 * Creates input and output streams using sockets input and output streams
	 * @throws IOException
	 */
	public void open() throws IOException {  
        streamOut = new ObjectOutputStream(socket.getOutputStream());
        streamOut.flush();
        streamIn = new ObjectInputStream(socket.getInputStream());
    }
    
    /**
     * Closes both socket and input-output streams
     * @throws IOException
     */
    public void close() throws IOException {  
    	if (socket != null)    socket.close();
        if (streamIn != null)  streamIn.close();
        if (streamOut != null) streamOut.close();
    }
}





/**
 * The SocketServer class is responsible for creating and serving the 
 * main ServerSocket. It generates a dedicated ClientThread for each 
 * user connects to server. It maintains all the users: adds user, 
 * removes user etc. It is also responsible for handling messages and
 * pass the message to the appropriate ClientThread. It also able to 
 * restart and shutdown the server.
 * 
 * @author Kawsar Ahmed
 * @author Abu Raihan
 *
 */
public class SocketServer implements Runnable {
    
	public ClientThread allClientThreads[];
	public ServerSocket serverSocket = null;
	public Thread 		thread = null;
	public int 			clientCount = 0, port = 13000;
	public ServerUI 	serverUI;
	public Database 	db;
	private int 		dbPort = 3306;
	private boolean 	serverShouldLive = true;
	
	/**
	 * @param frame
	 * @throws Exception 
	 */
	public SocketServer(ServerUI frame) throws Exception{
	   
		this(frame, 0);
	}
    
    /**
     * creates the main ServerSocket and initializes database connection.
     * @param frame server window frame
     * @param Port MySQL database server port
     * @throws Exception 
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public SocketServer(ServerUI frame, int definedPort) throws Exception {
	    if (definedPort != 0 )
	    	port = definedPort;
		serverSocket = new ServerSocket(port);
        port = serverSocket.getLocalPort();
		
        try {
			db = new Database( InetAddress.getLocalHost().getHostAddress(), dbPort , "root", null);
		} catch (ClassNotFoundException | UnknownHostException | SQLException e) {
			
			try { serverSocket.close();	} 
			catch (IOException e2) { e2.printStackTrace();}
			
			throw new Exception("Problem connecting Database: " + e.getMessage());  
		}
        allClientThreads = new ClientThread[50];

        serverUI = frame;
        serverUI.display("Server started. IP : " + InetAddress.getLocalHost() + ", Port : " + serverSocket.getLocalPort());
		
        start();   
    }
	
    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run(){  
	while (thread != null && serverShouldLive){  
            try{  
		serverUI.display("Waiting for a client ..."); 
	        addThread(serverSocket.accept()); 
	    }
	    catch(Exception ioe){ 
	    	if( serverShouldLive ) {
	    		serverUI.display("Server accept error: ");
	    		serverUI.RetryStart(0);
	    		
	    	}
	    }
        }
    }
	
    /**
     * sets the value of serverShouldLive variable, which controls the server.
	 * @param serverShouldLive the serverShouldLive to set
	 */
	public void setServerShouldLive(boolean serverShouldLive) {
		this.serverShouldLive = serverShouldLive;
	}

	/**
	 * Starts the server Thread
	 */
	public void start(){  
    	if (thread == null){  
            thread = new Thread(this); 
	    thread.start();
    	}
    }
    
    /**
     * Stops the server Thread
     */
    public void stop(){  
        if (thread != null){  
            setServerShouldLive(false); 
	    thread = null;
	}
    }
    
    /**
     * Finds client using the ID. 
     * @param iD user id.iD forms as {@code <user ip>:<user port>} , 
     * like 192.168.10.2:55087
     * @return
     */
    private int findClient(String iD){  
    	for (int i = 0; i < clientCount; i++){
        	if (allClientThreads[i].getID().equals(iD) ){
                    return i;
                }
	}
	return -1;
    }
	
    /**
     * Handles all the incoming and outgoing messages and forwards 
     * to the appropriate user
     * @param iD user iD.iD forms as {@code <user ip>:<user port>} , 
     * like 192.168.10.2:55087
     * @param msg the message
     * @throws SQLException
     */
    public synchronized void handle(String iD, Message msg) throws SQLException{  
		if (msg.type.equals("signout")){
	            Announce("signout", "SERVER", msg.sender);
	            remove(iD); 
		}
		else{
	            if(msg.type.equals("login")){
	                if(findUserThread(msg.sender) == null){
	                    if(db.checkLogin(msg.sender, msg.content)){
	                    	allClientThreads[findClient(iD)].username = msg.sender;
	                    	serverUI.display("'"+msg.sender+"' has logged in.");
	                        allClientThreads[findClient(iD)].send(new Message("login", "SERVER", "TRUE", msg.sender));
	                        Announce("newuser", "SERVER", msg.sender);//r/		type	sender	content		recipient
	                        SendUserList(msg.sender);
	                    }
	                    else{
	                    	allClientThreads[findClient(iD)].send(new Message("login", "SERVER", "FALSE", msg.sender));
	                    } 											//r/		type		sender		content		recipient
	                }
	                else{
	                	allClientThreads[findClient(iD)].send(new Message("login", "SERVER", "DUPLICATE", msg.sender));
	                }										//r/		type		sender		content		recipient
	            }
	            else if(msg.type.equals("message")){
	                if(msg.recipient.equals("All")){
	                    Announce("message", msg.sender, msg.content);
	                }
	                else{
	                    findUserThread(msg.recipient).send(new Message(msg.type, msg.sender, msg.content, msg.recipient));
	                    allClientThreads[findClient(iD)].send(new Message(msg.type, msg.sender, msg.content, msg.recipient));
	                }											//r/		type		sender		content		recipient
	            }
	            else if(msg.type.equals("test")){
	            	allClientThreads[findClient(iD)].send(new Message("test", "SERVER", "OK", msg.sender));
	            }											//r/		type		sender		content		recipient
	            else if(msg.type.equals("signup")){
	                if(findUserThread(msg.sender) == null){
	                    if(!db.userExists(msg.sender)){
	                        db.addUser(msg.sender, msg.content,"");
	                        allClientThreads[findClient(iD)].username = msg.sender;
	                        allClientThreads[findClient(iD)].send(new Message("signup", "SERVER", "TRUE", msg.sender));
	                        allClientThreads[findClient(iD)].send(new Message("login", "SERVER", "TRUE", msg.sender));
	                        Announce("newuser", "SERVER", msg.sender);//r/		type		sender		content		recipient
	                        SendUserList(msg.sender);
	                    }
	                    else{
	                    	allClientThreads[findClient(iD)].send(new Message("signup", "SERVER", "FALSE", msg.sender));
	                    }											//r/		type		sender		content		recipient
	                }
	                else{
	                	allClientThreads[findClient(iD)].send(new Message("signup", "SERVER", "FALSE", msg.sender));
	                }											//r/		type		sender		content		recipient
	            }
	            else if(msg.type.equals("upload_req")){
	                if(msg.recipient.equals("All")){
	                	allClientThreads[findClient(iD)].send(new Message("message", "SERVER", "Sending file to 'All' is forbidden", msg.sender));
	                }											//r/		type		sender		content		recipient
	                else{
	                    findUserThread(msg.recipient).send(new Message("upload_req", msg.sender, msg.content, msg.recipient));
	                }
	            }
	            else if(msg.type.equals("upload_res")){
	                if(!msg.content.equals("NO")){
	                    String IP = findUserThread(msg.sender).socket.getInetAddress().getHostAddress();
	                    findUserThread(msg.recipient).send(new Message("upload_res", IP, msg.content, msg.recipient));
	                }										//r/		type		sender		content		recipient
	                else{
	                    findUserThread(msg.recipient).send(new Message("upload_res", msg.sender, msg.content, msg.recipient));
	                }
	            }
		}
    }
    
    /**
     * Server uses this method to broadcast a message to all users
     * @param type the message type
     * @param sender the sender
     * @param content the message content
     */
    public void Announce(String type, String sender, String content){
        Message msg = new Message(type, sender, content, "All");
        				//r/		type		sender		content		recipient
        for(int i = 0; i < clientCount; i++){
        	allClientThreads[i].send(msg);
        }
    }
    
    /**
     * Sends all the logged in user's usernames to a specified user
     * @param toWhom the specified receiver user 
     */
    public void SendUserList(String toWhom){
        for(int i = 0; i < clientCount; i++){
            findUserThread(toWhom).send(new Message("newuser", "SERVER", allClientThreads[i].username, toWhom));
        }									//r/		type		sender		content		recipient
    }
    
    /**
     * Finds the ClientThread using the specified username {@code usr}.
     * @param usr username
     * @return
     */
    public ClientThread findUserThread(String usr){
        for(int i = 0; i < clientCount; i++){
            if(allClientThreads[i].username.equals(usr)){
                return allClientThreads[i];
            }
        }
        return null;
    }
	
    /**
     * Removes the specified client using the iD
     * @param iD user iD.iD forms as {@code <user ip>:<user port>} , 
     * like 192.168.10.2:55087
     */
    public synchronized void remove(String iD){  
    int pos = findClient(iD);
        if (pos >= 0){  
        	ClientThread toTerminate = allClientThreads[pos];
            serverUI.display("Removing client \'"+allClientThreads[pos].username+"\' : thread " + iD + " at " + pos);
	    if (pos < clientCount-1){
                for (int i = pos+1; i < clientCount; i++){
                	allClientThreads[i-1] = allClientThreads[i];
	        }
	    }
	    clientCount--;
	    try{  
	      	toTerminate.close(); 
	    }
	    catch(IOException ioe){  
	      	serverUI.display("Error closing thread: " + ioe); 
	    }
	    toTerminate.setClientShouldLive(false); 
	}
    }
    
    /**
     * Adds a clientThread for the new logged in user.
     * @param socket the socket to which the user is connected
     */
    private void addThread(Socket socket){  
	if (clientCount < allClientThreads.length){  
            serverUI.display("Client accepted: " + socket);
            allClientThreads[clientCount] = new ClientThread(this, socket);
	    try{  
	    	allClientThreads[clientCount].open(); 
	      	allClientThreads[clientCount].start();  
	        clientCount++;
	    }
	    catch(IOException ioe){  
	      	serverUI.display("Error opening thread: " + ioe); 
	    } 
	}
	else{
            serverUI.display("Client refused: maximum " + allClientThreads.length + " reached.");
	}
    }
    
    /**
     * Removes all the users and shuts down the server. 
     * Closes all the sockets and input-output streams, stops all the threads 
     */
    public void shutdown () {
    	serverUI.display("Shutting down the Server...");
    	for (int i = 0; i < clientCount; i++) {
    		serverUI.display("Removing client thread " + allClientThreads[i].username + " at " + i);
			
    		try{  
    			allClientThreads[i].close(); 
    		}
    		catch(IOException ioe){  
    			serverUI.display("Error closing thread: " + ioe); 
    		}
    		allClientThreads[i].setClientShouldLive(false); 
		}
    	try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	stop();
    	serverUI.display("Server has been shut down.");
    }
}
