/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import edu.mda.bioinfo.bei.authorization.Authorization;
import edu.mda.bioinfo.bei.servlets.BEIServletMixin;
import edu.mda.bioinfo.bei.servlets.BEIproperties;
import edu.mda.bioinfo.bei.status.JobStatus;
import java.util.Arrays;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
public class JOBinfo extends BEIServletMixin
{
	public JOBinfo()
	{
		super("application/text;charset=UTF-8", true, JOBinfo.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		// Pass in user tag and update it if the value has changed
		String newJobTag = request.getParameter("jobTag");
		// do not modify job owner, included for updating auths
		String jobOwner = request.getParameter("jobOwner");
		String newJobEmail = request.getParameter("jobEmail");
		JobStatus.setJobInfo(jobId, newJobTag, newJobEmail);
		//
		if (true==BEIproperties.isLoginAllowed(this))
		{
			TreeSet<String> jobAuthUsers = new TreeSet<>();
			TreeSet<String> jobAuthRoles = new TreeSet<>();
			String [] authUsers = request.getParameterValues("jobAuthUsers[]");
			log("passed in jobAuthUsers is " + Arrays.toString(authUsers));
			if (null!=authUsers)
			{
				jobAuthUsers.addAll(Arrays.asList(request.getParameterValues("jobAuthUsers[]")));
			}
			String [] authRoles = request.getParameterValues("jobAuthRoles[]");
			log("passed in jobAuthRoles is " + Arrays.toString(authRoles));
			if (null!=authRoles)
			{
				jobAuthRoles.addAll(Arrays.asList(request.getParameterValues("jobAuthRoles[]")));
			}
			Authorization.updateAuthorizationData(this, jobId, jobOwner, jobAuthUsers, jobAuthRoles);
		}
		// status really comes from job status being rechecked
		theBuffer.append(jobId);
	}

}
