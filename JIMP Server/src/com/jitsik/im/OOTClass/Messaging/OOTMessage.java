package com.jitsik.im.OOTClass.Messaging;

import java.nio.ByteBuffer;

import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTObjectLengthException;
import com.jitsik.im.OOTClass.OOTText;

public class OOTMessage extends OOTObject {

	private OOTText username;
	private OOTText message;
	
	private static byte[] messageContents (String username, String message) {
		try {
			OOTText usernameObject = new OOTText(username);
			OOTText messageObject = new OOTText(message);
			ByteBuffer encoded = ByteBuffer.allocate((int)(usernameObject.getContentLength() + messageObject.getContentLength() + 24));
			encoded.put(usernameObject.encode());
			encoded.put(messageObject.encode());
			return encoded.array();
		} catch (OOTObjectLengthException e) {
			return null;
		}
	}
	
	public OOTMessage (String username, String message) throws OOTObjectLengthException {
		super("mssg", messageContents(username, message));
		this.username = new OOTText(username);
		this.message = new OOTText(message);
	}
	
	public OOTMessage (OOTObject object) throws OOTObjectLengthException {
		super(object);
		initializeFromContents();
	}
	
	public OOTMessage (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContents();
	}
	
	private void initializeFromContents () throws OOTObjectLengthException {
		ByteBuffer buffer = ByteBuffer.wrap(this.getClassData());
		username = new OOTText(buffer);
		message = new OOTText(buffer);
	}
	
	public String getUsername () {
		return username.getTextString();
	}
	
	public String getMessage () {
		return message.getTextString();
	}
	
}
