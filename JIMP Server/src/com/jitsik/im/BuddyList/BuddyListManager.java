package com.jitsik.im.BuddyList;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


public class BuddyListManager {

	private static Connection databaseConnection;

	public static void initializeDatabase () throws Exception {
		Class.forName("org.sqlite.JDBC");
		databaseConnection = DriverManager.getConnection("jdbc:sqlite:blist.db");
		Statement stat = databaseConnection.createStatement();
		stat.executeUpdate("create table if not exists buddies " +
		"(owner, screenname, groupid INTEGER NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT " +
		", next INTEGER, last INTEGER);");
		stat.executeUpdate("create table if not exists groups " +
		"(owner, groupname, id INTEGER PRIMARY KEY AUTOINCREMENT, next INTEGER, last INTEGER);");
	}

	// BEGIN: Buddy Nodes

	// insert a buddy that has a next/prev, owner, screenname, group.  returns the buddy ID.
	public static long insertBuddyListNode (BuddyListNode node) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("insert into buddies (owner, screenname, groupid, next, last) values (?, ?, ?, ?, ?);");
			prep.setString(1, node.getOwner().toLowerCase());
			prep.setString(2, node.getScreenname().toLowerCase());
			prep.setLong(3, node.getGroupID());
			prep.setLong(4, node.getNextNodeID());
			prep.setLong(5, node.getPreviousNodeID());
			prep.executeUpdate();
			prep = databaseConnection.prepareStatement("select * from buddies where (owner=? and screenname=? and groupid=?);");
			prep.setString(1, node.getOwner().toLowerCase());
			prep.setString(2, node.getScreenname().toLowerCase());
			prep.setLong(3, node.getGroupID());
			ResultSet effectedRows = prep.executeQuery();
			while (effectedRows.next()) {
				return effectedRows.getLong("id");
			}
			throw new SQLException("Adding buddy failed, no generated key obtained.");
		}
	}
	
	public static void deleteBuddyListNode (BuddyListNode node) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("delete from buddies where (id=?);");
			prep.setLong(1, node.getNodeID());
			prep.executeUpdate();
		}
	}

	public static BuddyListNode firstBuddyNodeForUser (String user) throws SQLException {
		synchronized (databaseConnection) {

			PreparedStatement prep = databaseConnection.prepareStatement("select * from buddies where (owner=?);");
			prep.setString(1, user);
			ResultSet rs = prep.executeQuery();
			if (rs == null) {
				return null;
			}
			while (rs.next()) {
				long next = rs.getLong("next");
				long last = rs.getLong("last");
				if (last == 0) {
					// this is the first.
					String screenname = rs.getString("screenname");
					long groupID = rs.getLong("groupid");
					long nodeID = rs.getLong("id");
					BuddyListNode node = new BuddyListNode(groupID, nodeID, next, last, screenname, user);
					return node;
				}
			}
		}
		return null;
	}

	public static BuddyListNode findBuddyNodeWithID (long nodeID) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("select * from buddies where (id=?);");
			prep.setLong(1, nodeID);
			ResultSet rs = prep.executeQuery();
			if (rs == null) {
				return null;
			}
			while (rs.next()) {
				long currentID = rs.getLong("id");
				if (currentID == nodeID) {
					// this is the first.
					String screenname = rs.getString("screenname");
					String owner = rs.getString("owner");
					long groupID = rs.getLong("groupid");
					long nextID = rs.getLong("next");
					long lastID = rs.getLong("last");
					BuddyListNode node = new BuddyListNode(groupID, nodeID, nextID, lastID, screenname, owner);
					return node;
				}
			}
		}
		return null;
	}

	// updates the next and last index, as well as the group ID.
	public static void updateBuddyListNode (BuddyListNode blistNode) throws SQLException {
		synchronized (databaseConnection) {
			try {
				PreparedStatement prep = databaseConnection.prepareStatement("update buddies set next=?, last=?, groupid=? where (id=?)");
				prep.setLong(1, blistNode.getNextNodeID());
				prep.setLong(2, blistNode.getPreviousNodeID());
				prep.setLong(3, blistNode.getGroupID());
				prep.setLong(4, blistNode.getNodeID());
				prep.executeUpdate();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static ArrayList<String> allBuddyScreenNamesForUser (String user) {
		synchronized (databaseConnection) {
			try {
				PreparedStatement prep = databaseConnection.prepareStatement("select * from buddies where (owner=?)");
				prep.setString(1, user);
				ResultSet rs = prep.executeQuery();
				if (rs == null) {
					return null;
				}
				ArrayList<String> screenNames = new ArrayList<String>();
				while (rs.next()) {
					String screenName = rs.getString("screenname");
					screenNames.add(screenName);
				}
				return screenNames;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}
	}


	// BEGIN: Group Nodes

	public static long insertGroupListNode (BuddyListGroupNode node) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("insert into groups (owner, groupname, next, last) values (?, ?, ?, ?);");
			prep.setString(1, node.getOwner().toLowerCase());
			prep.setString(2, node.getGroupName());
			prep.setLong(3, node.getNextNodeID());
			prep.setLong(4, node.getPreviousNodeID());
			prep.executeUpdate();
			prep = databaseConnection.prepareStatement("select * from groups where (owner=? and groupname=? and next=? and last=?);");
			prep.setString(1, node.getOwner().toLowerCase());
			prep.setString(2, node.getGroupName());
			prep.setLong(3, node.getNextNodeID());
			prep.setLong(4, node.getPreviousNodeID());
			ResultSet generatedKeys = prep.executeQuery();
			if (generatedKeys.next()) {
				long nodeID = generatedKeys.getLong("id");
				return nodeID;
			} else {
				throw new SQLException("Adding buddy failed, no generated key obtained.");
			}
		}
	}
	
	public static void deleteGroupListNode (BuddyListGroupNode node) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("delete from groups where (id=?);");
			prep.setLong(1, node.getNodeID());
			prep.executeUpdate();
		}
	}

	public static void updateGroupListNode (BuddyListGroupNode node) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("update groups set next=?, last=?, groupname=? where (id=?)");
			prep.setLong(1, node.getNextNodeID());
			prep.setLong(2, node.getPreviousNodeID());
			prep.setString(3, node.getGroupName());
			prep.setLong(4, node.getNodeID());
			prep.executeUpdate();
		}
	}
	
	public static BuddyListGroupNode firstGroupNodeForUser (String user) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("select * from groups where (owner=?);");
			prep.setString(1, user);
			ResultSet rs = prep.executeQuery();
			if (rs == null) {
				return null;
			}
			while (rs.next()) {
				long next = rs.getLong("next");
				long last = rs.getLong("last");
				if (last == 0) {
					// this is the first.
					String groupName = rs.getString("groupname");
					long nodeID = rs.getLong("id");
					BuddyListGroupNode node = new BuddyListGroupNode(nodeID, next, last, groupName, user);
					return node;
				}
			}
		}
		return null;
	}

	public static BuddyListGroupNode findGroupNodeWithID (long nodeID) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("select * from groups where (id=?);");
			prep.setLong(1, nodeID);
			ResultSet rs = prep.executeQuery();
			if (rs == null) {
				return null;
			}
			while (rs.next()) {
				long currentID = rs.getLong("id");
				if (currentID == nodeID) {
					// this is the first.
					String groupname = rs.getString("groupname");
					String owner = rs.getString("owner");
					long nextID = rs.getLong("next");
					long lastID = rs.getLong("last");
					BuddyListGroupNode node = new BuddyListGroupNode(currentID, nextID, lastID, groupname, owner);
					return node;
				}
			}
			return null;
		}
	}
	
	public static long findGroupIDForName (String groupOwner, String groupName) throws SQLException {
		synchronized (databaseConnection) {
			PreparedStatement prep = databaseConnection.prepareStatement("select * from groups where (groupname=? and owner=?);");
			prep.setString(1, groupName);
			prep.setString(2, groupOwner);
			ResultSet rs = prep.executeQuery();
			if (rs == null) {
				return 0;
			}
			while (rs.next()) {
				return rs.getLong("id");
			}
			return 0;
		}
	}
	
}
