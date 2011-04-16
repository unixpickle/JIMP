package com.jitsik.im.OOTClass.BuddyOperations;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTBuddy;
import com.jitsik.im.OOTClass.OOTObjectLengthException;

public class OOTInsertBuddy extends OOTObject {

	private OOTBuddy buddy;
	private int buddyIndex;
	
	private static ByteBuffer insertBuddyData (int buddyIndex, OOTBuddy buddy) {
		ByteBuffer buffer = ByteBuffer.allocate((int)(3 + 12 + buddy.getContentLength()));
		try {
			buffer.put(OOTObject.paddNumberToLength(buddyIndex, 3).getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		buffer.put(buddy.encode());
		return buffer;
	}
	
	public OOTInsertBuddy (int buddyIndex, OOTBuddy buddy) throws OOTObjectLengthException {
		super("isrt", insertBuddyData(buddyIndex, buddy).array());
		this.buddy = buddy;
		this.buddyIndex = buddyIndex;
	}
	
	public OOTInsertBuddy (OOTObject object) throws OOTObjectLengthException {
		super(object);
		initializeFromContents();
	}
	
	public OOTInsertBuddy (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContents();
	}
	
	private void initializeFromContents () throws OOTObjectLengthException {
		byte[] indexBuffer = new byte[3];
		if (this.getContentLength() < 3) {
			OOTObjectLengthException exception = new OOTObjectLengthException(3, this.getContentLength());
			throw exception;
		}
		ByteBuffer contentBuffer = ByteBuffer.wrap(this.getClassData());
		contentBuffer.get(indexBuffer);
		buddy = new OOTBuddy(contentBuffer);
		try {
			buddyIndex = Integer.parseInt(new String(indexBuffer));
		} catch (NumberFormatException e) {
			buddyIndex = 0;
		}
	}
	
	public OOTBuddy getBuddy () {
		return buddy;
	}
	
	public int getBuddyIndex () {
		return buddyIndex;
	}
	
}
