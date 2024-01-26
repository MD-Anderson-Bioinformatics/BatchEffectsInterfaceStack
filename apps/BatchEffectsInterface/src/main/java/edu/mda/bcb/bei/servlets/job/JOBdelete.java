// Copyright (c) 2011-2024 University of Texas MD Anderson Cancer Center
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

import edu.mda.bcb.bei.utils.BEIUtils;
import edu.mda.bcb.bei.servlets.BEIServletMixin;
import edu.mda.bcb.bei.status.JobStatus;
import edu.mda.bcb.bei.utils.ScanCheck;
import java.io.File;
import java.io.IOException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBdelete", urlPatterns =
{
	"/JOBdelete"
})
public class JOBdelete extends BEIServletMixin
{
	public JOBdelete()
	{
		super("application/text;charset=UTF-8", true, JOBdelete.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		ScanCheck.checkForMetaCharacters(jobId);
		log("passed in jobId is " + jobId);
		JobStatus.checkJobId(jobId);
		if ((jobId!=null)&&!("".equals(jobId)))
		{
			File jobDir = new File(BEIUtils.M_OUTPUT, jobId);
			log("JOBdelete jobDir = " + jobDir);
			boolean success = deleteWebsiteFiles(jobId, this);
			log("JOBdelete deleteWebsiteFiles = " + success);
			if(true==success)
			{
				log("JOBdelete deleteQuietly before");
				success = FileUtils.deleteQuietly(jobDir);
				log("JOBdelete deleteQuietly = " + success);
				if(true==success)
				{
					log("JOBdelete deleteJob before");
					JobStatus.deleteJob(jobId, this);
					log("JOBdelete deleteJob after");
					theBuffer.append("Successfully deleted " + jobId);
					log("Successfully deleted " + jobId);
				}
				else
				{
					theBuffer.append("Unable to delete " + jobId);
					log("Unable to delete " + jobId);
				}
			}
			else
			{
				theBuffer.append("Unable to delete website directory " + jobId);
				log("Unable to delete website directory " + jobId);
			}
		}
		else
		{
			theBuffer.append("No job specified");
			log("No job specified");
		}
	}
	
	protected boolean deleteWebsiteFiles(String theJobId, JOBdelete theLog) throws IOException
	{
		boolean success = true;
		File [] files = new File(BEIUtils.M_WEBSITE).listFiles();
		if (null!=files)
		{
			for (File myFile : files)
			{
				if (myFile.getName().startsWith(theJobId))
				{
					theLog.log("deleteWebsiteDirs " + myFile.getAbsolutePath());
					boolean test = FileUtils.deleteQuietly(myFile);
					if (test==false)
					{
						success = false;
					}
				}
			}
		}
		return success;
	}

}
