/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.authorization;

import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;

/**
 *
 * @author linux
 */
public class UserAndRoleData
{
	static private HashMap<String, TreeSet<String>> mUsersToRoles =  new HashMap<>();
	static private HashMap<String, TreeSet<String>> mRolesToUsers =  new HashMap<>();
	
	private UserAndRoleData()
	{
		
	}
	
	synchronized static public void updateUserAndRoleData(Properties theUsers)
	{
		mUsersToRoles.clear();
		mRolesToUsers.clear();
		for( String userId : theUsers.stringPropertyNames())
		{
			TreeSet<String> roles = mUsersToRoles.get(userId);
			if (null==roles)
			{
				roles = new TreeSet<>();
			}
			for (String role : theUsers.getProperty(userId).split("\\|", -1))
			{
				roles.add(role);
				TreeSet<String> users = mRolesToUsers.get(role);
				if (null==users)
				{
					users = new TreeSet<>();
				}
				users.add(userId);
				mRolesToUsers.put(role, users);
			}
			mUsersToRoles.put(userId, roles);
		}
	}
	
	synchronized static public TreeSet<String> getUserList()
	{
		TreeSet<String> set = new TreeSet<>();
		set.addAll(mUsersToRoles.keySet());
		return set;
	}
	
	synchronized static public TreeSet<String> getRoleList()
	{
		TreeSet<String> set = new TreeSet<>();
		set.addAll(mRolesToUsers.keySet());
		return set;
	}
	
	synchronized static public TreeSet<String> getUsersInRoleList(String theRole)
	{
		TreeSet<String> set = new TreeSet<>();
		set.addAll(mRolesToUsers.get(theRole));
		return set;
	}
}
