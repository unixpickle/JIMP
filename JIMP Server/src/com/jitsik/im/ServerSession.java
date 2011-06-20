package com.jitsik.im;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.jitsik.im.BuddyList.BuddyListManager;
import com.jitsik.im.Database.AccountDatabaseManager;
import com.jitsik.im.Database.AccountNotFoundException;
import com.jitsik.im.Database.GenericDatabaseException;
import com.jitsik.im.OOTClass.OOTAccount;
import com.jitsik.im.OOTClass.OOTError;
import com.jitsik.im.OOTClass.OOTGetStatus;
import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTObjectLengthException;
import com.jitsik.im.OOTClass.OOTStatus;
import com.jitsik.im.StatusMessages.StatusMessageHandler;

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

	/**
	 * Sets the username. This method can be called from
	 * any thread, but shouldn't be called externally.
	 * @param newUsername The username of which to set for this session.
	 * This will be converted to lower case.
	 */
	private void setUsername (String newUsername) {
		synchronized (usernameLock) {
			if (newUsername != null)
				username = new String(newUsername).toLowerCase();
			else username = null;
		}
	}

	/**
	 * Gets a copy of the username.  This method can be called
	 * from any thread at any time. 
	 * @return A copy of the username.  Null if the session is not logged in.
	 */
	public String getUsername () {
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
				if (getUsername() != null) {
					handleSessionSignoff();
				}
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
		} 
		
		Log.log(Log.LEVEL_DEBUG, "Got object of class: " + object.getClassName());
		
		// code under this will only work
		// if they are signed on.
		if (getUsername() == null) return;
		
		if (object.getClassName().equals("pswd")) {
			handlePasswordChange(object);
		} else if (object.getClassName().equals("snof")) {
			handleSessionSignoff();
		} else if (object.getClassName().equals("gbst")) {
			operations.sendAccountBuddyList();
		} else if (object.getClassName().equals("gsts")) {
			try {
				OOTGetStatus getStatus = new OOTGetStatus(object);
				Log.log(Log.LEVEL_DEBUG, "Getting status: " + getStatus.getScreenName());
				OOTStatus status = StatusMessageHandler.statusForUsername(getStatus.getScreenName().toLowerCase());
				if (status != null) {
					sendStatusToBuddy(getUsername(), status);
				}
			} catch (OOTObjectLengthException e1) {
				Log.log(Log.LEVEL_ERROR, "Client sent invalid gsts object.");
			}
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
		} else if (object.getClassName().equals("delg")) {
			if (!operations.handleDeleteGroup(object)) {
				try {
					this.sendObject(new OOTError(7, "The group could not be deleted from your buddy list."));
				} catch (NotOpenException e1) {
				} catch (OOTObjectLengthException e1) {
				}
			}
		} else if (object.getClassName().equals("stts")) {
			try {
				OOTStatus status = new OOTStatus(object);
				StatusMessageHandler.setStatusForUsername(status, this.getUsername());
				broadcastStatus();
			} catch (OOTObjectLengthException e1) {
				Log.log(Log.LEVEL_ERROR, "Client sent invalid stts object");
			}
		} else if (object.getClassName().equals("mssg")) {
			if (!operations.handleMessageObject(object)) {
				Log.log(Log.LEVEL_ERROR, "Client sent invalid mssg object.");
			}
		}
	}

	/**
	 * Signs on an account, signing off the previously signed on account.
	 * 
	 * @param account The account of which we will use to sign on.
	 * @return Gives true on successful login, false if the username/password
	 * was incorrect.
	 */
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

	/**
	 * Parses a signup object, and adds the new account to the database.
	 * @param object The object that was received from the client.
	 * @throws Exception Thrown when there is a database error,
	 * or the object was not successfully parsed.
	 */
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

	/**
	 * Changes our password by parsing a password change object
	 * 
	 * @param object The object that is of the "pswd" class
	 */
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

	/**
	 * Sets our status message appropriately, nulls our username,
	 * broadcasts our departure.
	 */
	public void handleSessionSignoff () {
		Log.log(Log.LEVEL_EVENTS, "Session signoff: " + getUsername());
		String myUsername = getUsername();
		boolean sessionExists = false;
		synchronized (sessions) {
			for (ServerSession session : sessions) {
				String otherUsername = session.getUsername();
				if (username != null) {
					if (otherUsername.equalsIgnoreCase(myUsername) && session != this) {
						sessionExists = true;
					}
				}
			}
		}
		if (!sessionExists) {
			Log.log(Log.LEVEL_DEBUG, myUsername + " is no longer online, setting offline.");
			StatusMessageHandler.goOfflineForUsername(myUsername);
			broadcastStatus();
		}
		setUsername(null);
	}
	
	/**
	 * Sends our status to all of our followers, and ourselves.
	*/
	public void broadcastStatus () {
		Log.log(Log.LEVEL_DEBUG, "-broadcastStatus: start");
		String myUsername = this.getUsername();
		if (myUsername == null) return;
		OOTStatus status = StatusMessageHandler.statusForUsername(myUsername);
		ArrayList<String> followers = BuddyListManager.allFollowersForBuddy(myUsername);
		Log.log(Log.LEVEL_DEBUG, "-broadcastStatus: start");
		Log.log(Log.LEVEL_DEBUG, "-broadcastStatus: status = " + status);
		for (String follower : followers) {
			Log.log(Log.LEVEL_DEBUG, "-broadcastStatus: follower " + follower);
			sendStatusToBuddy(follower, status);
		}
		sendStatusToBuddy(myUsername.toLowerCase(), status);
	}
	
	/**
	 * Sends a status object to all sessions with
	 * a specified screenname.
	 * @param buddyName The screenname to send the status to.
	 * @param status The status of which to send.
	 */
	public void sendStatusToBuddy (String buddyName, OOTStatus status) {
		sendObjectToAccount(status, buddyName);
	}
	
	/**
	 * Sends an object to all sessions
	 * with a specified screenname/account name.
	 * @param object The object to send
	 * @param account The username of the account to send
	 * the object to.
	 */
	public void sendObjectToAccount (OOTObject object, String account) {
		synchronized (sessions) {
			for (ServerSession session : sessions) {
				String username = session.getUsername();
				if (username != null) {
					if (username.equalsIgnoreCase(account)) {
						try {
							session.sendObject(object);
						} catch (NotOpenException e) {
						}
					}
				}
			}
		}
	}

}
