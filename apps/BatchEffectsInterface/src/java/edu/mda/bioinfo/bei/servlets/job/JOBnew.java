/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import edu.mda.bioinfo.bei.servlets.BEISTDDatasets;
import edu.mda.bioinfo.bei.servlets.BEIServletMixin;
import edu.mda.bioinfo.bei.status.JobStatus;
import java.io.File;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
public class JOBnew extends BEIServletMixin
{
	public JOBnew()
	{
		super("application/text;charset=UTF-8", true, JOBnew.class);
	}

	static synchronized String newJobId()
	{
		try
		{
			Thread.sleep(1);
		}
		catch(Exception ignore)
		{
			// ignore
		}
		return Long.toString(System.currentTimeMillis());
	};

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = newJobId();
		File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
		jobDir.mkdir();
		JobStatus.createNewJob(jobId, request.getRemoteUser(), this);
		log("fileLocation is " + jobDir.getAbsolutePath());
		theBuffer.append(jobId);
	}
}
