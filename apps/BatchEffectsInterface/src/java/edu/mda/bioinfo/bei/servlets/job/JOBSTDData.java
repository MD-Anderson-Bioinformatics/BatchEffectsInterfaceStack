/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.mda.bioinfo.bei.servlets.BEISTDDatasets;
import edu.mda.bioinfo.bei.servlets.BEIServletMixin;
import edu.mda.bioinfo.bei.status.JOB_STATUS;
import edu.mda.bioinfo.bei.status.JobStatus;
import edu.mda.bioinfo.bevindex.FileFindInZip;
import edu.mda.bioinfo.bevindex.display.DisplayRun;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeMap;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

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
		String dataname = request.getParameter("dataname");
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
			bw.write("dataname\t" + dataname);
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
		String source = null;	//index file name without .json
		String dataname = null;	//path within index
		for(String line : Files.readAllLines(theConfigFile.toPath()))
		{
			String [] splitted = line.split("\t", -1);
			if ("source".equals(splitted[0]))
			{
				source = splitted[1];
			}
			else if ("dataname".equals(splitted[0]))
			{
				dataname = splitted[1];
			}
		}
		if ((null==source)||(null==dataname))
		{
			throw new Exception("Configuration file did not provide full path to the data");
		}	
		//
		theServlet.log("use M_INDICES:" + BEISTDDatasets.M_INDICES);
		String indexFilename = source + ".json";
		File indexFile = new File(BEISTDDatasets.M_INDICES, indexFilename);
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		String zipFile = null;
		try (BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(indexFile.getAbsolutePath()), Charset.availableCharsets().get("UTF-8")))
		{
			DisplayRun dr = gson.fromJson(br, DisplayRun.class);
			TreeMap<String, String> dare = dr.dataRelations(source);
			zipFile = dare.get(dataname);
			if (null==zipFile)
			{
				throw new Exception("Zip file not found for " + source + " and " + dataname);
			}
		}
		if ("YES".equals(isAlternate))
		{
			FileFindInZip ffiz = new FileFindInZip();
			ffiz.findAndCopy(Paths.get(zipFile), "matrix_data.tsv", new File(theConfigFile.getParentFile(), "matrix_data2.tsv"));
			ffiz.findAndCopy(Paths.get(zipFile), "batches.tsv", new File(theConfigFile.getParentFile(), "batches2.tsv"));
			return "Std Data Success";
		}
		else
		{
			FileFindInZip ffiz = new FileFindInZip();
			ffiz.findAndCopy(Paths.get(zipFile), "matrix_data.tsv", new File(theConfigFile.getParentFile(), "matrix_data.tsv"));
			ffiz.findAndCopy(Paths.get(zipFile), "batches.tsv", new File(theConfigFile.getParentFile(), "batches.tsv"));
			return "Std Data Success";
		}
	}
}
