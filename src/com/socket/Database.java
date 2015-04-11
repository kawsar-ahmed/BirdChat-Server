package com.socket;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Kawsar Ahmed
 *
 */
public class Database {
    
    private String serverAddress = "localhost";
    private Connection connection;
	private String databaseName = "birdchat";
	private int dbServerPort = 3306;
	private String dbUser = "root";
	private String dbPass = "";
	

	/**
	 * Constructor Database() connect to the mysql database using the defaults
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Database() 
			throws ClassNotFoundException, SQLException {
		this( null, 0, null, null, null);
	}
	
    /**
     * Constructor - Takes database user and password to connect to the mysql
     *  database
     * @param user
     * @param pass
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Database( String user, String pass)
			throws ClassNotFoundException, SQLException {
		this( null, 0, null, user, pass);
	}
	/**
	 * Constructor - Takes db name, db user and password 
	 * to connect to the mysql database
	 * @param db
	 * @param user
	 * @param pass
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Database(String db, String user, String pass)
			throws ClassNotFoundException, SQLException {
		this( null, 0, db, user, pass);
	}
	/**
	 * Constructor - Takes db name, db user and password to connect to the mysql database
	 * @param server
	 * @param port
	 * @param user
	 * @param pass
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Database(String server, int port, String user, String pass)
			throws ClassNotFoundException, SQLException {
		this( server, port, null, user, pass);
	}
	/**
	 * @param server
	 * @param port
	 * @param db
	 * @param user
	 * @param pass
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Database(String server, int port, String db, String user, String pass) 
			throws ClassNotFoundException, SQLException{
		if (serverAddress.startsWith("127.0.0."))
			dbPass = "pass";
        if (server	!= null) 	serverAddress 	= server;
        if (db		!= null)	databaseName 	= db;
        if (user	!= null)	dbUser 			= user;
        if (pass	!= null)	dbPass 			= pass;
        if (port 	!= 	0  )	dbServerPort 	= port;
        if (serverAddress.startsWith("127.0.0."))
        	dbPass = "pass";
        connect();
    }
	
    /**
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void connect() throws SQLException, ClassNotFoundException {
    	/*r* load driver for mysql jdbc */
		Class.forName("com.mysql.jdbc.Driver");
			
		/*r* create connection to mysql */
		connection = DriverManager.getConnection( "jdbc:mysql://" + serverAddress + ":"
							+ dbServerPort + "/" 	+	 databaseName, 	dbUser	, dbPass);
				
    }
    
    /**
     * @param user
     * @return <b>true</b> if <b>user</b> exists false otherwise
     * @throws SQLException
     */
    public boolean userExists(String user) throws SQLException{
    	String sql = "Select `username` from `user` where `username` = " + user ;
    	return hasResult( sql);
    }

	/**
	 * checks login
	 * @param user
	 * @param pass
	 * @return
	 * @throws SQLException
	 */
	public boolean checkLogin(String user, String pass) throws SQLException{
		
		String sql = "SELECT `username` FROM `user` " +
				"WHERE `username` = '"+user+"' AND " +
				"`password` = MD5( '"+pass+"' ) LIMIT 0 , 30" ;
	    return hasResult(sql);
	}

	/**
	 * Adds new user's details to database
	 * @param user username of new user
	 * @param pass password
	 * @param photoPath the java path of user's profile photo
	 * @throws SQLException
	 */
	public void addUser(String user, String pass, String photoPath) throws SQLException{
	    String sql = "INSERT INTO `birdchat`.`user` " +
	    		"( `id` ,`username` ,`password` ,`photo_path`) " +
	    		"VALUES(NULL , '" + user + "', '" + pass + "'," + photoPath + ");";
	    java.sql.PreparedStatement query = connection.prepareStatement(sql);
		query.executeQuery();
	}

	/**
	 * Executes the query string <b>sql</b> and checks whether the result is empty or not
	 * @param sql the sql query string
	 * @return <b>true</b> if the query has some results, <b>false</b> otherwise
	 * @throws SQLException
	 */
	private boolean hasResult(String sql)
			throws SQLException {
		try {
			java.sql.PreparedStatement query = connection.prepareStatement(sql);
			ResultSet result = query.executeQuery();
			return result.next();
		} catch (SQLException e1) {
			java.sql.PreparedStatement query = connection.prepareStatement(sql);
			ResultSet result = query.executeQuery();
			return result.next();
		}
	}
	
	/**
	 * Disconnect from database
	 */
	public void close() {
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
