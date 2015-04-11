import java.io.IOException;

import com.socket.CommandExecutor;


public class Tester {

	/**
	 * @param args
	 * @throws InterruptedException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		CommandExecutor cmdPrompt = new CommandExecutor("cmd");
		cmdPrompt.append("\"D:\\xampplite\\mysql\\bin\\mysql.exe\" -uroot -ppass --execute=\"GRANT ALL ON birdchat.* TO 'root'@'rony-cse-bd';\"");
//		cmdPrompt.append("cd D:\\xampplite\\mysql\\bin\\");
//		cmdPrompt.append("mysql -u root -p pass;");
//		\"D:\\xampplite\\mysql\\bin\\mysql.exe\" -uroot -ppass --execute=\"GRANT ALL ON birdchat.* TO 'root'@'localhost';\"
		cmdPrompt.runCommands();
	}

}
