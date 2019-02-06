/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import edu.mda.bioinfo.bei.authorization.Authorization;
import edu.mda.bioinfo.bei.servlets.AuthUpdate;
import edu.mda.bioinfo.bei.servlets.BEIServletMixin;
import edu.mda.bioinfo.bei.status.JobStatus;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
public class JOBlist extends BEIServletMixin
{
	public JOBlist()
	{
		super("application/json;charset=UTF-8", true, JOBlist.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		// TODO: jobStatus.readProperties(); would refresh the job list from ...
		// the xml before returning the results to user. This also makes the user wait ...
		// for possibly no benefit.
		theBuffer.append("[\n");
		boolean wrote = false;
		String username = AuthUpdate.getUserName(request);
		TreeSet<String> roles = AuthUpdate.getUserRoles(request);
		for(String job : JobStatus.getJobList())
		{
			if (Authorization.userHasAccess(this, job, username, roles))
			{
				if (false==wrote)
				{
					wrote = true;
				}
				else
				{
					theBuffer.append(",");
				}
				theBuffer.append(JobStatus.getResponseString(this, job, /*includeTail=*/ false));
			}
		}
		theBuffer.append("\n]\n");
	}
}
