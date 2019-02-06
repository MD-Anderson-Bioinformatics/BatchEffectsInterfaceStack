/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import edu.mda.bioinfo.bei.servlets.BEISTDDatasets;
import edu.mda.bioinfo.bei.servlets.BEIServletMixin;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
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
		theBuffer.append(getURL(jobId, this));
	}

	static protected String getURL(String theJobId, HttpServlet theServlet) throws IOException
	{
		File jobDir = new File(BEISTDDatasets.M_OUTPUT, theJobId);
		theServlet.log("jobDir is " + jobDir.getAbsolutePath());
		String url = "";
		if (new File(jobDir, "website.txt").exists())
		{
			List<String> wsLine = Files.readAllLines(new File(jobDir, "website.txt").toPath());
			// third line is website URL
			url = wsLine.get(2);
		}
		theServlet.log("url=" + url);
		return url;
	}
}