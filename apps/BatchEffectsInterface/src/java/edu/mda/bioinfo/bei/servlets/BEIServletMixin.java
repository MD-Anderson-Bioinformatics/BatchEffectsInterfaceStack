/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets;

import edu.mda.bioinfo.bei.authorization.Authorization;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author linux
 */
public abstract class BEIServletMixin extends HttpServlet
{
	protected String mReturnType = "";
	protected boolean mCheckUserAuthorization = true;
	protected String mErrorFilename = null;
	protected File mErrorFile = null;
	
	public BEIServletMixin(String theReturnType, boolean theCheckUserAuthorization, Class theChildClass)
	{
		super();
		mReturnType = theReturnType;
		mCheckUserAuthorization = theCheckUserAuthorization;
		if (null!=theChildClass)
		{
			mErrorFilename = theChildClass.getName() + "_error.log";
		}
		else
		{
			mErrorFilename = null;
		}
		mErrorFile = null;
	}
	
	abstract protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception;
	
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
			log("version = " + edu.mda.bioinfo.bei.servlets.BEISTDDatasets.M_VERSION);
			String jobId = request.getParameter("jobId");
			log("passed in jobId is " + jobId);
			if ((null!=jobId)&&(false!=mCheckUserAuthorization))
			{
				log("auth jobId is " + jobId);
				Authorization.userHasAccessException(this, request, jobId);
			}
			if ((null!=jobId)&&(null!=mErrorFilename))
			{
				File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
				log("fileLocation is " + jobDir.getAbsolutePath());
				mErrorFile = new File(jobDir, mErrorFilename);
				if (mErrorFile.exists())
				{
					mErrorFile.delete();
				}
			}
			StringBuffer sb = new StringBuffer();
			internalProcess(request, sb);
			try (PrintWriter out = response.getWriter())
			{
				response.setContentType(mReturnType);
				response.setStatus(200);
				out.append(sb.toString());
			}
		}
		catch (Exception exp)
		{
			log("BEIServletMixin::processRequest failed", exp);
			response.setContentType("text;charset=UTF-8");
			response.setStatus(500);
			response.sendError(500, exp.getMessage());
			if (null!=mErrorFile)
			{
				Files.write(mErrorFile.toPath(), exp.getMessage().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			}
		}
	}
	
	protected void writeToError(String theMessage) throws IOException
	{
		if (null!=mErrorFile)
		{
			Files.write(mErrorFile.toPath(), theMessage.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
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
