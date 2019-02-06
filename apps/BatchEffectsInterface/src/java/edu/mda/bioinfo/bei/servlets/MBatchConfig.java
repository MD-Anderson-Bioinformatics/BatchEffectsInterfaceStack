/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author linux
 */
public class MBatchConfig extends BEIServletMixin
{
	public MBatchConfig()
	{
		super("application/json;charset=UTF-8", true, MBatchConfig.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		String action = request.getParameter("action");
		log("passed in action is " + action);
		// action options
		boolean readOrInitFile = ("initialize".equals(action));
		boolean writeFile = ("write".equals(action));
		// get list of parameters and write them
		File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
		Enumeration<String> sEnum = request.getParameterNames();
		while(sEnum.hasMoreElements())
		{
			String paraName = sEnum.nextElement();
			String paraValue = request.getParameter(paraName);
			log("'" + paraName + "' is '" + paraValue + "'");
		}
		File configFile = new File(jobDir, "MBatchConfig.tsv");
		if (true==readOrInitFile)
		{
			if (!configFile.exists())
			{
				writeConfigFile(request, configFile);
			}
		}
		else if (true==writeFile)
		{
			writeConfigFile(request, configFile);
		}
		readConfigFile(theBuffer, configFile);
	}
	
	public void writeConfigFile(HttpServletRequest theRequest, File theConfigFile) throws IOException
	{
		try(BufferedWriter bw = java.nio.file.Files.newBufferedWriter(Paths.get(theConfigFile.getAbsolutePath()), Charset.availableCharsets().get("UTF-8")))
		{
			boolean foundTitle = false;
			Enumeration<String> sEnum = theRequest.getParameterNames();
			while(sEnum.hasMoreElements())
			{
				String paraName = sEnum.nextElement();
				String value = null;
				if ((!"_".equals(paraName))&&(!"action".equals(paraName)))
				{
					if ("title".equals(paraName))
					{
						foundTitle = true;
					}
					if (paraName.endsWith("[]"))
					{
						paraName = paraName.replace("[]", "");
						String [] tmp = theRequest.getParameterValues("batchTypesForMBatch[]");
						if (null!=tmp)
						{
							for (String val : tmp)
							{
								if (null==value)
								{
									value = val;
								}
								else
								{
									value = value + "," + val;
								}
							}
						}
					}
					else if (paraName.endsWith("Flag"))
					{
						value = theRequest.getParameter(paraName);
						if ("true".equalsIgnoreCase(value))
						{
							value = "TRUE";
						}
						else
						{
							value = "FALSE";
						}
					}
					else
					{
						value = theRequest.getParameter(paraName);
					}
					bw.write(paraName + "\t" + value);
					bw.newLine();
				}
			}
			if (false==foundTitle)
			{
				bw.write("title\tBatch Effects Run from BEI");
				bw.newLine();
			}
		}
	}

	public void readConfigFile(StringBuffer theBuffer, File theConfigFile) throws IOException
	{
		List<String> lines = java.nio.file.Files.readAllLines(theConfigFile.toPath());
		boolean wrote = false;
		theBuffer.append("{");
		for (String line : lines)
		{
			String [] splitted = line.split("\t");
			if (wrote)
			{
				theBuffer.append(",");
			}
			else
			{
				wrote = true;
			}
			if (splitted[0].endsWith("Array"))
			{
				if ("null".equals(splitted[1]))
				{
					theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":[]");
				}
				else
				{
					theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":[");
					String [] arrayList = splitted[1].split(",", -1);
					boolean listStarted = false;
					for (String ele : arrayList)
					{
						if (true==listStarted)
						{
							theBuffer.append(",");
						}
						else
						{
							listStarted = true;
						}
						theBuffer.append("\"" + StringEscapeUtils.escapeJson(ele) + "\"");
					}
					theBuffer.append("]");
				}
			}
			else if (splitted[0].endsWith("Flag"))
			{
				if ("true".equalsIgnoreCase(splitted[1]))
				{
					theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":\"true\"");
				}
				else
				{
					theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":\"false\"");
				}
			}
			else
			{
				theBuffer.append("\n\"" + StringEscapeUtils.escapeJson(splitted[0]) + "\":\"" + StringEscapeUtils.escapeJson(splitted[1]) + "\"");
			}
		}
		theBuffer.append("\n}\n");
	}
}
