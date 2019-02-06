/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.status;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.mda.bioinfo.bei.authorization.Authorization;
import edu.mda.bioinfo.bei.servlets.BEISTDDatasets;
import edu.mda.bioinfo.bei.servlets.BEIproperties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import java.io.StringWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServlet;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author linux
 */
public class JobStatus
{

	static private Properties M_JOB_STATUS = null;

	static public void readProperties() throws FileNotFoundException, IOException
	{
		// TODO: this reads the job file every time--change to only load when needed if this is too slow
		//if (null==M_JOB_STATUS)
		{
			M_JOB_STATUS = new Properties();
			File myfile = new File(BEISTDDatasets.M_PROPS, "job.properties");
			if (myfile.exists())
			{
				try (FileInputStream is = new FileInputStream(myfile))
				{
					M_JOB_STATUS.loadFromXML(is);
				}
			}
		}
	}

	static public void writeProperties() throws FileNotFoundException, IOException
	{
		if (null != M_JOB_STATUS)
		{
			try (FileOutputStream os = new FileOutputStream(new File(BEISTDDatasets.M_PROPS, "job.properties")))
			{
				M_JOB_STATUS.storeToXML(os, "job status");
			}
		}
	}

	synchronized static public void setJobStatus(String theJob, JOB_STATUS theStatus, HttpServletRequest theRequest, HttpServlet theServlet)
			throws FileNotFoundException, IOException
	{
		if (JOB_STATUS.MBATCHRUN_END_SUCCESS == theStatus)
		{
			copyToWebsite(theJob, theRequest, theServlet);
		}
		readProperties();
		JOB_STATUS status1 = getJobStatus(theJob);
		M_JOB_STATUS.setProperty(theJob, theStatus.mStatus);
		writeProperties();
		boolean sendEmail = false;
		JOB_STATUS status2 = getJobStatus(theJob);
		if (status1.mStatus.endsWith("_WAIT"))
		{
			if (status1 != status2)
			{
				sendEmail = true;
			}
		}
		if (true == sendEmail)
		{
			try
			{
				SendEmail.sendEmail(theJob, theServlet);
			}
			catch (MessagingException exp)
			{
				theServlet.log("Error sending email for " + theJob, exp);
			}
		}
	}

	synchronized static public void setJobInfo(String theJob, String theTag, String theEmail) throws FileNotFoundException, IOException
	{
		readProperties();
		if (null == theTag)
		{
			theTag = "";
		}
		if (null == theEmail)
		{
			theEmail = "";
		}
		// TODO: Possibly expand these if statments to check if key exits and value@key isn't null
		M_JOB_STATUS.setProperty(theJob + ".tag", theTag);
		//TODO: Add owner to job info M_JOB_STATUS.setProperty(theJob + ".owner", theJobInfo.get("owner"));
		M_JOB_STATUS.setProperty(theJob + ".email", theEmail);
		writeProperties();
		// jobStatusPostProcessing? I think no, because updating the info shouldn't (and can't) change the job status
	}

	synchronized static public void createNewJob(String theJob, String theUser, HttpServlet theServlet) throws FileNotFoundException, IOException
	{
		// createNewJob is defined independantly rather than executiing setJobStatus and setJobInfo
		// back to back to avoid reading and writing twice unncessisarily.
		readProperties();
		M_JOB_STATUS.setProperty(theJob, JOB_STATUS.NEWJOB_START.mStatus);
		M_JOB_STATUS.setProperty(theJob + ".tag", "");
		M_JOB_STATUS.setProperty(theJob + ".owner", (null == theUser ? "" : theUser));
		M_JOB_STATUS.setProperty(theJob + ".email", "");
		writeProperties();
		if ((!"".equals(theUser)) && (null != theUser))
		{
			Authorization.updateAuthorizationData(theServlet, theJob, theUser, new TreeSet<String>(), new TreeSet<String>());
		}
	}

	synchronized static public void deleteJob(String theJob, HttpServlet theServlet) throws FileNotFoundException, IOException
	{
		readProperties();
		for (String prop : M_JOB_STATUS.stringPropertyNames())
		{
			if (prop.startsWith(theJob))
			{
				M_JOB_STATUS.remove(prop);
			}
		}
		writeProperties();
		Authorization.removeAuthorizationData(theServlet, theJob);
	}

	synchronized static public JOB_STATUS getJobStatus(String theJob) throws FileNotFoundException, IOException
	{
		readProperties();
		return (JOB_STATUS.StringToEnum(M_JOB_STATUS.getProperty(theJob)));
	}

	synchronized static public String[] getJobList() throws IOException
	{
		// NOTE: This function only returns the JobId, Status entries of job.properties.
		// This is done so the JSON created in JOBlist.processRequest is properly formed
		readProperties();
		TreeSet<String> trees = new TreeSet<>();
		if (M_JOB_STATUS.size() > 0)
		{
			for (Object foo : M_JOB_STATUS.keySet())
			{
				// this ensures job info entries don't get returned in list
				if (!foo.toString().contains("."))
				{
					trees.add((String) foo);
				}

			}
		}
		return trees.toArray(new String[0]);
	}

	synchronized static public String getWithJobStatusUpdate(JOB_STATUS theOldStatusA, JOB_STATUS theNewStatusA, JOB_STATUS theOldStatusB, JOB_STATUS theNewStatusB,
			HttpServletRequest theRequest, HttpServlet theServlet) throws IOException
	{

		String jobId = null;
		readProperties();
		if (M_JOB_STATUS.size() > 0)
		{
			// sort to get oldest first
			TreeSet<String> keys = new TreeSet<>();
			for (Object strObj : M_JOB_STATUS.keySet())
			{
				if (null == jobId)
				{
					if (theOldStatusA.mStatus.equals(M_JOB_STATUS.getProperty((String) strObj)))
					{
						jobId = (String) strObj;
						setJobStatus(jobId, theNewStatusA, theRequest, theServlet);
					}
					else if (null != theOldStatusB)
					{
						if (theOldStatusB.mStatus.equals(M_JOB_STATUS.getProperty((String) strObj)))
						{
							jobId = (String) strObj;
							setJobStatus(jobId, theNewStatusB, theRequest, theServlet);
						}
					}
				}
			}
		}
		if (null == jobId)
		{
			jobId = "none";
		}
		return jobId;
	}

	static public HashMap<String, String> getJobMap(String theJob) throws IOException
	{
		HashMap<String, String> map = new HashMap<>();
		JOB_STATUS status = JobStatus.getJobStatus(theJob);
		map.put("jobid", theJob);
		map.put("status", status.mStatus);
		map.put("message", status.mReport);
		map.put("tag", JobStatus.M_JOB_STATUS.getProperty(theJob + ".tag", ""));
		map.put("owner", JobStatus.M_JOB_STATUS.getProperty(theJob + ".owner", ""));
		map.put("email", JobStatus.M_JOB_STATUS.getProperty(theJob + ".email", ""));
		String[] tail = getTailForStatus(theJob, status);
		String myTail = "";
		for (String line : tail)
		{
			myTail = myTail + line + "\n\n";
		}
		map.put("tail", myTail);
		return map;
	}

	// isLoginAllowed()
	static public String getResponseString(HttpServlet theServlet, String theJob, boolean includeTail) throws IOException
	{
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		//
		JOB_STATUS status = JobStatus.getJobStatus(theJob);
		StringWriter out = new StringWriter();
		out.append("{");
		out.append("\n\"jobid\":\"" + theJob + "\",");
		out.append("\n\"status\":\"" + status.mStatus + "\",");
		out.append("\n\"message\":\"" + status.mReport + "\",");
		out.append("\n\"tag\":\"" + JobStatus.M_JOB_STATUS.getProperty(theJob + ".tag", "") + "\",");
		out.append("\n\"owner\":\"" + JobStatus.M_JOB_STATUS.getProperty(theJob + ".owner", "") + "\",");
		out.append("\n\"email\":\"" + JobStatus.M_JOB_STATUS.getProperty(theJob + ".email", "") + "\",");
		out.append("\n\"authRoles\":" + gson.toJson(Authorization.getJobRoles(theServlet, theJob)) + ",");
		out.append("\n\"authUsers\":" + gson.toJson(Authorization.getJobUsers(theServlet, theJob)));
		if (includeTail)
		{
			out.append(",\n\"tail\":[");
			String[] tail = getTailForStatus(theJob, status);
			if ((null != tail) && (tail.length > 0))
			{
				boolean wrote = false;
				for (String line : tail)
				{
					if (true == wrote)
					{
						out.append(",");
					}
					else
					{
						wrote = true;
					}
					out.append("\n\"" + line + "\"");
				}
			}
			out.append("]");
		}
		out.append("\n}\n");
		String json = out.toString();
		return json;
	}

	static public String[] getTailForStatus(String theJob, JOB_STATUS theStatus)
	{
		String[] results = new String[1];
		File tailMe = null;
		if (theStatus.mStatus.startsWith("NEWJOB_"))
		{
			tailMe = new File(new File(BEISTDDatasets.M_OUTPUT, theJob), "DatasetConfig2.log");
			if (!tailMe.exists())
			{
				tailMe = new File(new File(BEISTDDatasets.M_OUTPUT, theJob), "DatasetConfig.log");
				if (!tailMe.exists())
				{
					tailMe = null;
				}
			}
		}
		else if (theStatus.mStatus.startsWith("MBATCHCONFIG_"))
		{
			tailMe = new File(new File(BEISTDDatasets.M_OUTPUT, theJob), "MBatchConfig_err.log");
			if (!tailMe.exists())
			{
				tailMe = null;
			}
		}
		else if (theStatus.mStatus.startsWith("MBATCHRUN_"))
		{
			tailMe = new File(new File(BEISTDDatasets.M_OUTPUT, theJob), "mbatch.log");
			if (!tailMe.exists())
			{
				tailMe = null;
			}
		}
		// add finding log based on data being used
		if (null != tailMe)
		{
			try
			{
				ArrayList<String> moreLogs = new ArrayList<>();
				moreLogs.add("");
				moreLogs.add("");
				moreLogs.add(tailMe.getAbsolutePath());
				moreLogs.addAll(Arrays.asList(FileTail.tail(tailMe.getAbsolutePath(), 100)));
				File[] logs = new File(BEISTDDatasets.M_OUTPUT, theJob).listFiles(new FilenameFilter()
				{
					@Override
					public boolean accept(File dir, String name)
					{
						return name.endsWith("_error.log");
					}
				});
				for (File myLog : logs)
				{
					moreLogs.add("");
					moreLogs.add("");
					moreLogs.add(tailMe.getAbsolutePath());
					moreLogs.addAll(Arrays.asList(FileTail.tail(myLog.getAbsolutePath(), 100)));
				}
				results = moreLogs.toArray(new String[0]);
			}
			catch (Exception err)
			{
				results[0] = "Error reading log file " + StringEscapeUtils.escapeJson(err.getMessage());
			}
		}
		else
		{
			results[0] = null;
		}
		return results;
	}
	
	public static String getFirstDir(File theDir)
	{
		String name = null;
		if (theDir.exists())
		{
			File[] ls = theDir.listFiles(File::isDirectory);
			if (null!=ls)
			{
				name = ls[0].getName();
			}
		}
		return name;
	}

	public static String getDefaultDir(File theResultDir, String theJobId)
	{
		// jobid, algorithm, diagram, subtype, diagram
		String level1 = theJobId;
		String level2 = "PCA";
		String level3 = null;
		String level4 = null;
		String level5 = null;
		// check for PCA directory and batch dir
		level3 = getFirstDir(new File(new File(theResultDir, level1), level2));
		if (null==level3)
		{
			// if no PCA, redo level 2 and 3
			level2 = getFirstDir(new File(theResultDir, level1));
			level3 = getFirstDir(new File(new File(theResultDir, level1), level2));
		}
		level4 = getFirstDir(new File(new File(new File(theResultDir, level1), level2), level3));
		level5 = getFirstDir(new File(new File(new File(new File(theResultDir, level1), level2), level3), level4));
		// build return dir
		String ret = "";
		if (null!=level1)
		{
			ret = ret + level1 + "/";
			if (null!=level2)
			{
				ret = ret + level2 + "/";
				if (null!=level3)
				{
					ret = ret + level3 + "/";
					if (null!=level4)
					{
						ret = ret + level4 + "/";
						if (null!=level5)
						{
							ret = ret + level5 + "/";
						}
					}
				}
			}
		}
		return ret;
	}

	public static void copyToWebsite(String theJob, HttpServletRequest theRequest, HttpServlet theServlet) throws IOException
	{
		// no longer copies--just creates a website.txt file with a link
		File jobDir = new File(BEISTDDatasets.M_OUTPUT, theJob);
		String index = theJob;
		File resultDir = new File(jobDir, "MBatch");
		theServlet.log("resultDir=" + resultDir);
		theServlet.log("theJob=" + theJob);
		String myPath = getDefaultDir(resultDir, theJob);
		theServlet.log("myPath=" + myPath);
		ArrayList<String> websiteTxt = new ArrayList<>();
		//
		// websiteTxt first line, path to directory
		//
		websiteTxt.add(myPath);
		//
		// websiteTxt second line, URL to JOB
		//
		//http://bei_service:8080/BatchEffectsInterface/JOBupdate?jobId=1516904638750&status=MBATCH_SUCCESS
		websiteTxt.add(buildUrlToJob(theRequest, theServlet, theJob));
		//
		// websiteTxt third line, URL to WEBSITE
		//
		//http://bei_service:8080/BatchEffectsViewer/?index=XXX&path=YYY
		websiteTxt.add(buildUrlToWebsite(theRequest, theServlet, index, myPath));
		Files.write(new File(jobDir, "website.txt").toPath(), websiteTxt);
	}

	public static String buildUrlToJob(HttpServletRequest request, HttpServlet theServlet, String theJob) throws IOException
	{
		return request.getScheme() + "://" + BEIproperties.getProperty("BEI_URL", theServlet) + request.getContextPath() + "/newjob.html?job=" + theJob;
	}

	public static String buildUrlToWebsite(HttpServletRequest request, HttpServlet theServlet, String theIndex, String thePath) throws IOException
	{
		return request.getScheme() + "://" + BEIproperties.getProperty("BEV_URL", theServlet) + "/BatchEffectsViewer/?index=" + theIndex + "&path=" + thePath;
	}
}
