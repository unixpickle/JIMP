package com.jitsik.im;

import java.sql.SQLException;

import com.jitsik.im.BuddyList.BuddyListGroupNode;
import com.jitsik.im.BuddyList.BuddyListManager;
import com.jitsik.im.BuddyList.BuddyListNode;
import com.jitsik.im.OOTClass.BuddyOperations.OOTDeleteBuddy;
import com.jitsik.im.OOTClass.BuddyOperations.OOTDeleteGroup;
import com.jitsik.im.OOTClass.BuddyOperations.OOTInsertBuddy;
import com.jitsik.im.OOTClass.BuddyOperations.OOTInsertGroup;

public class BuddyListOperationHandler {

	public static boolean handleAddBuddy (String accountUsername, OOTInsertBuddy buddyInsert) {
		// create a buddy node.
		try {
			if (buddyInsert.getBuddyIndex() > 999) return false; // cannot have that!
			if (buddyInsert.getBuddyIndex() < 0) return false;
			Log.log(Log.LEVEL_DEBUG, "-handleAddGroup: start, index = " + buddyInsert.getBuddyIndex());
			long groupID = BuddyListManager.findGroupIDForName(accountUsername, buddyInsert.getBuddy().getGroup());
			Log.log(Log.LEVEL_DEBUG, "-handleAddGroup: groupID = " + groupID);
			if (groupID == 0) return false;
			BuddyListNode node = new BuddyListNode(accountUsername, buddyInsert.getBuddy().getScreenName().toLowerCase(), groupID);
			BuddyListNode effectedNode = null;
			BuddyListNode currentNode = null;
			int desiredAfterIndex = buddyInsert.getBuddyIndex() - 1;

			currentNode = BuddyListManager.firstBuddyNodeForUser(accountUsername);
			int currentIndex = 0;
			while (currentNode != null) {
				if (currentIndex == desiredAfterIndex) {
					effectedNode = currentNode;
				} else if (desiredAfterIndex == -1 && currentIndex == 0)
					effectedNode = currentNode;
				
				if (currentNode.getScreenname().equalsIgnoreCase(buddyInsert.getBuddy().getScreenName())) {
					Log.log(Log.LEVEL_DEBUG, "-handleAddGroup: insert failed, buddy exists");
					return false;
				}
				currentIndex += 1;
				currentNode = currentNode.getNextNode();
			}
			if (effectedNode == null && !(currentIndex == 0 && buddyInsert.getBuddyIndex() == 0)) {
				Log.log(Log.LEVEL_DEBUG, "-handleAddGroup: effected node is null.");
				// if currentIndex was zero, then it would mean that this is
				// our first buddy, and therefore there is no (null) other buddy.
				return false;
			}
			if (desiredAfterIndex == -1) {
				node.insertBeforeNode(effectedNode);
			} else {
				node.insertAfterNode(effectedNode);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
			Log.log(Log.LEVEL_ERROR, "-handleAddGroup: sqlException");
			return false;
		}
		return true;
	}
	
	public static boolean handleAddGroup (String accountUsername, OOTInsertGroup groupInsert) {
		try {
			Log.log(Log.LEVEL_DEBUG, "-handleAddGroup: start");
			if (groupInsert.getGroupIndex() > 999) return false;
			if (groupInsert.getGroupIndex() < 0) return false;
			int desiredAfterIndex = groupInsert.getGroupIndex() - 1;
			BuddyListGroupNode currentNode = null;
			BuddyListGroupNode effectedNode = null;
			int currentIndex = 0;
			currentNode = BuddyListManager.firstGroupNodeForUser(accountUsername);
			Log.log(Log.LEVEL_DEBUG, "-handleAddGroup: desiredAfterIndex = " + desiredAfterIndex);
			while (currentNode != null) {
				if (currentIndex == desiredAfterIndex) 
					effectedNode = currentNode;
				else if (desiredAfterIndex == -1 && currentIndex == 0)
					effectedNode = currentNode;
				if (currentNode.getGroupName().equals(groupInsert.getGroupName())) return false;
				currentIndex += 1;
				currentNode = currentNode.getNextNode();
			}
			Log.log(Log.LEVEL_DEBUG, "-handleAddGroup: effectedNode = " + effectedNode);
			if (effectedNode == null && groupInsert.getGroupIndex() != 0) return false;
			BuddyListGroupNode node = new BuddyListGroupNode(0, 0, 0, groupInsert.getGroupName(), accountUsername);
			if (currentIndex >= 0) {
				node.insertAfterNode(effectedNode);
			} else {
				node.insertBeforeNode(effectedNode);
			}
			Log.log(Log.LEVEL_DEBUG, "-handleAddGroup: done");
		} catch (SQLException ex) {
			ex.printStackTrace();
			Log.log(Log.LEVEL_ERROR, "-handleAddGroup: exception");
			return false;
		}
		return true;
	}
	
	public static boolean handleDeleteBuddy (String accountUsername, OOTDeleteBuddy delete) {
		try {
			Log.log(Log.LEVEL_DEBUG, "-handleDeleteBuddy: start");
			BuddyListNode node = null;
			node = BuddyListManager.firstBuddyNodeForUser(accountUsername.toLowerCase());
			while (node != null) {
				if (node.getScreenname().equalsIgnoreCase(delete.getScreenName())) {
					node.removeFromList();
					Log.log(Log.LEVEL_DEBUG, "-handleDeleteBuddy: deleted buddy");
					return true;
				}
				node = node.getNextNode();
			}
			Log.log(Log.LEVEL_DEBUG, "-handleDeleteBuddy: done");
			return false;
		} catch (SQLException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	public static boolean handleDeleteGroup (String accountUsername, OOTDeleteGroup delete) {
		try {
			Log.log(Log.LEVEL_DEBUG, "-handleDeleteGroup: start");
			BuddyListNode node = null;
			BuddyListGroupNode groupNode = null;
			node = BuddyListManager.firstBuddyNodeForUser(accountUsername.toLowerCase());
			while (node != null) {
				long groupID = node.getGroupID();
				if (groupID != 0) {
					BuddyListGroupNode group = BuddyListManager.findGroupNodeWithID(groupID);
					if (group.getGroupName().equals(delete.getGroupName())) {
						groupNode = group;
						// we can delete it, but still get the next node
						// since that will remain valid.
						Log.log(Log.LEVEL_DEBUG, "-handleDeleteGroup: deleting buddy \"" + node.getScreenname() + "\"");
						node.removeFromList();
						BuddyListManager.deleteBuddyListNode(node);
					}
				}
				node = node.getNextNode();
			}
			if (groupNode == null) {
				Log.log(Log.LEVEL_DEBUG, "-handleDeleteGroup: no buddies with group specified (doing search).");
				groupNode = BuddyListManager.firstGroupNodeForUser(accountUsername.toLowerCase());
				while (groupNode != null) {
					if (groupNode.getGroupName().equals(delete.getGroupName())) {
						break;
					}
					groupNode = groupNode.getNextNode();
				}
			}
			if (groupNode == null) {
				Log.log(Log.LEVEL_DEBUG, "-handleDeleteGroup: group \"" + delete.getGroupName() + "\" not found");
				return false;
			}
			groupNode.removeFromList();
			Log.log(Log.LEVEL_DEBUG, "-handleDeleteGroup: done");
		} catch (SQLException ex) {
			return false;
		}
		return true;
	}
	
}
