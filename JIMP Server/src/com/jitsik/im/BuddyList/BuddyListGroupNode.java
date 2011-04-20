package com.jitsik.im.BuddyList;

import java.sql.SQLException;

import com.jitsik.im.Log;

public class BuddyListGroupNode {

	private long nodeID;
	private long nextNodeID;
	private long lastNodeID;
	private String groupName;
	private String owner;
	
	public BuddyListGroupNode (long nodeID, long nextNodeID, long lastNodeID, String groupName, String owner) {
		this.nodeID = nodeID;
		this.nextNodeID = nextNodeID;
		this.lastNodeID = lastNodeID;
		this.groupName = groupName;
		this.owner = owner;
	}
	
	public String getGroupName () {
		return groupName;
	}
	
	public String getOwner () {
		return owner;
	}
	
	public long getNextNodeID () {
		return nextNodeID;
	}
	
	public long getPreviousNodeID () {
		return lastNodeID;
	}
	
	public long getNodeID () {
		return nodeID;
	}
	
	/**
	 * Fetch the next node through a node ID.
	 * @return	The node that immediately follows this one.  This will return null
	 * if there is a database error, or no other node is found.
	 */
	public BuddyListGroupNode getNextNode () {
		if (nextNodeID == 0) {
			return null;
		}
		BuddyListGroupNode node = null;
		try {
			node = BuddyListManager.findGroupNodeWithID(nextNodeID);
		} catch (SQLException e) {
			return null;
		}
		return node;
	}
	
	/**
	 * Fetch the previous node through a node ID.
	 * @return	The node that immediately precedes this one.  This will return null
	 * if there is a database error, or no other node is found.
	 */
	public BuddyListGroupNode getPreviousNode () {
		if (lastNodeID == 0) {
			return null;
		}
		BuddyListGroupNode node = null;
		try {
			node = BuddyListManager.findGroupNodeWithID(lastNodeID);
		} catch (SQLException e) {
			return null;
		}
		return node;
	}
	
	/**
	 * Inserts the node directly after another node that is owned by the same user.
	 *
	 * @param node	The node of which we will insert ourselves after.  If this is null,
	 * a new node will be created and added as the first and last node in a chain.
	 * @throws SQLException	Thrown when there is an internal error while modifying
	 * the database.
	 */
	public void insertAfterNode (BuddyListGroupNode node) throws SQLException {
		if (node == null) {
			// default, first node configuration.
			this.lastNodeID = 0;
			this.nextNodeID = 0;
			this.nodeID = BuddyListManager.insertGroupListNode(this);
			return;
		}
		
		BuddyListGroupNode next = node.getNextNode();
		this.lastNodeID = node.nodeID;
		if (next != null) {
			this.nextNodeID = next.nodeID;
		} else this.nextNodeID = 0;
		
		this.nodeID = BuddyListManager.insertGroupListNode(this);
		if (next != null) {
			next.lastNodeID = this.nodeID;
			BuddyListManager.updateGroupListNode(next);
		}
		node.nextNodeID = this.nodeID;
		BuddyListManager.updateGroupListNode(node);
	}
	
	/**
	 * Inserts a node directly before another node that is owned by the same user.
	 * 
	 * @param node	The node of which we will insert ourselves before.  If this is null,
	 * a new node will be created and added as the first and last node in a chain.
	 * @throws SQLException	Thrown when there is an internal error while modifying
	 * the database.
	 */
	public void insertBeforeNode (BuddyListGroupNode node) throws SQLException {
		if (node == null) {
			// default, first node configuration.
			this.lastNodeID = 0;
			this.nextNodeID = 0;
			this.nodeID = BuddyListManager.insertGroupListNode(this);
			return;
		}
		
		BuddyListGroupNode last = node.getPreviousNode();
		this.nextNodeID = node.nodeID;
		if (last != null) {
			this.lastNodeID = last.nodeID;
		} else this.lastNodeID = 0;
		
		this.nodeID = BuddyListManager.insertGroupListNode(this);
		node.lastNodeID = this.nodeID;
		BuddyListManager.updateGroupListNode(node);
		if (last != null) {
			last.nextNodeID = this.nodeID;
			BuddyListManager.updateGroupListNode(last);
		}
	}
	
	/**
	 * Removes this node from the node chain.  This will work for any node,
	 * as long as it has already be added or obtained from a chain.
	 * The previous and next nodes will be modified to fit this change.
	 * 
	 * @throws SQLException	Thrown when there is an internal error while modifying
	 * the database
	 */
	public void removeFromList () throws SQLException {
		Log.log(Log.LEVEL_DEBUG, "-removeFromList: group: nodeID=" + nodeID + " nextID=" + nextNodeID + " lastID=" + lastNodeID);
		if (this.nodeID == 0) {
			throw new SQLException("Not in the database.");
		}
		BuddyListGroupNode next = null;
		BuddyListGroupNode last = null;
		if (this.nextNodeID != 0) {
			next = this.getNextNode();
		}
		if (this.lastNodeID != 0) {
			last = this.getPreviousNode();
		}
		if (next != null) {
			if (last != null) {
				next.lastNodeID = last.nodeID;
				last.nextNodeID = next.nodeID;
				BuddyListManager.updateGroupListNode(last);
			} else {
				next.lastNodeID = 0;
			}
			BuddyListManager.updateGroupListNode(next);
		} else {
			if (last != null) {
				last.nextNodeID = 0;
				BuddyListManager.updateGroupListNode(last);
			}
		}
		BuddyListManager.deleteGroupListNode(this);
	}
	
}
