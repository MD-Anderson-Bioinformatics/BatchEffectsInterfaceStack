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

// JOBnext USED IN C:\work\code\BatchEffects\docker\MBatchImage\installations\runMBatch1.R
// JOBnext USED IN GDCDownload edu.mda.bcb.gdc.download.DatasetConfig

/**
 *
 * @author linux
 */
public class JOBnext extends BEIServletMixin
{
	public JOBnext()
	{
		super("application/text;charset=UTF-8", true, JOBnext.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobType = request.getParameter("jobType");
		log("passed in jobType is '" + jobType + "'");
		String message = "Unknown job type '" + jobType + "'";
		// JobStatus.getWithJobStatusUpdate will find a job of the passed jobType and ...
		// set the message equal to the jobId. The message is then returned to the calling container and ...
		// initiates an Mbatch/GDCDownload process inside the container
		if ("MBATCH".equals(jobType))
		{
			message = JobStatus.getWithJobStatusUpdate(JOB_STATUS.MBATCHRUN_START_WAIT, JOB_STATUS.MBATCHRUN_ACCEPTED_WAIT, null, null, request, this);
		}
		else if ("GDCDLD".equals(jobType))
		{
			message = JobStatus.getWithJobStatusUpdate(JOB_STATUS.NEWJOB_PRIMARY_GDC_WAIT, JOB_STATUS.NEWJOB_PRIMARY_GDCRUN_WAIT,
					JOB_STATUS.NEWJOB_SECONDARY_GDC_WAIT, JOB_STATUS.NEWJOB_SECONDARY_GDCRUN_WAIT, request, this);

		}
		theBuffer.append(message);
	}
}
