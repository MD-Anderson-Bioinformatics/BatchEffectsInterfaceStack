/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.status;

/**
 *
 * @author linux
 */
public enum JOB_STATUS
{
	NEWJOB_START("NEWJOB_START", "Waiting for new job data setup."),
	NEWJOB_PRIMARY_GDC_MANIFEST("NEWJOB_PRIMARY_GDC_MANIFEST", "Primary GDC Manifest Files Uploaded."),
	NEWJOB_PRIMARY_GDC_WAIT("NEWJOB_PRIMARY_GDC_WAIT", "Primary GDC Download Queued."),
	NEWJOB_PRIMARY_GDCRUN_WAIT("NEWJOB_PRIMARY_GDCRUN_WAIT", "Primary GDC Download in Progress."),
	NEWJOB_PRIMARY_USER_MATRIX("NEWJOB_PRIMARY_USER_MATRIX", "Primary Matrix Data Uploaded. Waiting for Batch Data."),
	NEWJOB_PRIMARY_DONE("NEWJOB_PRIMARY_DONE", "Primary Data Available. Waiting for Secondary Data."),
	NEWJOB_SECONDARY_GDC_MANIFEST("NEWJOB_SECONDARY_GDC_MANIFEST", "Secondaryd GDC Manifest Files Uploaded."),
	NEWJOB_SECONDARY_GDC_WAIT("NEWJOB_SECONDARY_GDC_WAIT", "Secondary Matrix GDC Download Queued"),
	NEWJOB_SECONDARY_GDCRUN_WAIT("NEWJOB_SECONDARY_GDCRUN_WAIT", "Secondary Matrix GDC Download in Progress"),
	NEWJOB_SECONDARY_USER_MATRIX("NEWJOB_SECONDARY_USER_MATRIX", "Secondary Matrix Data Uploaded. Waiting for Batch Data."),
	NEWJOB_SECONDARY_DONE("NEWJOB_SECONDARY_DONE", "Secondary Data Available."),
	NEWJOB_DONE("NEWJOB_DONE", "Data Setup Complete. Ready for MBatch Configuration."),
	
	MBATCHCONFIG_START("MBATCHCONFIG_START", "MBatch Configuration in Process"),
	MBATCHCONFIG_END("MBATCHCONFIG_END", "MBatch Configuration Complete"),
	
	MBATCHRUN_START_WAIT("MBATCHRUN_START_WAIT", "MBatch Run Queued"),
	MBATCHRUN_ACCEPTED_WAIT("MBATCHRUN_ACCEPTED_WAIT", "MBatch Run Accepted for Processing"),
	MBATCHRUN_RUNNING_WAIT("MBATCHRUN_RUNNING_WAIT", "MBatch Run in Progress"),
	MBATCHRUN_END_SUCCESS("MBATCHRUN_END_SUCCESS", "MBatch Run Finished Successfully"),
	MBATCHRUN_END_FAILURE("MBATCHRUN_END_FAILURE", "MBatch Run Failed");

	public final String mStatus;
	public final String mReport;

	JOB_STATUS(String theStatus, String theReportString)
	{
		this.mStatus = theStatus;
		this.mReport = theReportString;
	}

	static public JOB_STATUS StringToEnum(String theStatus)
	{
		JOB_STATUS result = null;
		for (JOB_STATUS status : JOB_STATUS.values())
		{
			if (status.mStatus.equals(theStatus))
			{
				result = status;
			}
		}
		return result;
	}

}
