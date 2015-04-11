package com.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.socket.CommandExecutor;
import com.socket.SocketServer;

/**
 * @author Kawsar
 *
 */
public class ServerUI extends JFrame {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public SocketServer server;
    public Thread serverThread;
    private String mySQLServerPath = "D:\\xampplite\\mysql\\bin\\mysql.exe";
    private boolean serverIsRunning = false;
	private final Color deepBlue = new Color(12, 125, 175);
	private final Color blue = new Color(0, 175, 240);
	private final Color lightBlue = new Color(204, 239, 252);
	private final Color white = new Color(255, 255, 255) ;
	private Icon backgroundImage;
	private int uiWidth;
	private int uiHeight;
	private Font consolas12 = new Font("Consolas", 0, 12);
	private Date logtime;
	private JLabel loadingIconLabel;
	
	
	/**
	 * Application Starts here
	 * @param args
	 */
	public static void main(String args[]) {
	
	  
	    
	    java.awt.EventQueue.invokeLater(new Runnable() {
	        public void run() {
	            try {
					new ServerUI().setVisible(true);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, "Background Image file not found.");
					e.printStackTrace();
				}
	        }
	    });
	}

	/**
	 * Constractor of ServerUI class. initilize , build and organize ui components.
	 * @throws IOException
	 */
	public ServerUI() throws IOException {
		init();
        buildComponents();
		addComponents();
        addWindowListener();
    }
    
	/**
	 * Initializes the ui frame and set application icon 
	 * and gets the background image
	 * @throws IOException
	 */
	private void init() throws IOException{
		setTitle("Birdchat Server");
		
		// the path must be relative to your *class* files
		String imagePath = "images/Birdchat-icon.png";
		InputStream imgStream = getClass().getResourceAsStream(imagePath );
		BufferedImage myImg = ImageIO.read(imgStream);
		setIconImage(myImg);
		
		String filePath = "images/Birdchat-screen-server.png";
		backgroundImage = new ImageIcon(getClass().getResource(filePath));
		uiWidth = backgroundImage.getIconWidth();
		uiHeight = backgroundImage.getIconHeight();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds( 0, 0, uiWidth+5, uiHeight+28);
		setLocationRelativeTo(null);
		setLayout(null);
		setResizable(false);
	}
	
	    /**
	     * create components
	     * 
	     */
	    private void buildComponents() {
			
	    	panel = new JPanel();
	//    	panel.setBounds(0, 0, 718, 500);
	    	panel.setBounds( 0, 0, uiWidth, uiHeight);
	    	panel.setLayout(null);
	    	
	    	backgroundImageLabel = new JLabel(backgroundImage);
			backgroundImageLabel.setBounds(0, 0, uiWidth, uiHeight);
			backgroundImageLabel.setOpaque(false);
			
			String filePath = "images/loader-blue-smallest.gif";
			Icon icon = new ImageIcon(getClass().getResource(filePath));
			loadingIconLabel = new JLabel(icon);
			loadingIconLabel.setBounds(uiWidth-320, 12, 45, 45);
			loadingIconLabel.setOpaque(false);
			//panel.add(loadingIconLabel);
			
			createStartStopServerButton();
	
			createGrantAccessLabel();
			
			prepareDisplayArea();
			
			prepareFileChooser();
		}

	/**
	 * Add created components to the panel
	 */
	private void addComponents() {
		add(panel);
		panel.add(grantAccessLabel);
		panel.add(startStopServerButton);
		panel.add(displayScrollPane);
		
		//r/ must be added last, otherwise other components will go under the background
		panel.add(backgroundImageLabel);
	}

	/**
	 * checks wheather the OS is windows
	 * @return <b>true</b> if OS is windows <b>false</b> otherwise
	 */
	public boolean isWin32(){
        return System.getProperty("os.name").startsWith("Windows");
    }

    /**
     * Starts the server if it is not stared yet, stops otherwise
     * @param evt
     */
    private void startStopServerButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	panel.add(loadingIconLabel);
    	panel.add(backgroundImageLabel);
    	panel.updateUI();
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (serverIsRunning){
					server.shutdown();
					startStopServerButton.setText("Start Server");
					serverIsRunning = false;
				} else {
					
					if (startServer() == true) {
						startStopServerButton.setText("Stop Server");
						serverIsRunning = true;
					}
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				panel.remove(loadingIconLabel);
		    	panel.updateUI();
			}
		}).start();
	}

	/**
	 * @param port
	 */
	public void RetryStart(int port) {
	    if(server != null){ server.shutdown(); }
	    if (startServer() == true) {
	    	startStopServerButton.setText("Stop Server");
	    	serverIsRunning = true;
	    }
	    else {
	    	if(server != null){ server.shutdown(); }
	    	startStopServerButton.setText("Start Server");
	    	serverIsRunning = false;
	    }
	    	
	}

	/**
	 * starts server
	 * @return true if start successful
	 * 
	 */
	public boolean startServer() {
		try {
			server = new SocketServer(this);
		} catch (Exception e) {
			display("Can not start server: "+ e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param text
	 */
	public void display(String text) {
		logtime = new Date();
		
		text = logtime.toString()+": -> "+text +"\n"+ displayTextArea.getText();
		displayTextArea.setText(text);
	}

	/**
	 * Adds a window listener to shutdown the server 
	 * while exiting using window close button
	 */
	private void addWindowListener() {
		addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				if ( serverIsRunning )
					server.shutdown();
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
	}

    /**
     * Generally MySQL server does not permit access to 
     * other database users rather than the local users.
     * This method grants access using MySQL GRANT command
     * 
     */
    private void grantMySQLAccess (){
    	try{
	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	    }
	    catch(Exception ex){
	        System.out.println("Look & Feel Exception");
	    } 
    	fileChooser.setSelectedFile(new File(mySQLServerPath));
		int fileChosen = fileChooser.showDialog(getParent(), "Select");
		File file = fileChooser.getSelectedFile();

		if(fileChosen  != JFileChooser.APPROVE_OPTION)
			return;
			
		mySQLServerPath = file.getPath();

    	JPanel serverSettingPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        
        JTextField fileAddressTxtFld = new JTextField( mySQLServerPath, 25);
        JTextField dbNameTxtFld = new JTextField("birdchat");
        JTextField adminUserTxtFld = new JTextField("root");
        JTextField userToBeGrantedTxtFld = new JTextField("root");
        JPasswordField adminPassTxtFld = new JPasswordField("pass");

        // mysql.exe location
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(1,1,1,5);
        serverSettingPanel.add(new JLabel("mysql.exe location:", SwingConstants.LEFT), c);
        c.gridx++;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.LINE_END;
        serverSettingPanel.add( fileAddressTxtFld, c);
        
     // Database Name
        c.gridx--;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        serverSettingPanel.add(new JLabel("Database Name:", SwingConstants.LEFT), c);
        c.gridx++;
        c.fill = GridBagConstraints.BOTH;
        serverSettingPanel.add(dbNameTxtFld, c);
        
     // DB admin username
        c.gridx--;
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        serverSettingPanel.add(new JLabel("DB admin username:", SwingConstants.LEFT), c);
        c.gridx++;
        c.fill = GridBagConstraints.BOTH;
        serverSettingPanel.add(adminUserTxtFld, c);

    // DB admin password
		c.gridx--;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		serverSettingPanel.add(new JLabel("DB admin password:", SwingConstants.LEFT), c);
		c.gridx++;
		c.fill = GridBagConstraints.BOTH;
		serverSettingPanel.add(adminPassTxtFld, c);

   //  username to be granted
		c.gridx--;
		c.gridy++;
		c.fill = GridBagConstraints.NONE;
		serverSettingPanel.add(new JLabel("User to be granted:", SwingConstants.LEFT), c);
		c.gridx++;
		c.fill = GridBagConstraints.BOTH;
		serverSettingPanel.add(userToBeGrantedTxtFld, c);
    
        // add focus selection listener
        FocusAdapter focusListener = new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (e.getComponent() instanceof JTextField) {
                    JTextField entry = (JTextField) e.getComponent();
                    entry.selectAll();
                }
            }
        };

        fileAddressTxtFld.addFocusListener(focusListener);
        dbNameTxtFld.addFocusListener(focusListener);
        adminUserTxtFld.addFocusListener(focusListener);
        userToBeGrantedTxtFld.addFocusListener(focusListener);
        adminPassTxtFld.addFocusListener(focusListener);
        
        int returnVal = JOptionPane.showConfirmDialog(this, serverSettingPanel, 
                "Specify MySQL database server info", JOptionPane.OK_CANCEL_OPTION);
        try {
        	UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }
        catch(Exception ex){
        	System.out.println("Look & Feel Exception");
        }

        if (returnVal != JOptionPane.OK_OPTION) 
            return;

        mySQLServerPath 	   = fileAddressTxtFld.getText();
        String dbName 		   = dbNameTxtFld.getText().trim();
        String dbAdminUsername = adminUserTxtFld.getText().trim();
        String dbAdminPassword = String.valueOf( adminPassTxtFld.getPassword() );
        String userToBeGranted = userToBeGrantedTxtFld.getText().trim();
        String hostToBeGranted = null;
        
        try {
			hostToBeGranted = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        String command = "\""+mySQLServerPath+"\" -u"
				+dbAdminUsername+" -p"+dbAdminPassword
				+" --execute=\"GRANT ALL ON "+dbName
				+".* TO '"+userToBeGranted+"'@'"+hostToBeGranted+"';\"";
		try {
			CommandExecutor cmdPrompt = new CommandExecutor("cmd");
			cmdPrompt.append(command);
			if (cmdPrompt.runCommands() == false)
				display("Can not grant access. check datas and try again.");
			else
				display("Granting Successful.");
		} catch (IOException | InterruptedException e) {
			display("Internal problem with granting access.Try again.");
			e.printStackTrace();
		}
        
    }

	/**
	 * Creates a button 
	 */
	private void createStartStopServerButton() {
		startStopServerButton = new JButton("Start Server");
		startStopServerButton.setForeground(white);
		startStopServerButton.setBounds(uiWidth-260, 20, 250, 30);
		startStopServerButton.setBorder(null);
		startStopServerButton.setBackground(deepBlue);//r/12, 125, 175
		startStopServerButton.setFocusable(false);
		startStopServerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		startStopServerButton.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
					mouseExitedFromSignInButton();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
	
					mouseExitedFromSignInButton();
			}
	
	
			/**
			 * 
			 */
			private void mouseExitedFromSignInButton() {
				startStopServerButton.setBackground(deepBlue);
				startStopServerButton.setForeground(white);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
					startStopServerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					startStopServerButton.setBackground(white);
					startStopServerButton.setForeground(deepBlue);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
		//r/ 	mouse click on signInButton will be handled by ActionListener->actionPerformed
					mouseExitedFromSignInButton();
			}
		});
		startStopServerButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				startStopServerButtonActionPerformed(evt);
			}
		});
	}

	
	/**
	 * Sets the attributes of file chooser for mysql.exe file
	 */
	private void prepareFileChooser() {
		  try{
	  	        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	  	    }
	  	    catch(Exception ex){
	  	        System.out.println("Look & Feel Exception");
	  	    } 
		fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Select mysql.exe of your MySQL database server's bin folder");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"exe files", "exe");
		fileChooser.setFileFilter(filter);
		fileChooser.setCurrentDirectory(new File(""));try{
		        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
		    }
		    catch(Exception ex){
		        System.out.println("Look & Feel Exception");
		    }
	}

	/**
	 * sets the attributes of log display
	 */
	private void prepareDisplayArea() {
		displayTextArea = new JTextArea();
		displayTextArea.setEditable(false);
		displayTextArea.setLineWrap(true);
		displayTextArea.setBackground(lightBlue);
		displayTextArea.setMargin(new Insets(10, 10, 10, 10));
		displayTextArea.setFont(consolas12 ); // NOI18N
		
		displayScrollPane = new JScrollPane();
		displayScrollPane.setBounds(10, 60, uiWidth - 21, uiHeight - 70);
		displayScrollPane.setBorder(null);
		displayScrollPane.setViewportView(displayTextArea);
	}

	/**
	 * creates the label granting access link and add listener
	 */
	private void createGrantAccessLabel() {
		grantAccessLabel = new JLabel("<html><u>Get permission to access MySQL Database</u></html>");
		grantAccessLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
		grantAccessLabel.setForeground(white);
		grantAccessLabel.setBounds( uiWidth - 258, 0, 250, 15);
		grantAccessLabel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				grantMySQLAccess();
			}
	
		});
	}


	/**
	 *Components declaration 
	 */
	private JPanel panel;
    private JButton startStopServerButton;
    private JScrollPane displayScrollPane;
    private JTextArea displayTextArea;
    private JLabel grantAccessLabel;
	private JFileChooser fileChooser;
	private JLabel backgroundImageLabel;
}
