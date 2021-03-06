// Copyright (c) 2011, 2012, 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021 University of Texas MD Anderson Cancer Center
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
import edu.mda.bcb.bei.status.JOB_STATUS;
import edu.mda.bcb.bei.status.JobStatus;
import java.io.File;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Tod-Casasent
 */
@MultipartConfig
@WebServlet(name = "JOBMWDownload", urlPatterns =
{
	"/JOBMWDownload"
})
public class JOBMWDownload extends BEIServletMixin
{
	public JOBMWDownload()
	{
		super("application/text;charset=UTF-8", true, JOBMWDownload.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		////////////////////////////////////////////////////////////////////
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		String isAlternate = request.getParameter("isAlternate");
		log("passed in isAlternate is " + isAlternate);
		File jobDir = new File(BEIUtils.M_OUTPUT, jobId);
		log("fileLocation is " + jobDir.getAbsolutePath());
		////////////////////////////////////////////////////////////////////
		String altStr = "PRI";
		if (isAlternate.equals("YES"))
		{
			altStr = "SEC";
		}
		// find subdir that starts with PRI or SEC
		File [] subdirs = jobDir.listFiles();
		File secPriDir = null;
		for (File sd : subdirs)
		{
			if (sd.isDirectory())
			{
				if (sd.getName().startsWith(altStr))
				{
					secPriDir = sd;
				}
			}
		}
		new File(secPriDir, "util").mkdirs();
		// copy HG38_Genes.tsv to secPriDir
		FileUtils.copyFile(new File(BEIUtils.M_UTILS, "HG38_Genes.tsv"), new File(new File(secPriDir, "util"), "HG38_Genes.tsv"));
		//
		File configFile = new File(secPriDir, "PROCESS.TXT");
		configFile.createNewFile();
		if (isAlternate.equals("YES"))
		{
			JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_MW_WAIT, request, this);
		}
		else
		{
			JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_MW_WAIT, request, this);
		}
		// status really comes from job status being rechecked
		theBuffer.append(jobId);
	}
}
