/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets.job;

import edu.mda.bioinfo.bei.authorization.Authorization;
import edu.mda.bioinfo.bei.servlets.BEISTDDatasets;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author linux
 */
public class JOBdownload extends HttpServlet
{
	/**
	 * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
	 * methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		try
		{
			String jobId = request.getParameter("jobId");
			log("passed in jobId is " + jobId);
			Authorization.userHasAccessException(this, request, jobId);
			if ((jobId != null) && !("".equals(jobId)))
			{
				File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
				log("jobDir is " + jobDir.getAbsolutePath());
				response.setContentType("application/zip;charset=UTF-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + jobId + "_Results.zip\"");
				try(ServletOutputStream out = response.getOutputStream())
				{
					zipFolder(jobDir.getAbsolutePath(), out);
					response.setStatus(200);
				}
			}
		}
		catch (Exception exp)
		{
			log("JOBdownload::processRequest failed", exp);
			response.setStatus(500);
			response.sendError(500, exp.getMessage());
		}
	}

	// zip code modified from https://stackoverflow.com/questions/32090198/zip-directory-with-java-util-zip-and-keeping-structure
	private void zipFolder(String srcFolder, ServletOutputStream out) throws IOException
	{
		ZipOutputStream zip = null;
		try
		{
			zip = new ZipOutputStream(out);
			addFolderToZip("", srcFolder, zip);
		}
		finally
		{
			if (null != zip)
			{
				zip.flush();
				zip.close();
			}
		}
	}

	// recursively add files to the zip files
	private void addFileToZip(String path, String srcFile, ZipOutputStream zip, boolean flag) throws IOException
	{
		//
		// create the file object for inputs
		File folder = new File(srcFile);
		// if the folder is empty add empty folder to the Zip file
		if (flag == true)
		{
			zip.putNextEntry(new ZipEntry(path + File.separator + folder.getName() + File.separator));
		}
		else
		{
			// if the current name is directory, recursively traverse it to get the files
			if (folder.isDirectory())
			{
				// if folder is not empty
				addFolderToZip(path, srcFile, zip);
			}
			else
			{
				// write the file to the output
				byte[] buf = new byte[1024];
				int len;
				FileInputStream in = new FileInputStream(srcFile);
				zip.putNextEntry(new ZipEntry(path + File.separator + folder.getName()));
				while ((len = in.read(buf)) > 0)
				{
					// Write the Result
					zip.write(buf, 0, len);
				}
				in.close();
			}
		}
	}

	// add folder to the zip file
	private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws IOException
	{
		File folder = new File(srcFolder);
		// check the empty folder
		if (folder.list().length == 0)
		{
			log(folder.getName());
			addFileToZip(path, srcFolder, zip, true);
		}
		else
		{
			// list the files in the folder
			for (String fileName : folder.list())
			{
				if (path.equals(""))
				{
					addFileToZip(folder.getName(), srcFolder + File.separator + fileName, zip, false);
				}
				else
				{
					addFileToZip(path + File.separator + folder.getName(), srcFolder + File.separator + fileName, zip, false);
				}
			}
		}
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo()
	{
		return "Short description";
	}// </editor-fold>
}