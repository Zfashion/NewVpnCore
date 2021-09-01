/*
 * Copyright (C) 2012-2019 Tobias Brunner
 * Copyright (C) 2012 Giuliano Grassi
 * Copyright (C) 2012 Ralf Sager
 * HSR Hochschule fuer Technik Rapperswil
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.  See <http://www.fsf.org/copyleft/gpl.txt>.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 */

package com.core.ikev2.data;

import com.core.unitevpn.utils.VPNLog;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class VpnProfileDataSource
{
	private static final String TAG = VpnProfileDataSource.class.getSimpleName();
	public static final String KEY_ID = "_id";
//	public static final String KEY_UUID = "_uuid";
	public static final String KEY_NAME = "name";
	public static final String KEY_GATEWAY = "gateway";
	public static final String KEY_VPN_TYPE = "vpn_type";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_PASSWORD = "password";
	/*public static final String KEY_CERTIFICATE = "certificate";
	public static final String KEY_USER_CERTIFICATE = "user_certificate";
	public static final String KEY_MTU = "mtu";
	public static final String KEY_PORT = "port";
	public static final String KEY_SPLIT_TUNNELING = "split_tunneling";
	public static final String KEY_LOCAL_ID = "local_id";
	public static final String KEY_REMOTE_ID = "remote_id";
	public static final String KEY_EXCLUDED_SUBNETS = "excluded_subnets";
	public static final String KEY_INCLUDED_SUBNETS = "included_subnets";
	public static final String KEY_SELECTED_APPS = "selected_apps";
	public static final String KEY_SELECTED_APPS_LIST = "selected_apps_list";
	public static final String KEY_NAT_KEEPALIVE = "nat_keepalive";
	public static final String KEY_FLAGS = "flags";
	public static final String KEY_IKE_PROPOSAL = "ike_proposal";
	public static final String KEY_ESP_PROPOSAL = "esp_proposal";
	public static final String KEY_DNS_SERVERS = "dns_servers";*/

	/*private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDatabase;
	private final Context mContext;

	private static final String DATABASE_NAME = "strongswan.db";
	private static final String TABLE_VPNPROFILE = "vpnprofile";

	private static final int DATABASE_VERSION = 17;*/

	private volatile static VpnProfileDataSource profileDataSource;

	public static VpnProfileDataSource getInstance() {
		if (null == profileDataSource) {
			synchronized (VpnProfileDataSource.class) {
				if (null == profileDataSource) {
					profileDataSource = new VpnProfileDataSource();
				}
			}
		}
		return profileDataSource;
	}

	private final List<VpnProfile> profileList = new ArrayList<>();
	private Iterator<VpnProfile> profileIterator;

	public void addNewProfiles(List<VpnProfile> profiles) {
		profileList.addAll(profiles);
		profileIterator = profiles.iterator();
	}

	/**
	 * Delete the given VPN profile from the database.
	 * @return true if deleted, false otherwise
	 */
	public void clearProfiles() {
		profileList.clear();
		profileIterator = null;
	}

	public VpnProfile getNextVpnProfile() {
		if (profileIterator != null && profileIterator.hasNext()) {
			return profileIterator.next();
		} else {
			return null;
		}
		/*VpnProfile poll = profileList.poll();
		VPNLog.d("next profile= " + poll.getId());
		return poll;*/
	}

	public boolean hasNextProfile() {
		return profileIterator != null && profileIterator.hasNext();
	}

	/**
	 * Get a single VPN profile from the database.
	 * @param id the ID of the VPN profile
	 * @return the profile or null, if not found
	 */
	public VpnProfile getVpnProfile(long id)
	{
		if (id < 0L) {
			return getNextVpnProfile();
		} else {
			if (!profileList.isEmpty()) {
				for (VpnProfile vpnProfile : profileList) {
					if (vpnProfile.getId() == id) {
						return vpnProfile;
					}
				}
			}
		}
		return null;
	}

}
