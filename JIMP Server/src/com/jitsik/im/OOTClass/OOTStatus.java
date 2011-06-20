package com.jitsik.im.OOTClass;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class OOTStatus extends OOTObject {

	private byte statusType;
	private long idleTime;
	private OOTText statusMessage;
	private OOTText owner;
	
	public static final byte STATUS_AVAILABLE = (byte)'o';
	public static final byte STATUS_AWAY = (byte)'a';
	public static final byte STATUS_IDLE = (byte)'i';
	public static final byte STATUS_OFFLINE = (byte)'n';
	
	
	private static byte[] bytesForInfo (byte statusType, long idleTime, String statusMessage, String owner) {
		OOTText statusTxtObject = null;
		OOTText ownerTxtObject = null;
		try {
			statusTxtObject = new OOTText(statusMessage);
			ownerTxtObject = new OOTText(owner);
		} catch (OOTObjectLengthException e) {
			return null;
		}
		int length = (int)(9 + 24 + statusTxtObject.getContentLength() + ownerTxtObject.getContentLength());
		ByteBuffer bb = ByteBuffer.allocate(length);
		bb.put(statusType);
		try {
			bb.put(OOTObject.padNumberToLength(idleTime, 8).getBytes("Cp1252"));
		} catch (Exception e) {
			return null;
		}
		bb.put(statusTxtObject.encode());
		bb.put(ownerTxtObject.encode());
		return bb.array();
	}
	
	public OOTStatus (byte statusType, long idleTime, String statusMessage, String owner) throws OOTObjectLengthException {
		super("stts", bytesForInfo(statusType, idleTime, statusMessage, owner));
		this.statusType = statusType;
		this.idleTime = idleTime;
		this.statusMessage = new OOTText(statusMessage);
		this.owner = new OOTText(owner);
	}
	
	public OOTStatus (OOTObject object) throws OOTObjectLengthException {
		super(object);
		initializeFromContents();
	}
	
	public OOTStatus (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContents();
	}
	
	private void initializeFromContents() throws OOTObjectLengthException {
		try {
			ByteBuffer buffer = ByteBuffer.wrap(this.getClassData());
			byte[] idleTimeBytes = new byte[8];
			String idleTimeString = null;
			
			statusType = buffer.get();
			buffer.get(idleTimeBytes);
			statusMessage = new OOTText(buffer);
			owner = new OOTText(buffer);
			try {
				idleTimeString = new String(idleTimeBytes);
				idleTime = Long.parseLong(idleTimeString);
			} catch (NumberFormatException e) {
				throw new OOTObjectLengthException(8, 0);
			}
		} catch (BufferUnderflowException ex) {
			throw new OOTObjectLengthException(0, 0);
		}
	}
	
	public String getStatusMessage () {
		return statusMessage.getTextString();
	}
	
	public String getOwner () {
		return owner.getTextString();
	}
	
	public long getIdleTime () {
		return idleTime;
	}
	
	public byte getStatusType () {
		return statusType;
	}
	
}
