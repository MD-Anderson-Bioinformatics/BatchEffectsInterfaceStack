/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import edu.mda.bioinfo.bei.servlets.BEIServletMixin;
import edu.mda.bioinfo.bei.status.JOB_STATUS;
import edu.mda.bioinfo.bei.status.JobStatus;
import javax.servlet.http.HttpServletRequest;

// JOBupdate USED IN C:\work\code\BatchEffects\docker\MBatchImage\installations\runMBatch1.R
// JOBupdate USED IN C:\work\code\BatchEffects\docker\MBatchImage\installations\runMBatch3.R

/**
 *
 * @author linux
 */
public class JOBupdate extends BEIServletMixin
{
	public JOBupdate()
	{
		super("application/text;charset=UTF-8", false, JOBupdate.class);
		// TODO: note this is not secured by authorization, since the download and Mbatch images need to change statuses
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		String status = request.getParameter("status");
		log("passed in jobId is " + jobId);
		log("passed in status is " + status);
		JobStatus.setJobStatus(jobId, JOB_STATUS.StringToEnum(status), request, this);
		// GDC Download looks for job id in response
		theBuffer.append(jobId);
	}
}
