package com.jitsik.im.OOTClass;

import java.nio.ByteBuffer;

// class type "acco".
// can also be used by "snup".

public class OOTAccount extends OOTObject {

	private OOTText username;
	private OOTText password;
	
	public OOTAccount (OOTObject object) throws OOTObjectLengthException {
		super(object);
		initializeFromContents();
	}
	
	public OOTAccount (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContents();
	}
	
	public OOTAccount (OOTAccount account) throws OOTObjectLengthException {
		super(ByteBuffer.wrap(account.encode()));
		initializeFromContents();
	}
	
	private void initializeFromContents () throws OOTObjectLengthException {
		ByteBuffer innerBuffer = ByteBuffer.wrap(this.getClassData());
		innerBuffer.position(0);
		username = new OOTText(innerBuffer);
		password = new OOTText(innerBuffer);
	}
	
	public byte[] encode () {
		return super.encode();
	}
	
	public String toString () {
		return super.toString() + " OOTAccount(username:\"" + getUsername() + 
		                          "\" password:\"" + getPassword() + "\")";
	}
	
	public String getUsername () {
		return username.getTextString();
	}
	
	public String getPassword () {
		return password.getTextString();
	}
	
}
