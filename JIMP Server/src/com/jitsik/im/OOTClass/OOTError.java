package com.jitsik.im.OOTClass;

import java.nio.ByteBuffer;

// class type "errr".

public class OOTError extends OOTObject {

	private int errorCode;
	private String errorMessage;
	
	public static byte[] ClassDataForError (int newErrorCode, String newErrorMessage) {
		byte[] charBuffer = new byte[2 + newErrorMessage.length()];
		String errorCodeString = "" + newErrorCode;
		while (errorCodeString.length() < 2) {
			errorCodeString = "0" + errorCodeString;
		}
		charBuffer[0] = (byte)errorCodeString.charAt(0);
		charBuffer[1] = (byte)errorCodeString.charAt(1);
		for (int i = 2; i < newErrorMessage.length() + 2; i++) {
			char letter = newErrorMessage.charAt(i - 2);
			charBuffer[i] = (byte)letter;
		}
		return charBuffer;
	}
	
	public OOTError (OOTObject object) throws OOTObjectLengthException, NumberFormatException {
		super(object);
		initializeFromContents();
	}
	
	public OOTError (ByteBuffer buffer) throws OOTObjectLengthException, NumberFormatException {
		super(buffer);
		initializeFromContents();
	}
	
	public OOTError (int newErrorCode, String newErrorMessage) throws OOTObjectLengthException {
		super("errr", OOTError.ClassDataForError(newErrorCode, newErrorMessage));
		errorCode = newErrorCode;
		errorMessage = newErrorMessage;
	}
	
	private void initializeFromContents () throws OOTObjectLengthException {
		if (this.getContentLength() < 2) {
			OOTObjectLengthException exception = new OOTObjectLengthException(2, this.getContentLength());
			throw exception;
		}
		try {
			errorCode = Integer.parseInt(new String(this.getClassData(), 0, 2));
		} catch (NumberFormatException e) {
			errorCode = 0;
		}
		errorMessage = new String(this.getClassData(), 2, this.getClassData().length - 2);
	}
	
	public int getErrorCode () {
		return errorCode;
	}
	
	public String getErrorMessage () {
		return errorMessage;
	}
	
	public String toString () {
		return super.toString() + " OOTError(code: " + errorCode + " message:\"" + errorMessage + "\")";
	}
	
}
