/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets;

import edu.mda.bioinfo.bei.processes.BatchdataObj;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
public class Batchdata extends BEIServletMixin
{
	public Batchdata()
	{
		super("application/json;charset=UTF-8", true, Batchdata.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		String isAlternate = request.getParameter("isAlternate");
		// NO, YES, YES-IGNORE
		log("passed in isAlternate is " + isAlternate);
		boolean isAltFlag = isAlternate.startsWith("YES");
		Collection<BatchdataObj> batches = loadBatchdata((new File(BEISTDDatasets.M_OUTPUT, jobId)), isAltFlag);
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		String json = gson.toJson(batches);
		json = json.replace("}},", "}},\n");
		theBuffer.append(json);
	}

	protected Collection<BatchdataObj> loadBatchdata(File theDownloadDir, boolean theIsAlternateP) throws Exception
	{
		TreeMap<String, BatchdataObj> batchTypesTo = new TreeMap<>();
		if (!theDownloadDir.exists())
		{
			throw new Exception("Download directory not found:" + theDownloadDir);
		}
		else
		{
			File batchFile = null;
			if(false == theIsAlternateP)
			{
				batchFile = new File(theDownloadDir, "batches.tsv");
				if (!batchFile.exists())
				{
					throw new Exception("Primary Batch file not found:" + batchFile);
				}
			}
			else
			{
				batchFile = new File(theDownloadDir, "batches2.tsv");    
				if (!batchFile.exists())
				{
					// won't always be there
					batchFile = null;
				}
			}
			if (null!=batchFile)
			{
				// batch type name, batch names with count
				// read the file
				ArrayList<String> headers = null;
				try (BufferedReader br = new BufferedReader(new FileReader(batchFile)))
				{
					String line;
					while ((line = br.readLine()) != null)
					{
						if (null==headers)
						{
							// populate headers (batch types)
							headers = new ArrayList<>();
							headers.addAll(Arrays.asList(line.split("\t", -1)));
							for(String header : headers)
							{
								batchTypesTo.put(header, new BatchdataObj(header));
							}
						}
						else
						{
							// populate batches
							String [] splitted = line.split("\t", -1);
							for(int index=0; index<headers.size(); index++ )
							{
								String batchtype = headers.get(index);
								String batch = splitted[index];
								BatchdataObj batchtypeObj = batchTypesTo.get(batchtype);
								Integer batchcount = batchtypeObj.mBatches.get(batch);
								if (null==batchcount)
								{
									// use auto int to Integer
									batchcount = 1;
								}
								else
								{
									// use auto add int to Integer
									batchcount += 1;
								}
								batchtypeObj.mBatches.put(batch, batchcount);
							}
						}
					}
				}
			}
		}
		return batchTypesTo.values();
	}
}
