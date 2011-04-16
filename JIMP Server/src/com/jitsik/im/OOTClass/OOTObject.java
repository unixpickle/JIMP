package com.jitsik.im.OOTClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class OOTObject {

	// max datasize is about a MB.
	public static final long DATASIZE_MAX = 1048576;
	protected String className = null;
	protected long dataLength = 0;
	protected byte[] classData;
	
	public static String paddNumberToLength (long number, int length) {
		String numberString = "" + number;
		while (numberString.length() < length) {
			numberString = "0" + numberString;
		}
		
		return numberString;
	}

	public OOTObject (String newClassName, byte[] data) throws OOTObjectLengthException {
		if (newClassName.length() != 4) {
			OOTObjectLengthException exception = new OOTObjectLengthException(4, className.length());
			throw exception;
		}
		className = newClassName;
		classData = data;
		if (data != null) {
			dataLength = data.length;
		} else {
			data = new byte[0];
			dataLength = 0;
		}
	}

	// extracts the length and class name from the header,
	// then reads the class data from @reader.
	public OOTObject (byte[] header, InputStream reader) throws OOTObjectLengthException, IOException {
		if (header.length != 12) {
			OOTObjectLengthException exception = new OOTObjectLengthException(12, header.length);
			throw exception;
		}
		String classLengthString = new String (header, 0, 8);

		try {
			dataLength = Long.parseLong(classLengthString);
		} catch (NumberFormatException e) {
			OOTObjectLengthException exception = new OOTObjectLengthException(0, -1);
			throw exception;
		}

		if (dataLength < 0 || dataLength > DATASIZE_MAX) {
			OOTObjectLengthException exception = new OOTObjectLengthException(0, dataLength);
			throw exception;
		}

		className = new String(header, 8, 4);
		classData = new byte[(int)dataLength];
		long hasRead = 0;
		while (hasRead < dataLength) {
			hasRead += reader.read(classData, (int)hasRead, classData.length - (int)hasRead);
		}
	}

	public OOTObject (ByteBuffer buffer) throws OOTObjectLengthException {
		byte[] header = new byte[12];
		int givenHeader = buffer.remaining();
		try {
			buffer.get(header, 0, 12);
		} catch (BufferUnderflowException e) {
			OOTObjectLengthException exception = new OOTObjectLengthException(12, givenHeader);
			throw exception;
		}
		
		String classLengthString = new String (header, 0, 8);
		try {
			dataLength = Long.parseLong(classLengthString);
		} catch (NumberFormatException e) {
			OOTObjectLengthException exception = new OOTObjectLengthException(0, -1);
			throw exception;
		}
		
		className = new String(header, 8, 4);		
		classData = new byte[(int)dataLength];
		int given = buffer.remaining();
		try {
			buffer.get(classData, 0, classData.length);
		} catch (BufferUnderflowException e) {
			OOTObjectLengthException exception = new OOTObjectLengthException(dataLength + 12, given + 12);
			throw exception;
		}
	}

	public OOTObject (OOTObject object) {
		className = new String(object.getClassName());
		classData = new byte[(int)(object.getContentLength())];
		dataLength = object.getContentLength();
		
		// copy the buffer into our new data.
		ByteBuffer buffer = ByteBuffer.wrap(object.getClassData());
		buffer.get(classData);
	}

	public OOTObject () {

	}

	public String getClassName () {
		return className;
	}

	public long getContentLength () {
		return dataLength;
	}

	public byte[] getClassData () {
		return classData;
	}

	public String toString () {
		return "OOTObject(class:\"" + className + "\" length:" + dataLength + ")";
	}

	public byte[] encode () {
		int encodeLength = 12;
		if (classData != null) encodeLength += classData.length;
		byte[] encoded = new byte[encodeLength];
		String dataLengthString = OOTObject.paddNumberToLength(dataLength, 8);
		byte[] dataLengthStringBytes = null;
		try {
			dataLengthStringBytes = dataLengthString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		for (int i = 0; i < 8; i++) {
			encoded[i] = dataLengthStringBytes[i];
		}
		for (int i = 0; i < 4; i++) {
			encoded[i + 8] = (byte)className.charAt(i);
		}
		if (classData != null) {
			for (int i = 0; i < classData.length; i++) {
				encoded[i + 12] = classData[i];
			}
		}
		return encoded;
	}
	
}
