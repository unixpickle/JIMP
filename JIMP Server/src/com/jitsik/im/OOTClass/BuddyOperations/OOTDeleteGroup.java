package com.jitsik.im.OOTClass.BuddyOperations;

import java.io.UnsupportedEncodingException;
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
		try {
			groupName = new String(this.getClassData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			groupName = "";
		}
		this.className = "delb";
	}
	
	public OOTDeleteGroup(ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		try {
			groupName = new String(this.getClassData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			groupName = "";
		}
		this.className = "delb";
	}
	
	public OOTDeleteGroup(OOTObject anObject) throws OOTObjectLengthException {
		super(anObject);
		try {
			groupName = new String(this.getClassData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			groupName = "";
		}
		this.className = "delb";
	}

	public String getGroupName () {
		return groupName;
	}
	
}
