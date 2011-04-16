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
	
	// inserts a BuddyListNode after another node.
	// does change database, and the other nodes.
	// if @node is null, this will be the first and last node.
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
	
	// inserts a BuddyLIstNode before another node.
	// does change database, and other nodes.
	// if @node is null, this will be the first and last node.
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
