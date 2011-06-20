package com.jitsik.im.OOTClass;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

// class type "text".

public class OOTText extends OOTObject {
	
	private String textString = null;
	
	public static byte[] bytesFromString (String string) {
		try {
			return string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public OOTText (OOTObject object) {
		super(object);
		try {
			textString = new String(this.getClassData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			textString = "";
		}
	}
	
	public OOTText (byte[] data) throws OOTObjectLengthException {
		super(ByteBuffer.wrap(data));
		try {
			textString = new String(this.getClassData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			textString = "";
		}
	}
	
	public OOTText (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		try {
			textString = new String(this.getClassData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			textString = "";
		}
	}
	
	public OOTText (String string) throws OOTObjectLengthException {
		super("text", bytesFromString(string));
		try {
			textString = new String(this.getClassData(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			textString = "";
		}
	}
	
	public String toString () {
		return super.toString() + " OOTText(\"" + textString + "\")";
	}
	
	public String getTextString () {
		return textString;
	}
	
}