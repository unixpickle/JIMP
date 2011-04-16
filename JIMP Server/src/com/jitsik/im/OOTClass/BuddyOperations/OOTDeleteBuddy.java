package com.jitsik.im.OOTClass.BuddyOperations;

import java.nio.ByteBuffer;

import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTObjectLengthException;
import com.jitsik.im.OOTClass.OOTText;

public class OOTDeleteBuddy extends OOTText {

	private String screenName = null;
	
	public OOTDeleteBuddy(String screenName) throws OOTObjectLengthException {
		super(screenName);
		this.screenName = screenName;
		this.className = "delb";
	}
	
	public OOTDeleteBuddy(byte[] data) throws OOTObjectLengthException {
		super(data);
		screenName = new String (getClassData());
		this.className = "delb";
	}
	
	public OOTDeleteBuddy(ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		screenName = new String (getClassData());
		this.className = "delb";
	}
	
	public OOTDeleteBuddy(OOTObject anObject) throws OOTObjectLengthException {
		super(anObject);
		screenName = new String (getClassData());
		this.className = "delb";
	}

	public String getScreenName () {
		return screenName;
	}
	
}
