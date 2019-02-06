/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.authorization;

import edu.mda.bioinfo.bei.servlets.AuthUpdate;
import edu.mda.bioinfo.bei.servlets.BEISTDDatasets;
import edu.mda.bioinfo.bei.servlets.BEIproperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeSet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
public class Authorization
{
	static private boolean mIsRead = false;
	static private HashMap<String, String> mJobsToOwner =  new HashMap<>();
	static private HashMap<String, TreeSet<String>> mJobsToUsers =  new HashMap<>();
	static private HashMap<String, TreeSet<String>> mJobsToRoles =  new HashMap<>();
	
	private Authorization()
	{
		
	}
	
	synchronized static public TreeSet<String> getJobRoles(HttpServlet theServlet, String theJobId) throws IOException
	{
		TreeSet<String> result = new TreeSet<>();
		if (true==BEIproperties.isLoginAllowed(theServlet))
		{
			//theServlet.log("getJobRoles theJobId=" + theJobId);
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			TreeSet<String> tmp = mJobsToRoles.get(theJobId);
			if (null!=tmp)
			{
				result.addAll(tmp);
			}
		}
		return result;
	}
	
	synchronized static public TreeSet<String> getJobUsers(HttpServlet theServlet, String theJobId) throws IOException
	{
		TreeSet<String> result = new TreeSet<>();
		if (true==BEIproperties.isLoginAllowed(theServlet))
		{
			//theServlet.log("getJobUsers theJobId=" + theJobId);
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			TreeSet<String> tmp = mJobsToUsers.get(theJobId);
			if (null!=tmp)
			{
				result.addAll(tmp);
			}
		}
		return result;
	}
	
	static public String treeSetToString(TreeSet<String> theSet) throws IOException
	{
		String result = null;
		for(String data : theSet)
		{
			if (null==result)
			{
				result = data;
			}
			else
			{
				result = result + "|" + data;
			}
		}
		if (null==result)
		{
			result = "";
		}
		return result;
	}
	
	static public TreeSet<String> stringToTreeSet(String theString) throws IOException
	{
		TreeSet<String> result = new TreeSet<>();
		result.addAll(Arrays.asList(theString.split("\\|", -1)));
		return result;
	}
	
	synchronized static public void removeAuthorizationData(HttpServlet theServlet, String theJobId) throws IOException
	{
		if (true==BEIproperties.isLoginAllowed(theServlet))
		{
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			if (BEIproperties.isLoginAllowed(theServlet))
			{
				//theServlet.log("removeAuthorizationData version = " + edu.mda.bioinfo.bei.servlets.BEISTDDatasets.M_VERSION);
				// update maps with new information
				mJobsToUsers.remove(theJobId+ ".USERS");
				mJobsToRoles.remove(theJobId+ ".ROLES");
				mJobsToOwner.remove(theJobId+ ".OWNER");
				Properties props = new Properties();
				for (Entry<String, TreeSet<String>> myData : mJobsToUsers.entrySet())
				{
					props.setProperty(myData.getKey()+ ".USERS", treeSetToString(myData.getValue()));
				}
				for (Entry<String, TreeSet<String>> myData : mJobsToRoles.entrySet())
				{
					props.setProperty(myData.getKey()+ ".ROLES", treeSetToString(myData.getValue()));
				}
				for (Entry<String, String> myData : mJobsToOwner.entrySet())
				{
					props.setProperty(myData.getKey()+ ".OWNER", myData.getValue());
				}
				try (FileOutputStream os = new FileOutputStream(new File(BEISTDDatasets.M_PROPS, "auth.properties")))
				{
					props.storeToXML(os, "authorization properties");
				}
			}
			else
			{
				//theServlet.log("updateAuthorizationData disabled/skipped");
			}
		}
	}
	
	
	synchronized static public void updateAuthorizationData(HttpServlet theServlet, String theJobId, String theJobOwner, 
			TreeSet<String> theUsers, TreeSet<String> theRoles) throws FileNotFoundException, IOException
	{

		if (true==BEIproperties.isLoginAllowed(theServlet))
		{
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			if (BEIproperties.isLoginAllowed(theServlet))
			{
				//theServlet.log("updateAuthorizationData version = " + edu.mda.bioinfo.bei.servlets.BEISTDDatasets.M_VERSION);
				//theServlet.log("theJobId = " + theJobId);
				long start = System.currentTimeMillis();
				// update maps with new information
				mJobsToUsers.put(theJobId, theUsers);
				mJobsToRoles.put(theJobId, theRoles);
				mJobsToOwner.put(theJobId, theJobOwner);
				// add job and users to properties
				Properties props = new Properties();
				for (Entry<String, TreeSet<String>> myData : mJobsToUsers.entrySet())
				{
					props.setProperty(myData.getKey()+ ".USERS", treeSetToString(myData.getValue()));
				}
				for (Entry<String, TreeSet<String>> myData : mJobsToRoles.entrySet())
				{
					props.setProperty(myData.getKey()+ ".ROLES", treeSetToString(myData.getValue()));
				}
				for (Entry<String, String> myData : mJobsToOwner.entrySet())
				{
					props.setProperty(myData.getKey()+ ".OWNER", myData.getValue());
				}
				try (FileOutputStream os = new FileOutputStream(new File(BEISTDDatasets.M_PROPS, "auth.properties")))
				{
					props.storeToXML(os, "authorization properties");
				}
				long finish = System.currentTimeMillis();
				//theServlet.log("updateAuthorizationData completed in " + (finish-start)/1000 + " seconds");
			}
			else
			{
				//theServlet.log("updateAuthorizationData disabled/skipped");
			}
		}
	}
	
	static private void processProp(HttpServlet theServlet, String theProp, String theValue) throws IOException
	{
		//theServlet.log("processProp theProp=" + theProp);
		//theServlet.log("processProp theValue=" + theValue);
		String jobId = theProp.substring(0, theProp.length()-6);
		if (theProp.endsWith(".USERS"))
		{
			mJobsToUsers.put(jobId, stringToTreeSet(theValue));
		}
		else if (theProp.endsWith(".ROLES"))
		{
			mJobsToRoles.put(jobId, stringToTreeSet(theValue));
		}
		else if (theProp.endsWith(".OWNER"))
		{
			mJobsToOwner.put(jobId, theValue);
		}
	}
	
	synchronized static public void readAuthorizationData(HttpServlet theServlet) throws FileNotFoundException, IOException
	{
		if (BEIproperties.isLoginAllowed(theServlet))
		{
			//theServlet.log("readAuthorizationData version = " + edu.mda.bioinfo.bei.servlets.BEISTDDatasets.M_VERSION);
			long start = System.currentTimeMillis();
			Properties props = new Properties();
			//authorization properties
			try (FileInputStream is = new FileInputStream(new File(BEISTDDatasets.M_PROPS, "auth.properties")))
			{
				props.loadFromXML(is);
			}
			mJobsToUsers.clear();
			mJobsToRoles.clear();
			mJobsToOwner.clear();

			for (Entry<Object, Object> myData : props.entrySet())
			{
				processProp(theServlet, (String)myData.getKey(), (String)myData.getValue());
			}
			long finish = System.currentTimeMillis();
			//theServlet.log("readAuthorizationData completed in " + (finish-start)/1000 + " seconds");
		}
		else
		{
			//theServlet.log("readAuthorizationData disabled/skipped");
		}
		mIsRead = true;
	}
		
	synchronized static public void userHasAccessException(HttpServlet theServlet, HttpServletRequest theRequest, String theJobId) throws IOException, Exception
	{
		//theServlet.log("userHasAccessException version = " + edu.mda.bioinfo.bei.servlets.BEISTDDatasets.M_VERSION);
		if (false==mIsRead)
		{
			readAuthorizationData(theServlet);
		}
		if (false==userHasAccess(theServlet, theRequest, theJobId))
		{
			throw new Exception("User does not have access");
		}
	}
		
	synchronized static public boolean userHasAccess(HttpServlet theServlet, HttpServletRequest theRequest, String theJobId) throws IOException
	{
		if (false==mIsRead)
		{
			readAuthorizationData(theServlet);
		}
		String username = AuthUpdate.getUserName(theRequest);
		TreeSet<String> roles = AuthUpdate.getUserRoles(theRequest);
		return userHasAccess(theServlet, theJobId, username, roles);
	}
	
	synchronized static public boolean userHasAccess(HttpServlet theServlet, String theJobId, String theUser, TreeSet<String> theUserRoles) throws IOException
	{
		//theServlet.log("userHasAccess version = " + edu.mda.bioinfo.bei.servlets.BEISTDDatasets.M_VERSION);
		boolean grant = false;
		if (BEIproperties.isLoginAllowed(theServlet))
		{
			if (false==mIsRead)
			{
				readAuthorizationData(theServlet);
			}
			//theServlet.log("userHasAccess theJobId=" +theJobId);
			//theServlet.log("userHasAccess theUser=" +theUser);
			// owners always have access
			String owner =  mJobsToOwner.get(theJobId);
			TreeSet<String> users = getJobUsers(theServlet, theJobId);
			TreeSet<String> roles = getJobRoles(theServlet, theJobId);
			//theServlet.log("userHasAccess owner=" +owner);
			//theServlet.log("userHasAccess users=" +users);
			//theServlet.log("userHasAccess roles=" +roles);
			if (null==owner)
			{
				//theServlet.log("userHasAccess null owner, allow");
				// no owner, and/or created before authorization was added
				grant = true;
			}
			else if ("".equals(owner))
			{
				//theServlet.log("userHasAccess empty string owner, allow");
				// no owner, anyone can see
				grant = true;
			}
			else if (owner.equals(theUser))
			{
				//theServlet.log("userHasAccess is owner, allow");
				// owners always have access
				grant = true;
			}
			else if ((null!=theUser)&&(users.contains(theUser)))
			{
				//theServlet.log("userHasAccess in users");
				// if member of users list, allow
				grant = true;
			}
			else if (true==roles.removeAll(theUserRoles))
			{
				//theServlet.log("userHasAccess has role, allow");
				// if roles overlap, which means roles was changed (making removeAll return a true)
				// grant access
				grant = true;
			}
			else
			{
				//theServlet.log("userHasAccess no access");
			}
		}
		else
		{
			grant = true;
			//theServlet.log("userHasAccess disabled/skipped, allow anyone to access");
		}
		return grant;
	}
	
	
}
