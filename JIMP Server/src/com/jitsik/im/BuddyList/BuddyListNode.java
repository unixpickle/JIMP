package com.jitsik.im.BuddyList;

import java.sql.SQLException;

public class BuddyListNode {
	
	private long group;
	private long nodeID;
	private long nextNodeID;
	private long lastNodeID;
	private String screenname;
	private String owner;
	
	public BuddyListNode (long groupID, long nodeID, long nextNodeID, long lastNodeID, String screenname, String owner) {
		this.group = groupID;
		this.nodeID = nodeID;
		this.lastNodeID = lastNodeID;
		this.nextNodeID = nextNodeID;
		this.screenname = screenname;
		this.owner = owner;
	}
	
	public BuddyListNode (String owner, String buddyName, long group) {
		this.group = group;
		this.screenname = buddyName;
		this.owner = owner;
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
	
	public long getGroupID () {
		return group;
	}
	
	public String getScreenname() {
		return screenname;
	}
	
	public String getOwner () {
		return owner;
	}
	
	/**
	 * Fetch the next node through a node ID.
	 * 
	 * @return	The node that immediately follows this one.  This will return null
	 * if there is a database error, or no other node is found.
	 */
	public BuddyListNode getNextNode () {
		if (nextNodeID == 0) {
			return null;
		}
		BuddyListNode node = null;
		try {
			node = BuddyListManager.findBuddyNodeWithID(nextNodeID);
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
	public BuddyListNode getPreviousNode () {
		if (lastNodeID == 0) {
			return null;
		}
		BuddyListNode node = null;
		try {
			node = BuddyListManager.findBuddyNodeWithID(lastNodeID);
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
	public void insertAfterNode (BuddyListNode node) throws SQLException {
		if (node == null) {
			// default, first node configuration.
			this.lastNodeID = 0;
			this.nextNodeID = 0;
			this.nodeID = BuddyListManager.insertBuddyListNode(this);
			return;
		}
		
		BuddyListNode next = node.getNextNode();
		this.lastNodeID = node.nodeID;
		if (next != null) {
			this.nextNodeID = next.nodeID;
		} else this.nextNodeID = 0;
		
		this.nodeID = BuddyListManager.insertBuddyListNode(this);
		if (next != null) next.lastNodeID = this.nodeID;
		node.nextNodeID = this.nodeID;
		BuddyListManager.updateBuddyListNode(node);
		if (next != null) BuddyListManager.updateBuddyListNode(next);
	}
	
	/**
	 * Inserts a node directly before another node that is owned by the same user.
	 * 
	 * @param node	The node of which we will insert ourselves before.  If this is null,
	 * a new node will be created and added as the first and last node in a chain.
	 * @throws SQLException	Thrown when there is an internal error while modifying
	 * the database.
	 */
	public void insertBeforeNode (BuddyListNode node) throws SQLException {
		if (node == null) {
			// default, first node configuration.
			this.lastNodeID = 0;
			this.nextNodeID = 0;
			this.nodeID = BuddyListManager.insertBuddyListNode(this);
			return;
		}
		
		BuddyListNode last = node.getPreviousNode();
		this.nextNodeID = node.nodeID;
		if (last != null) {
			this.lastNodeID = last.nodeID;
		} else this.lastNodeID = 0;
		
		this.nodeID = BuddyListManager.insertBuddyListNode(this);
		node.lastNodeID = this.nodeID;
		BuddyListManager.updateBuddyListNode(node);
		if (last != null) {
			last.nextNodeID = this.nodeID;
			BuddyListManager.updateBuddyListNode(last);
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
		if (this.nodeID == 0) {
			throw new SQLException("Not in the database.");
		}
		BuddyListNode next = null;
		BuddyListNode last = null;
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
				BuddyListManager.updateBuddyListNode(last);
			} else {
				next.lastNodeID = 0;
			}
			BuddyListManager.updateBuddyListNode(next);
		} else {
			if (last != null) {
				last.nextNodeID = 0;
				BuddyListManager.updateBuddyListNode(last);
			}
		}
		BuddyListManager.deleteBuddyListNode(this);
	}
	
}
