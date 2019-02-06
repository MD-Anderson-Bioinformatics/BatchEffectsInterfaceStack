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
import java.io.File;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author linux
 */
@MultipartConfig
public class JOBGDCDownload extends BEIServletMixin
{
	public JOBGDCDownload()
	{
		super("application/text;charset=UTF-8", true, JOBGDCDownload.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		////////////////////////////////////////////////////////////////////
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		String isAlternate = request.getParameter("isAlternate");
		log("passed in isAlternate is " + isAlternate);
		File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
		log("fileLocation is " + jobDir.getAbsolutePath());
		////////////////////////////////////////////////////////////////////
		String altStr = "PRI";
		if (isAlternate.equals("YES"))
		{
			altStr = "SEC";
		}
		// find subdir that starts with PRI or SEC
		File [] subdirs = jobDir.listFiles();
		File secPriDir = null;
		for (File sd : subdirs)
		{
			if (sd.isDirectory())
			{
				if (sd.getName().startsWith(altStr))
				{
					secPriDir = sd;
				}
			}
		}
		new File(secPriDir, "downloaded").mkdirs();
		new File(secPriDir, "converted").mkdirs();
		// copy HG38_Genes.tsv and fileto files to secPriDir
		FileUtils.copyDirectory(new File(BEISTDDatasets.M_GENOMICS_FILETO), secPriDir);
		FileUtils.copyDirectory(new File(BEISTDDatasets.M_GENOMICS_MAPS), secPriDir);
		//
		File configFile = new File(secPriDir, "PROCESS.TXT");
		configFile.createNewFile();
		if (isAlternate.equals("YES"))
		{
			JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_GDC_WAIT, request, this);
		}
		else
		{
			JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_GDC_WAIT, request, this);
		}
		// status really comes from job status being rechecked
		theBuffer.append(jobId);
	}
}
