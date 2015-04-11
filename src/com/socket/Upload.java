package com.socket;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.imageio.ImageIO;

import com.ui.ServerUI;

public class Upload implements Runnable{

    public String addr;
    public int port;
    public Socket socket;
    public FileInputStream In;
    public OutputStream Out;
    public File file;
    public ServerUI ui;
    private boolean imagetransfer = false;
	private BufferedImage bufferedImage;
    
    public Upload(String addr, int port, File filepath, ServerUI frame){
        super();
        try {
            file = filepath; ui = frame;
            socket = new Socket(InetAddress.getByName(addr), port);
            Out = socket.getOutputStream();
            In = new FileInputStream(filepath);
        } 
        catch (Exception ex) {
            System.out.println("Exception [Upload : Upload(...)]");
        }
    }
    public Upload(String addr, int port, BufferedImage bimg){
        super();
        try {
        	bufferedImage = bimg; 
            socket = new Socket(InetAddress.getByName(addr), port);
            Out = socket.getOutputStream();
        } 
        catch (Exception ex) {
            System.out.println("Exception [Upload : Upload(...)]");
        }
        imagetransfer = true;
    }
    
    @Override
    public void run() {
        try {   
        	if (imagetransfer){
                ImageIO.write(bufferedImage,"JPG",socket.getOutputStream());
        	} else {
	        		
	            byte[] buffer = new byte[1024];
	            int count;
	            
	            while((count = In.read(buffer)) >= 0){
	                Out.write(buffer, 0, count);
	            }
	            Out.flush();
	            
	            ui.display("[Applcation > Me] : File upload complete\n");
	            
	            if(In != null){ In.close(); }
	        }
        	if(Out != null){ Out.close(); }
        	if(socket != null){ socket.close(); 
        	}
        	
        }
        catch (Exception ex) {
            System.out.println("Exception [Upload : run()]");
            ex.printStackTrace();
        }
    }

}