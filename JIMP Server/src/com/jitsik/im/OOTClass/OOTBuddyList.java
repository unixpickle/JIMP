package com.jitsik.im.OOTClass;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;

import com.jitsik.im.BuddyList.BuddyListGroupNode;
import com.jitsik.im.BuddyList.BuddyListManager;
import com.jitsik.im.BuddyList.BuddyListNode;

public class OOTBuddyList extends OOTObject {

	private ArrayList<OOTObject> groups = new ArrayList<OOTObject>();
	private ArrayList<OOTObject> buddies = new ArrayList<OOTObject>();
	
	public OOTBuddyList (String username) throws OOTBuddyListError, OOTObjectLengthException {
		try {
			BuddyListNode buddyNode = BuddyListManager.firstBuddyNodeForUser(username);
			BuddyListGroupNode groupNode = BuddyListManager.firstGroupNodeForUser(username);
			while (buddyNode != null) {
				String groupName = BuddyListManager.findGroupNodeWithID(buddyNode.getGroupID()).getGroupName();
				OOTBuddy buddy = new OOTBuddy (groupName, buddyNode.getScreenname());
				buddies.add(buddy);
				buddyNode = buddyNode.getNextNode();
			}
			while (groupNode != null) {
				String groupName = groupNode.getGroupName();
				groups.add(new OOTText(groupName));
				groupNode = groupNode.getNextNode();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new OOTBuddyListError();
		} catch (NullPointerException e1) {
			e1.printStackTrace();
			throw new OOTBuddyListError();
		}
		// encode our class data.
		byte[] groupsEncoded = new OOTArray(groups).encode();
		byte[] buddiesEncoded = new OOTArray(buddies).encode();
		ByteBuffer buffer = ByteBuffer.allocate(groupsEncoded.length + buddiesEncoded.length);
		buffer.put(groupsEncoded);
		buffer.put(buddiesEncoded);
		buffer.position(0);
		byte[] totalBytes = new byte[buffer.remaining()];
		buffer.get(totalBytes);
		this.classData = totalBytes;
		this.className = "blst";
		this.dataLength = totalBytes.length;
	}
	
	public OOTBuddyList (OOTObject object) throws OOTObjectLengthException {
		super(object);
		initializeFromContents();
	}
	
	public OOTBuddyList (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContents();
	}
	
	private void initializeFromContents () throws OOTObjectLengthException {
		ByteBuffer buffer = ByteBuffer.wrap(this.getClassData());
		groups = new OOTArray(buffer).getObjects();
		buddies = new OOTArray(buffer).getObjects();
		for (int i = 0; i < groups.size(); i++) {
			OOTObject object = groups.get(i);
			OOTText text = new OOTText(object);
			groups.set(i, text);
		}
	}
	
	public ArrayList<String> getGroupNames () {
		ArrayList<String> strings = new ArrayList<String>();
		for (int i = 0; i < groups.size(); i++) {
			OOTObject object = groups.get(i);
			OOTText text = (OOTText)object;
			strings.add(text.getTextString());
		}
		return strings;
	}
	
	public ArrayList<String> getBuddies () {
		ArrayList<String> strings = new ArrayList<String>();
		for (int i = 0; i < buddies.size(); i++) {
			OOTObject object = buddies.get(i);
			OOTBuddy buddy = (OOTBuddy)object;
			strings.add(buddy.getScreenName());
		}
		return strings;
	}
	
}
