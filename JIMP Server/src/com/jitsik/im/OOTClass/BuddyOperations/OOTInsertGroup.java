package com.jitsik.im.OOTClass.BuddyOperations;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTObjectLengthException;
import com.jitsik.im.OOTClass.OOTText;

public class OOTInsertGroup extends OOTObject {

	private int groupIndex;
	private OOTText groupName;
	
	private ByteBuffer encodedData (int groupIndex, OOTText groupName) {
		try {
			ByteBuffer buffer = ByteBuffer.allocate(15 + groupName.getTextString().length());
			buffer.put(OOTObject.padNumberToLength(groupIndex, 3).getBytes("UTF-8"));
			buffer.put(groupName.encode());
			buffer.position(0);
			return buffer;
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public OOTInsertGroup (int groupIndex, String group) {
		try {
			this.groupIndex = groupIndex;
			this.groupName = new OOTText(group);
			this.classData = encodedData(groupIndex, this.groupName).array();
			this.dataLength = this.classData.length;
			this.className = "irtg";
		} catch (OOTObjectLengthException e) {
			
		}
	}
	
	public OOTInsertGroup (OOTObject object) throws OOTObjectLengthException {
		super(object);
		initializeFromContents();
	}
	
	public OOTInsertGroup (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContents();
	}
	
	private void initializeFromContents () throws OOTObjectLengthException {
		if (this.getContentLength() < 3) {
			throw new OOTObjectLengthException(3, this.getContentLength());
		}
		String indexString = new String(this.getClassData(), 0, 3);
		ByteBuffer buffer = ByteBuffer.wrap(this.getClassData(), 3, this.getClassData().length - 3);
		groupName = new OOTText(buffer);
		try {
			groupIndex = Integer.parseInt(indexString);
		} catch (NumberFormatException e) {
			groupIndex = 0;
		}
	}
	
	public int getGroupIndex () {
		return groupIndex;
	}
	
	public String getGroupName () {
		return groupName.getTextString();
	}
	
}
