package com.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

public class CommandExecutor {

	private Process process;
	private PrintWriter pushingPipe;
	private String commands[] ;
	private int numOfCommands = 0;
	private SyncPipe errorSyncPipe;
	private SyncPipe inputSyncPipe;
	/**
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public CommandExecutor(String application) 
			throws IOException, InterruptedException {
		
	    process = Runtime.getRuntime().exec(application);
	    
	    errorSyncPipe = new SyncPipe(process.getErrorStream(), System.err);
	    new Thread(errorSyncPipe).start();
	    
	    inputSyncPipe = new SyncPipe(process.getInputStream(), System.out);
	    new Thread(inputSyncPipe).start();
	    
	    pushingPipe = new PrintWriter(process.getOutputStream());
	    commands = new String[50];
	}
	
	/**
	 * @param command
	 */
	public void append(String command) {
		commands[ numOfCommands ] = command;
		numOfCommands++;
	}
	
	/**
	 * Executes all the commands appended using appen().
	 * @return <b>true</b> if successful, <b>false</b> otherwise.
	 * @throws InterruptedException
	 */
	public boolean runCommands() throws InterruptedException {
		for (int i = 0; i < numOfCommands; i++) {
			if (commands[i].equals(""))
				pushingPipe.println();
			else
				pushingPipe.println( commands[i] );
			
		}
		pushingPipe.flush();
		pushingPipe.close();
	    int returnCode = process.waitFor();
	    boolean errorFound = errorSyncPipe.getOutput() != null;
	    System.out.println( "\nReturn code = " + returnCode +"\nError found:"+errorFound);
	    
	    return returnCode == 0 && errorFound == false;
	}
	

}
class SyncPipe implements Runnable
{
	private final OutputStream outputBoard;
	private final InputStream inputSource;
	private byte output[];

	/**
	 * @param istrm
	 * @param ostrm
	 */
	public SyncPipe(InputStream istrm, OutputStream ostrm) {
		inputSource = istrm;
		outputBoard = ostrm;
		output = null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try
		{
			final byte[] buffer = new byte[1024];
			for (int length = 0; (length = inputSource.read(buffer)) != -1; )
			{
				outputBoard.write(buffer, 0, length);
				if(output == null)
					output = buffer;
			}
			outputBoard.flush();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public byte[] getOutput() {
		return output;
	}
}