package com.jitsik.im.BuddyList;

import java.sql.SQLException;

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
	
	// inserts a BuddyListNode after another node.
	// does change database, and the other nodes.
	// if @node is null, this will be the first and last node.
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
	
	// inserts a BuddyListGroupNode before another node.
	// does change database, and other nodes.
	// if @node is null, this will be the first and last node.
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
	
}
