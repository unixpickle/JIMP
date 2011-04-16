package com.jitsik.im;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.jitsik.im.Database.AccountDatabaseManager;
import com.jitsik.im.Database.AccountNotFoundException;
import com.jitsik.im.Database.GenericDatabaseException;
import com.jitsik.im.OOTClass.OOTAccount;
import com.jitsik.im.OOTClass.OOTError;
import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTObjectLengthException;

public class ServerSession implements Runnable {

	private ObjectsOverTCPConnection connection = null;
	private ServerSessionOperationHandler operations = null;
	private Object usernameLock = new Object();
	private String username = null;

	public static ArrayList<ServerSession> sessions = new ArrayList<ServerSession>();

	public ServerSession (ObjectsOverTCPConnection openConnection) {
		connection = openConnection;
		sessions.add(this);
		operations = new ServerSessionOperationHandler(this);
	}

	synchronized public void sendObject (OOTObject object) throws NotOpenException {
		synchronized (connection) {
			connection.writeObject(object);
		}
	}

	private void setUsername (String newUsername) {
		synchronized (usernameLock) {
			if (newUsername != null)
				username = new String(newUsername).toLowerCase();
			else username = null;
		}
	}

	synchronized public String getUsername () {
		String usernameCopy;
		synchronized (usernameLock) {
			if (username == null) {
				return null;
			}
			usernameCopy = new String(username);
		}
		return usernameCopy;
	}

	public void run () {
		// once we hit an error, we will remove ourselves from the sessions array.
		try {
			boolean isOpen = true;
			while (isOpen) {
				OOTObject object = null;
				try {
					Thread.sleep(300);
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
				synchronized (connection) {
					if (!connection.getIsOpen()) {
						isOpen = false;
						break;
					}

					try {
						object = connection.readObject(false);
						if (object != null) {
							// System.out.println("Got object: " + object);
						}
					} catch (NotOpenException e) {
						isOpen = false;
					}
				}
				if (object != null && isOpen) {
					handleObject(object);
				}
			}
			if (getUsername() != null) {
				Log.log(Log.LEVEL_EVENTS, "Connection terminated: " + getUsername());
				handleSessionSignoff();
			} else {
				Log.log(Log.LEVEL_EVENTS, "Unauthorized connection ended.");
			}
			synchronized (sessions) {
				sessions.remove(this);
			}
		} catch (Exception e) {
			connection.close();
		}
	}

	private void handleObject (OOTObject object) {
		if (object.getClassName().equals("acco")) {
			try {
				OOTAccount account = new OOTAccount(object);
				Log.log(Log.LEVEL_DEBUG, "Account Object: " + account);
				if (performAccountSignon(account)) {
					Log.log(Log.LEVEL_DEBUG, "Login correct: " + account.getUsername());
					try {
						this.sendObject(new OOTObject("onln", new byte[0]));
					} catch (NotOpenException e) {
						e.printStackTrace();
					}
				} else {
					try {
						this.sendObject(new OOTError(1, "The username or password that you entered is incorrect."));
					} catch (NotOpenException e) {
						e.printStackTrace();
					}
					Log.log(Log.LEVEL_DEBUG, "Login incorrect: " + account.getUsername());
				}
			} catch (OOTObjectLengthException e) {
				System.out.println("Invalid account object was sent, dying.");
			}
		} else if (object.getClassName().equals("snup")) {
			try {
				handleSignup(object);
				try {
					this.sendObject(new OOTObject("aded", new byte[0]));
				} catch (NotOpenException e1) {
				} catch (OOTObjectLengthException e1) {
				}
			} catch (Exception e) {
				e.printStackTrace();
				try {
					this.sendObject(new OOTError(2, "An internal server error occured while adding your account."));
				} catch (NotOpenException e1) {
				} catch (OOTObjectLengthException e1) {
				}
			}
		} else if (object.getClassName().equals("pswd")) {
			handlePasswordChange(object);
		} else if (object.getClassName().equals("snof")) {
			handleSessionSignoff();
		} else if (object.getClassName().equals("gbst")) {
			operations.sendAccountBuddyList();
		} else if (object.getClassName().equals("isrt")) {
			if (!operations.handleInsertBuddy(object)) {
				try {
					this.sendObject(new OOTError(4, "The buddy has not been added to your buddy list."));
				} catch (NotOpenException e1) {
				} catch (OOTObjectLengthException e1) {
				}
			}
		} else if (object.getClassName().equals("irtg")) {
			if (!operations.handleInsertGroup(object)) {
				try {
					this.sendObject(new OOTError(5, "The group has not been added to your buddy list."));
				} catch (NotOpenException e1) {
				} catch (OOTObjectLengthException e1) {
				}
			}
		} else if (object.getClassName().equals("delb")) {
			if (!operations.handleDeleteBuddy(object)) {
				try {
					this.sendObject(new OOTError(6, "The buddy could not be deleted from your buddy list."));
				} catch (NotOpenException e1) {
				} catch (OOTObjectLengthException e1) {
				}
			}
		}
	}

	private boolean performAccountSignon (OOTAccount account) {
		if (this.getUsername() != null) return false;
		boolean loginSuccess = false;
		try {
			String password = AccountDatabaseManager.getPasswordHashForUsername(account.getUsername());
			if (password != null) {
				if (password.toLowerCase().equals(account.getPassword().toLowerCase())) {
					setUsername(account.getUsername());
					loginSuccess = true;
					// TODO: go through all of the other sessions.
					// if there isn't a session with this screen name,
					// send all sessions an online notification
					// if this buddy is on their list.
				}
			}
		} catch (AccountNotFoundException e) {
		} catch (GenericDatabaseException e) {
		}
		return loginSuccess;
	}

	private void handleSignup (OOTObject object) throws Exception {
		OOTAccount account = new OOTAccount (object);
		if (AccountDatabaseManager.getUsernameExists(account.getUsername())) {
			this.sendObject(new OOTError (2, "The specified username was already taken."));
		} else {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();
			String signupDate = dateFormat.format(date);
			AccountDatabaseManager.addNewUser(account.getUsername(), account.getPassword(), signupDate);
		}
	}

	public void handlePasswordChange (OOTObject object) {
		try {
			String password = new String (object.getClassData());
			if (password.length() != 32) {
				this.sendObject(new OOTError(03, "Password change failed."));
			} else {
				AccountDatabaseManager.changeAccountPassword(this.getUsername(), password);
				synchronized (sessions) {
					for (ServerSession session : sessions) {
						if (session != this && session.getUsername().equals(this.getUsername())) {
							session.sendObject(new OOTObject("pswc", new byte[0]));
							session.connection.close();
						}
					}
				}
			}
		} catch (Exception e) {
			try {
				this.sendObject(new OOTError (03, "Password change failed."));
			} catch (Exception e1) {

			}
		}
	}

	public void handleSessionSignoff () {
		// TODO: go through all other sessions, find if this session
		// is the last one of its screen name.  If it is, we will send
		// an offline notification to all other clients.
		Log.log(Log.LEVEL_EVENTS, "Session signoff: " + getUsername());
		setUsername(null);
	}

}
