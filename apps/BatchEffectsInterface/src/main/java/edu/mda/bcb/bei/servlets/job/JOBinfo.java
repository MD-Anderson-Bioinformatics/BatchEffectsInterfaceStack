// Copyright (c) 2011-2022 University of Texas MD Anderson Cancer Center
//
// This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
//
// MD Anderson Cancer Center Bioinformatics on GitHub <https://github.com/MD-Anderson-Bioinformatics>
// MD Anderson Cancer Center Bioinformatics at MDA <https://www.mdanderson.org/research/departments-labs-institutes/departments-divisions/bioinformatics-and-computational-biology.html>

package edu.mda.bcb.bei.servlets.job;

import edu.mda.bcb.bei.authorization.Authorization;
import edu.mda.bcb.bei.servlets.BEIServletMixin;
import edu.mda.bcb.bei.servlets.BEIproperties;
import edu.mda.bcb.bei.status.JobStatus;
import edu.mda.bcb.bei.utils.ScanCheck;
import java.util.Arrays;
import java.util.TreeSet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBinfo", urlPatterns =
{
	"/JOBinfo"
})
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
		ScanCheck.checkForMetaCharacters(jobId);
		log("passed in jobId is " + jobId);
		JobStatus.checkJobId(jobId);
		// Pass in user tag and update it if the value has changed
		String newJobTag = request.getParameter("jobTag");
		ScanCheck.checkForMetaCharacters(newJobTag);
		// do not modify job owner, included for updating auths
		String jobOwner = request.getParameter("jobOwner");
		ScanCheck.checkForMetaCharacters(jobOwner);
		String newJobEmail = request.getParameter("jobEmail");
		ScanCheck.checkForMetaCharacters(newJobEmail);
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
				for (String test : authUsers)
				{
					ScanCheck.checkForMetaCharacters(test);
				}
				jobAuthUsers.addAll(Arrays.asList(request.getParameterValues("jobAuthUsers[]")));
			}
			String [] authRoles = request.getParameterValues("jobAuthRoles[]");
			log("passed in jobAuthRoles is " + Arrays.toString(authRoles));
			if (null!=authRoles)
			{
				for (String test : authRoles)
				{
					ScanCheck.checkForMetaCharacters(test);
				}
				jobAuthRoles.addAll(Arrays.asList(request.getParameterValues("jobAuthRoles[]")));
			}
			Authorization.updateAuthorizationData(this, jobId, jobOwner, jobAuthUsers, jobAuthRoles);
		}
		// status really comes from job status being rechecked
		theBuffer.append(jobId);
	}

}
