/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import edu.mda.bioinfo.bei.servlets.BEIServletMixin;
import edu.mda.bioinfo.bei.status.JobStatus;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
public class JOBstatus extends BEIServletMixin
{
	public JOBstatus()
	{
		super("application/json;charset=UTF-8", true, JOBstatus.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		log("version = " + edu.mda.bioinfo.bei.servlets.BEISTDDatasets.M_VERSION);
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		theBuffer.append(JobStatus.getResponseString(this, jobId, /*includeTail=*/ true));
	}
}