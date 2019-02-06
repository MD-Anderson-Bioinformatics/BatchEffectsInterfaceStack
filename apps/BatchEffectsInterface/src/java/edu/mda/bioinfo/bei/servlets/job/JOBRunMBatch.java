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

/**
 *
 * @author linux
 */
public class JOBRunMBatch extends BEIServletMixin
{
	public JOBRunMBatch()
	{
		super("application/text;charset=UTF-8", true, JOBRunMBatch.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		JobStatus.setJobStatus(jobId, JOB_STATUS.MBATCHCONFIG_END, request, this);
		JobStatus.setJobStatus(jobId, JOB_STATUS.MBATCHRUN_START_WAIT, request, this);
		theBuffer.append(jobId);
	}
}
