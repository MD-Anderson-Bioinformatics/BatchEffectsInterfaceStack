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
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author linux
 */
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
		log("passed in jobId is " + jobId);
		if ((jobId!=null)&&!("".equals(jobId)))
		{
			File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
			boolean success = deleteWebsiteFiles(jobId, this);
			if(true==success)
			{
				success = FileUtils.deleteQuietly(jobDir);
				if(true==success)
				{
					JobStatus.deleteJob(jobId, this);
					theBuffer.append("Successfully deleted " + jobId);
				}
				else
				{
					theBuffer.append("Unable to delete " + jobId);
				}
			}
			else
			{
				theBuffer.append("Unable to delete website directory " + jobId);
			}
		}
		else
		{
			theBuffer.append("No job specified");
		}
	}
	
	protected boolean deleteWebsiteFiles(String theJobId, JOBdelete theLog) throws IOException
	{
		boolean success = true;
		File [] files = new File(BEISTDDatasets.M_WEBSITE).listFiles();
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
