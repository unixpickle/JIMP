package com.jitsik.im;

import com.jitsik.im.OOTClass.OOTBuddyList;
import com.jitsik.im.OOTClass.OOTBuddyListError;
import com.jitsik.im.OOTClass.OOTObject;
import com.jitsik.im.OOTClass.OOTObjectLengthException;
import com.jitsik.im.OOTClass.BuddyOperations.OOTDeleteBuddy;
import com.jitsik.im.OOTClass.BuddyOperations.OOTDeleteGroup;
import com.jitsik.im.OOTClass.BuddyOperations.OOTInsertBuddy;
import com.jitsik.im.OOTClass.BuddyOperations.OOTInsertGroup;

public class ServerSessionOperationHandler {

	private ServerSession session;

	public ServerSessionOperationHandler (ServerSession session) {
		this.session = session;
	}

	public ServerSession getSession () {
		return session;
	}

	/**
	 * Writes the buddy list to the session.
	 */
	public void sendAccountBuddyList () {
		try {
			OOTBuddyList blist = new OOTBuddyList(session.getUsername());
			session.sendObject(blist);
		} catch (OOTBuddyListError e) {
			e.printStackTrace();
		} catch (OOTObjectLengthException e) {
			e.printStackTrace();
		} catch (NotOpenException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a buddy insert object.  This will update the buddy list, and
	 * forward the insert object to all other sessions.
	 * @param isrt The insert object that was received over the connection.
	 * @return Gives true on complete success, false on any failure.
	 */
	public boolean handleInsertBuddy (OOTObject isrt) {
		try {
			Log.log(Log.LEVEL_DEBUG, "-handleInsertBuddy: creating addBuddy");
			OOTInsertBuddy buddyAdd = new OOTInsertBuddy(isrt);
			Log.log(Log.LEVEL_DEBUG, "-handleInsertBuddy: inserting buddy");
			if (!BuddyListOperationHandler.handleAddBuddy(session.getUsername(), buddyAdd)) {
				Log.log(Log.LEVEL_DEBUG, "-handleInsertBuddy: handleAddBuddy failed");
				return false;
			} else {
				sendToAllSessions(isrt);
			}
			return true;
		} catch (OOTObjectLengthException e) {
			e.printStackTrace();
			return false;
		}	
	}

	/**
	 * Handles a group insert object.  This will update the buddy list, and
	 * forward the insert object to all other sessions.
	 * @param irtg The insert object that was received over the connection.
	 * @return Gives true on complete success, false on any failure.
	 */
	public boolean handleInsertGroup (OOTObject irtg) {
		try {
			Log.log(Log.LEVEL_DEBUG, "-handleInsertGroup: start");
			OOTInsertGroup groupInsert = new OOTInsertGroup(irtg);
			if (!BuddyListOperationHandler.handleAddGroup(session.getUsername(), groupInsert)) {
				Log.log(Log.LEVEL_ERROR, "-handleInsertGroup: handleAddGroup failed");
				return false;
			} else {
				sendToAllSessions(irtg);
			}
			return true;
		} catch (OOTObjectLengthException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Handles a delete object.  This will forward the delete to all other connections.
	 * @param delb The delete object received over the connection.
	 * @return Returns true if the buddy was deleted, false if there was ANY error.
	 */
	public boolean handleDeleteBuddy (OOTObject delb) {
		try {
			OOTDeleteBuddy deleteObject = new OOTDeleteBuddy(delb);
			if (!BuddyListOperationHandler.handleDeleteBuddy(session.getUsername(), deleteObject)) {
				return false;
			} else {
				sendToAllSessions(delb);
			}
		} catch (OOTObjectLengthException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Handles a delete object.  This will forward the delete to all other connections.
	 * @param delg The delete object received over the connection.
	 * @return Returns true if the group was deleted, false if there was ANY error.
	 */
	public boolean handleDeleteGroup (OOTObject delg) {
		try {
			OOTDeleteGroup deleteObject = new OOTDeleteGroup(delg);
			if (!BuddyListOperationHandler.handleDeleteGroup(session.getUsername(), deleteObject)) {
				return false;
			} else {
				sendToAllSessions(delg);
			}
		} catch (OOTObjectLengthException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * Sends an object to all sessions signed on with
	 * the account that the main session is signed on to.
	 * @param object The object of which to forward.
	 */
	public void sendToAllSessions (OOTObject object) {
		synchronized (ServerSession.sessions) {
			for (ServerSession aSession : ServerSession.sessions) {
				if (aSession.getUsername() != null) {
					if (aSession.getUsername().equals(session.getUsername())) {
						try {
							aSession.sendObject(object);
						} catch (NotOpenException e) {
						}
					}
				}
			}
		}
	}
	
	

}
