package com.jitsik.im.Database;

import java.sql.*;


public class AccountDatabaseManager {

	private static Connection databaseConnection;

	public static void initializeDatabase () throws Exception {
		Class.forName("org.sqlite.JDBC");
		databaseConnection = DriverManager.getConnection("jdbc:sqlite:jimp.db");
		Statement stat = databaseConnection.createStatement();
		stat.executeUpdate("create table if not exists accounts " +
		"(username, status_message, password, signupdate);");
	}

	public static boolean getUsernameExists (String _username) throws GenericDatabaseException {
		synchronized (databaseConnection) {
			String username = _username.toLowerCase();
			try {
				PreparedStatement prep = databaseConnection.prepareStatement(
				"select * from accounts where (username=?);");
				prep.setString(1, username);
				ResultSet rs = prep.executeQuery();
				while (rs.next()) {
					return true;
				}
				return false;
			} catch (SQLException e) {
				e.printStackTrace();
				throw new GenericDatabaseException ();
			}
		}
	}

	public static String getPasswordHashForUsername (String _username) throws AccountNotFoundException, GenericDatabaseException {
		synchronized (databaseConnection) {
			String username = _username.toLowerCase();
			try {
				PreparedStatement prep = databaseConnection.prepareStatement(
				"select * from accounts where (username=?);");
				prep.setString(1, username);
				ResultSet rs = prep.executeQuery();
				if (rs == null) {
					throw new GenericDatabaseException();
				}
				while (rs.next()) {
					String password = rs.getString("password");
					if (password != null) return password;
				}
				throw new AccountNotFoundException();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new GenericDatabaseException ();
			}
		}
	}

	public static void addNewUser (String username, String password, String signupdate) throws GenericDatabaseException, UsernameAlreadyExistsException {
		synchronized (databaseConnection) {
			if (getUsernameExists(username)) {
				throw new UsernameAlreadyExistsException ();
			}
			try {
				PreparedStatement prep = databaseConnection.prepareStatement("insert into accounts (username, password, status_message, signupdate) values (?, ?, ?, ?);");
				prep.setString(1, username.toLowerCase());
				prep.setString(2, password);
				prep.setString(3, "Available");
				prep.setString(4, signupdate);
				prep.execute();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new GenericDatabaseException ();
			}
		}
	}

	public static void changeAccountPassword (String username, String newPassword) throws GenericDatabaseException {
		synchronized (databaseConnection) {
			try {
				PreparedStatement prep = databaseConnection.prepareStatement("update accounts set password=? where (username=?);");
				prep.setString(1, newPassword);
				prep.setString(2, username.toLowerCase());
				prep.execute();
			} catch (SQLException e) {
				throw new GenericDatabaseException ();
			}
		}
	}

}
