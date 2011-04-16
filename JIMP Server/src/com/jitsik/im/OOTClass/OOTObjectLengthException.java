package com.jitsik.im.OOTClass;

public class OOTObjectLengthException extends Exception {
	
	private static final long serialVersionUID = -4094678272068028678L;
	
	private long neededLength;
	private long givenLength;
	
	public OOTObjectLengthException (long neededLength, long givenLength) {
		this.neededLength = neededLength;
		this.givenLength = givenLength;
	}
	
	public long getNeededLength () {
		return neededLength;
	}
	
	public long getGivenLength () {
		return givenLength;
	}
	
}
