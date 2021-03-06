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
import edu.mda.bcb.bei.servlets.BEIproperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "JOBwsurl", urlPatterns =
{
	"/JOBwsurl"
})
public class JOBwsurl extends BEIServletMixin
{
	public JOBwsurl()
	{
		super("application/text;charset=UTF-8", true, JOBwsurl.class);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		log("passed in jobId is " + jobId);
		theBuffer.append(getURL(jobId, this, request.getScheme()));
	}

	static protected String getURL(String theJobId, HttpServlet theServlet, String theScheme) throws IOException
	{
		File jobDir = new File(BEIUtils.M_OUTPUT, theJobId);
		theServlet.log("jobDir is " + jobDir.getAbsolutePath());
		String url = "";
		if (new File(jobDir, "website.txt").exists())
		{
			List<String> wsLine = Files.readAllLines(new File(jobDir, "website.txt").toPath());
			// third line is website URL
			url = wsLine.get(2);
		}
		theServlet.log("url=" + url);
		// check if URL has new version of path
		if (!url.contains("/BEV/view"))
		{
			theServlet.log("reset URL for new BEV");
			// copied from buildUrlToWebsite in JobStatus
			url = theScheme + "://" + BEIproperties.getProperty("BEV_URL", theServlet) + "/BEI/BEV/view?id=" + theJobId + "&index=BEI_JOB&alg=PCA%2B&lvl1=BatchId";
			theServlet.log("reset URL=" + url);
		}
		return url;
	}
}
