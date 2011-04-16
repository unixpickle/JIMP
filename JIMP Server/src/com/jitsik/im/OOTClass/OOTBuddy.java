package com.jitsik.im.OOTClass;

import java.nio.ByteBuffer;

public class OOTBuddy extends OOTObject {

	private OOTText group;
	private OOTText screenName;

	private static byte[] buddyData (String group, String screenname) {
		try {
			OOTText screenNameText = new OOTText(screenname);
			OOTText groupNameText = new OOTText(group);
			ByteBuffer buffer = ByteBuffer.allocate(24 + (int)screenNameText.getContentLength() + (int)groupNameText.getContentLength());
			buffer.put(groupNameText.encode());
			buffer.put(screenNameText.encode());
			buffer.position(0);
			byte[] buff = new byte[buffer.remaining()];
			buffer.get(buff);
			return buff;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
	
	public OOTBuddy (OOTObject object) throws OOTObjectLengthException {
		super(object);
		initializeFromContents();
	}

	public OOTBuddy (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContents();
	}
	
	private void initializeFromContents () throws OOTObjectLengthException {
		ByteBuffer byteBuffer = ByteBuffer.wrap(this.getClassData());
		group = new OOTText(byteBuffer);
		screenName = new OOTText(byteBuffer);
	}

	public OOTBuddy (String groupName, String screenName) throws OOTObjectLengthException {
		super("budd", buddyData(groupName, screenName));
		this.group = new OOTText(groupName);
		this.screenName = new OOTText(screenName);
	}

	public String getGroup () {
		return group.getTextString();
	}

	public String getScreenName () {
		return screenName.getTextString();
	}

}
