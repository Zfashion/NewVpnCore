/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.core.openvpn.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Binder;

import com.core.openvpn.core.Preferences;

import java.util.HashSet;
import java.util.Set;



public class ExternalAppDatabase {

	Context mContext;
	
	public ExternalAppDatabase(Context c) {
		mContext =c;
	}

	private final static String PREFERENCES_KEY = "allowed_apps";

	boolean isAllowed(String packagename) {
		Set<String> allowedapps = getExtAppList();

		return allowedapps.contains(packagename); 

	}

	public Set<String> getExtAppList() {
		SharedPreferences prefs = Preferences.getDefaultSharedPreferences(mContext);
        return prefs.getStringSet(PREFERENCES_KEY, new HashSet<String>());
	}
	
	public void addApp(String packagename)
	{
		Set<String> allowedapps = getExtAppList();
		allowedapps.add(packagename);
		saveExtAppList(allowedapps);
	}

	private void saveExtAppList( Set<String> allowedapps) {
		SharedPreferences prefs = Preferences.getDefaultSharedPreferences(mContext);
		Editor prefedit = prefs.edit();

		// Workaround for bug
		prefedit.putStringSet(PREFERENCES_KEY, allowedapps);
		int counter = prefs.getInt("counter", 0);
		prefedit.putInt("counter", counter + 1);
		prefedit.apply();
	}
	
	public void clearAllApiApps() {
		saveExtAppList(new HashSet<String>());
	}

	public void removeApp(String packagename) {
		Set<String> allowedapps = getExtAppList();
		allowedapps.remove(packagename);
		saveExtAppList(allowedapps);		
	}


	public String checkOpenVPNPermission(PackageManager pm) throws SecurityRemoteException {

		for (String appPackage : getExtAppList()) {
			ApplicationInfo app;
			try {
				app = pm.getApplicationInfo(appPackage, 0);
				if (Binder.getCallingUid() == app.uid) {
					return appPackage;
				}
			} catch (PackageManager.NameNotFoundException e) {
				// App not found. Remove it from the list
				removeApp(appPackage);
			}

		}
		throw new SecurityException("Unauthorized OpenVPN API Caller");
	}


	public boolean checkRemoteActionPermission(Context c, String callingPackage) {
		if (isAllowed(callingPackage)) {
			return true;
		} else {
			return false;
		}
	}
}