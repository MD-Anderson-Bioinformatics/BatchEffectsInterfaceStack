/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import edu.mda.bioinfo.bei.servlets.BEISTDDatasets;
import edu.mda.bioinfo.bei.servlets.BEIServletMixin;
import edu.mda.bioinfo.bei.status.JOB_STATUS;
import edu.mda.bioinfo.bei.status.JobStatus;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author linux
 */
public class JOBSTDData extends BEIServletMixin
{
	public JOBSTDData()
	{
		super("application/text;charset=UTF-8", true, JOBSTDData.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		String isAlternate = request.getParameter("isAlternate");
		log("passed in isAlternate is " + isAlternate);
		File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
		String source = request.getParameter("source");
		log("passed in source is " + source);
		String date = request.getParameter("date");
		String program = request.getParameter("program");
		String project = request.getParameter("project");
		String workflow = request.getParameter("workflow");
		String datatype = request.getParameter("datatype");
		// use isAlternate to conditionally construct StdDataInfo.tsv or StdDataInfo2.tsv 
		File configFile = null;
		if("YES".equals(isAlternate))
		{
			configFile = new File(jobDir, "StdDataInfo2.tsv");
		}
		else
		{
			configFile = new File(jobDir, "StdDataInfo.tsv");
		}
		try(BufferedWriter bw = java.nio.file.Files.newBufferedWriter(Paths.get(configFile.getAbsolutePath()), Charset.availableCharsets().get("UTF-8")))
		{
			bw.write("source\t" + source);
			bw.newLine();
			bw.write("date\t" + date);
			bw.newLine();
			bw.write("program\t" + program);
			bw.newLine();
			bw.write("project\t" + project);
			bw.newLine();
			bw.write("workflow\t" + workflow);
			bw.newLine();
			bw.write("datatype\t" + datatype);
			bw.newLine();
		}
		String testSuccess  = copyStandardDataFile(configFile, isAlternate, this);
		if(testSuccess.equals("Std Data Success"))
		{
			if(isAlternate.equals("YES"))
			{
				// succesfully retrieved standardized data, for a secondary dataset operation
				JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_DONE, request, this); 
			}
			else
			{
				// succesfully retrieved standardized data, for a primary dataset operation
				JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_DONE, request, this);
			}
		}
		else
		{
			if(isAlternate.equals("YES"))
			{
				// failed to retrieve standardized data, for a secondary dataset operation
				JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_DONE, request, this);
			}
			else
			{
				// failed to retrieve data, for a primary dataset operation
				JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_START, request, this);
			}
		}
		// actual failure/success is retrieved separately from job status
		theBuffer.append(jobId);
	}
	
	// this function is used to copy a specific matrix_data.tsv and batches.tsv from the /BEI/ARCHIVES/ datasets into
	// the jobDir based off information in the param theCOnfigFile. It returns a boolean indicating whether the process was succesfull.
	public static String copyStandardDataFile(File theConfigFile, String isAlternate, HttpServlet theServlet) throws IOException, Exception
	{
		// read config file (tab delimited data pairs)
		String source = null;	//TCGA
		String date = null;	//2016_07_28_1308
		String program = null;	//gbm
		String project = null;	//methylation
		String workflow = null;	//humanmethylation450_level3
		String datatype = null;	//Level_2
		for(String line : Files.readAllLines(theConfigFile.toPath()))
		{
			String [] splitted = line.split("\t", -1);
			if ("source".equals(splitted[0]))
			{
				source = splitted[1];
			}
			else if ("date".equals(splitted[0]))
			{
				date = splitted[1];
			}
			else if ("program".equals(splitted[0]))
			{
				program = splitted[1];
			}
			else if ("project".equals(splitted[0]))
			{
				project = splitted[1];
			}
			else if ("workflow".equals(splitted[0]))
			{
				workflow = splitted[1];
			}
			else if ("datatype".equals(splitted[0]))
			{
				datatype = splitted[1];
			}
		}
		if ((null==source)||(null==date)||(null==program)||(null==project)||(null==workflow)||(null==datatype))
		{
			throw new Exception("Configuration file did not provide full path to the data");
		}	
		File sourceDir = new File(new File(new File(new File(new File(new File(BEISTDDatasets.M_ARCHIVES, source), date), program), project), workflow), datatype);
		theServlet.log("sourceDir=" + sourceDir);
		if ("YES".equals(isAlternate))
		{
			// copyFile rather than copyFiletoDirectory allows for renaming
			FileUtils.copyFile(new File(sourceDir, "matrix_data.tsv"), new File(theConfigFile.getParentFile(), "matrix_data2.tsv"));
			FileUtils.copyFile(new File(sourceDir, "batches.tsv"), new File(theConfigFile.getParentFile(), "batches2.tsv"));
			return "Std Data Success";

		}
		else
		{
			FileUtils.copyFileToDirectory(new File(sourceDir, "matrix_data.tsv"), theConfigFile.getParentFile());
			FileUtils.copyFileToDirectory(new File(sourceDir, "batches.tsv"), theConfigFile.getParentFile());
			return "Std Data Success";
		}
	}
}
