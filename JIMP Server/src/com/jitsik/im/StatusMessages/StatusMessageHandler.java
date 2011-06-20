package com.jitsik.im.StatusMessages;

import java.util.ArrayList;

import com.jitsik.im.OOTClass.OOTObjectLengthException;
import com.jitsik.im.OOTClass.OOTStatus;

public class StatusMessageHandler {

	private static ArrayList<OOTStatus> statuses = new ArrayList<OOTStatus>();
	
	/**
	 * Gets the status of an account.  Returns the default
	 * offline status if no status is found.
	 * @param username The username (case insensitive) of which to
	 * fetch the status.
	 * @return The status message of the user.  This will never
	 * return null.
	 */
	public static synchronized OOTStatus statusForUsername (String username) {
		if (username == null) return null;
		for (OOTStatus status : statuses) {
			if (status.getOwner().equalsIgnoreCase(username)) {
				try {
					return new OOTStatus(status);
				} catch (OOTObjectLengthException e) {
				}
			}
		}
		try {
			return new OOTStatus(OOTStatus.STATUS_OFFLINE, 0, "", username.toLowerCase());
		} catch (OOTObjectLengthException e) {
			return null;
		}
	}
	
	public static synchronized void setStatusForUsername (OOTStatus newStatus, String username) {
		if (username == null) return;
		if (newStatus == null) return;
		boolean replaced = false;
		for (int i = 0; i < statuses.size(); i++) {
			OOTStatus status = statuses.get(i);
			if (status.getOwner().equalsIgnoreCase(username)) {
				try {
					OOTStatus copy = new OOTStatus(newStatus.getStatusType(), newStatus.getIdleTime(), 
							newStatus.getStatusMessage(), username.toLowerCase());
					statuses.set(i, copy);
					replaced = true;
				} catch (OOTObjectLengthException e) {
				}
			}
		}
		if (!replaced) {
			OOTStatus copy = null;
			try {
				copy = new OOTStatus(newStatus.getStatusType(), newStatus.getIdleTime(), 
						newStatus.getStatusMessage(), username.toLowerCase());
			} catch (OOTObjectLengthException e) {
			}
			if (copy != null) statuses.add(copy);
		}
	}
	
	public static void goOfflineForUsername (String username) {
		if (username == null) return;
		try {
			OOTStatus offline = new OOTStatus(OOTStatus.STATUS_OFFLINE, 0, "", username.toLowerCase());
			setStatusForUsername(offline, username);
		} catch (OOTObjectLengthException e) {
		}
		
	}
	
}
