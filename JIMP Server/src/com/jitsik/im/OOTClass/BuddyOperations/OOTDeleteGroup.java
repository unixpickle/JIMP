package com.jitsik.im.OOTClass.BuddyOperations;

import java.nio.ByteBuffer;

import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTObjectLengthException;
import com.jitsik.im.OOTClass.OOTText;

public class OOTDeleteGroup extends OOTText {

	private String groupName = null;
	
	public OOTDeleteGroup(String groupName) throws OOTObjectLengthException {
		super(groupName);
		this.groupName = groupName;
		this.className = "delb";
	}
	
	public OOTDeleteGroup(byte[] data) throws OOTObjectLengthException {
		super(data);
		groupName = new String (getClassData());
		this.className = "delb";
	}
	
	public OOTDeleteGroup(ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		groupName = new String (getClassData());
		this.className = "delb";
	}
	
	public OOTDeleteGroup(OOTObject anObject) throws OOTObjectLengthException {
		super(anObject);
		groupName = new String (getClassData());
		this.className = "delb";
	}

	public String getScreenName () {
		return groupName;
	}
	
}
