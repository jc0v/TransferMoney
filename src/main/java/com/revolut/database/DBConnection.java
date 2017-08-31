package com.revolut.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

	public static Connection getConnection() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		// In memory DB connection kept open until server stops by using DB_CLOSE_DELAY. Script creates schema on connection.
		String url = "jdbc:h2:mem:TransferMoney;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'classpath:createSchema.sql'";
		//String url = "jdbc:h2:~/TransferMoney;INIT=RUNSCRIPT FROM 'classpath:createSchema.sql'";
		Connection connection = DriverManager.getConnection(url, "sa", "");
		return connection;
	}
}
