package com.jitsik.im.OOTClass;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;


public class OOTGetStatus extends OOTText {

	private String screenName = null;
	
	public OOTGetStatus(String screenName) throws OOTObjectLengthException {
		super(screenName);
		this.screenName = screenName;
		this.className = "gsts";
	}
	
	public OOTGetStatus(byte[] data) throws OOTObjectLengthException {
		super(data);
		initializeFromContent();
	}
	
	public OOTGetStatus(ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContent();
	}
	
	public OOTGetStatus(OOTObject anObject) throws OOTObjectLengthException {
		super(anObject);
		initializeFromContent();
	}
	
	private void initializeFromContent () {
		try {
			screenName = new String (getClassData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.className = "gsts";
	}

	public String getScreenName () {
		return screenName;
	}
	
}
