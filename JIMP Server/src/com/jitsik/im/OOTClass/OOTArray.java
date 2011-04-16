package com.jitsik.im.OOTClass;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class OOTArray extends OOTObject {

	private ArrayList<OOTObject> objects = new ArrayList<OOTObject>();
	
	private static byte[] encodeArrayData (ArrayList<OOTObject> objectsArray) {
		int estimatedLength = 0;
		for (int i = 0; i < objectsArray.size(); i++) {
			OOTObject object = objectsArray.get(i);
			estimatedLength += object.getContentLength() + 12;
		}
		ByteBuffer buffer = ByteBuffer.allocate(estimatedLength + 3);
		
		// populate the length string with 000 (3 length) string.
		String lengthString = OOTObject.paddNumberToLength(objectsArray.size(), 3);
		try {
			buffer.put(lengthString.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		for (int i = 0; i < objectsArray.size(); i++) {
			OOTObject object = objectsArray.get(i);
			buffer.put(object.encode());
		}
		buffer.position(0);
		byte[] bufferBytes = new byte[buffer.remaining()];
		buffer.get(bufferBytes);
		return bufferBytes;
	}
	
	public OOTArray (OOTObject object) throws OOTObjectLengthException {
		super(object);
		initializeFromContents();
	}
	
	public OOTArray (ByteBuffer buffer) throws OOTObjectLengthException {
		super(buffer);
		initializeFromContents();
	}
	
	public ArrayList<OOTObject> getObjects() {
		return objects;
	}
	
	private void initializeFromContents () throws OOTObjectLengthException {
		if (this.getContentLength() < 3) {
			OOTObjectLengthException exception = new OOTObjectLengthException(3, this.getContentLength());
			throw exception;
		}
		String lengthString = new String(this.getClassData(), 0, 3);
		int objectCount = 0;
		try {
			objectCount = Integer.parseInt(lengthString);
		} catch (NumberFormatException e) {
		}
		ByteBuffer buffer = ByteBuffer.wrap(this.getClassData(), 3, (int) (this.getContentLength() - 3));
		for (int i = 0; i < objectCount; i++) {
			OOTObject anObject = new OOTObject(buffer);
			objects.add(anObject);
		}
	}
	
	public OOTArray (ArrayList<OOTObject> objectsArray) throws OOTObjectLengthException {
		super("list", encodeArrayData(objectsArray));
		objects = objectsArray;
	}
	
}
