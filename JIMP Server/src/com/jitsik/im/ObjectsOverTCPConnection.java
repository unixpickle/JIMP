package com.jitsik.im;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTObjectLengthException;

public class ObjectsOverTCPConnection {
	
	private ArrayList<OOTObject> bufferedWrites = new ArrayList<OOTObject>();
	private ArrayList<OOTObject> bufferedReads = new ArrayList<OOTObject>();
	
	private Boolean isOpen;
	private Object isOpenLock = new Object();
	
	private Socket connectionSocket = null;
	
	public ObjectsOverTCPConnection (Socket socket) {
		if (socket == null || !socket.isConnected()) {
			isOpen = false;
			return;
		}
		connectionSocket = socket;
		Runnable readRunnable = new Runnable() {
			public void run() {
				try {
					backgroundReadThread(connectionSocket.getInputStream());
				} catch (IOException e) {
					e.printStackTrace();
					try {
						connectionSocket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return;
				}
			}
		};
		Runnable writeRunnable = new Runnable() {
			public void run() {
				try {
					backgroundWriteThread(connectionSocket.getOutputStream());
				} catch (IOException e) {
					e.printStackTrace();
					try {
						connectionSocket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return;
				}
			}
		};
		isOpen = true;
		new Thread(readRunnable).start();
		new Thread(writeRunnable).start();
	}
	
	public void backgroundReadThread (InputStream input) {
		byte[] objectHeader = new byte[12];
		while (true) {
			if (!getIsOpen()) {
				break;
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException exception2) {
			}
			int currentlyHas = 0;
			while (currentlyHas < 12) {
				try {
					int added = input.read(objectHeader, currentlyHas, 12 - currentlyHas);
					currentlyHas += added;
					if (added < 0) {
						synchronized (isOpenLock) {
							isOpen = false;
							return;
						}
					}
					while (objectHeader[0] == '\n' || objectHeader[0] == '\r') {
						for (int i = 1; i < objectHeader.length; i++) {
							objectHeader[i - 1] = objectHeader[i];
						}
						currentlyHas -= 1;
					}
				} catch (IOException exception) {
					// exception.printStackTrace();
					synchronized (isOpenLock) {
						isOpen = false;
						return;
					}
				}
			}
			
			try {
				OOTObject object = new OOTObject(objectHeader, input);
				synchronized (bufferedReads) {
					bufferedReads.add(object);
				}
			} catch (OOTObjectLengthException e) {
				System.out.println("Exception on OOT level: " + currentlyHas);
				// there was an error on the OOT level, not the TCP level.
				try {
					input.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				synchronized (isOpenLock) {
					isOpen = false;
				}
				return;
			} catch (IOException e) {
				// TCP level error, time to close down!
				try {
					input.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				synchronized (isOpenLock) {
					isOpen = false;
				}
				return;
			}
		}
	}
	
	public void backgroundWriteThread (OutputStream output) {
		while (true) {
			OOTObject object = null;
			if (!getIsOpen()) {
				try {
					output.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;
			}
			synchronized (bufferedWrites) {
				if (bufferedWrites.size() > 0) {
					object = bufferedWrites.get(0);
					bufferedWrites.remove(0);
				}
			}
			if (object != null) {
				byte[] buffer = object.encode();
				try {
					output.write(buffer);
				} catch (IOException e) {
					synchronized (isOpenLock) {
						isOpen = false;
					}
					return;
				}
			}
			// sleep for x milliseconds.
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// if @block is true, then this will block until
	// a new object is waiting on the connection.
	// if @block is false, this will return null if
	// an object is not currently available.
	public OOTObject readObject (boolean block) throws NotOpenException {
		if (!getIsOpen()) {
			throw new NotOpenException();
		}
		while (true) {
			synchronized (bufferedReads) {
				if (bufferedReads.size() > 0) {
					OOTObject object = bufferedReads.get(0);
					bufferedReads.remove(0);
					return object;
				}
			}
			if (!block) break;
			
			// sleep for 100 milliseconds.
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void writeObject (OOTObject object) throws NotOpenException {
		if (!getIsOpen()) {
			throw new NotOpenException();
		}
		synchronized (bufferedWrites) {
			// create a copy of the new object
			// so that we won't get in trouble
			// for accessing it on another thread.
			bufferedWrites.add(new OOTObject(object));
		}
	}
	
	public synchronized boolean getIsOpen () {
		synchronized (isOpenLock) {
			return isOpen;
		}
	}
	
	public synchronized boolean close () {
		if (getIsOpen()) {
			try {
				connectionSocket.close();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
		return true;
	}
	
}
