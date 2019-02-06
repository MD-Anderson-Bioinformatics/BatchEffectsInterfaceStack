/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets;

import edu.mda.bioinfo.bei.status.JOB_STATUS;
import edu.mda.bioinfo.bei.status.JobStatus;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author linux
 */
@MultipartConfig
public class UploadManifest extends BEIServletMixin
{

	public UploadManifest()
	{
		super("application/text;charset=UTF-8", true, UploadManifest.class);
	}

	/**
	 * Message returned from saveFileUpload to indicate success. All other
	 * values are interpreted as failure, likely providing an explanation as to
	 * the failure.
	 */
	private static final String successfulUpload = "FILE UPLOAD: SUCCESS";

	/**
	 * Message returned from matrixPostProcessing to indicate success. All other
	 * values are interpreted as failure, likely providing an explanation as to
	 * the failure.
	 */
	private static final String successfulPostProcessing = "POST PROCESS: SUCCESS";

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		String jobId = request.getParameter("jobId");
		String manifestType = request.getParameter("manifestType");
		String isAlternate = request.getParameter("isAlternate");
		log("passed in jobId is " + jobId);
		log("passed in manifestType is " + manifestType);
		log("isAlternate is " + isAlternate);
		boolean isAlternateP = isAlternate.equals("YES");
		File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
		String uploadDir = jobDir.getAbsolutePath();
		new File(uploadDir).mkdirs();
		String releaseVer = "current";
		if (!manifestType.startsWith("Current"))
		{
			releaseVer = "legacy";
		}
		String dataType = "";
		if (manifestType.endsWith("biospeci"))
		{
			dataType = "biospecimen-xml";
		}
		else if (manifestType.endsWith("clinical"))
		{
			dataType = "clinical-xml";
		}
		else if (manifestType.endsWith("methy450"))
		{
			dataType = "methylation450-txt";
		}
		else if (manifestType.endsWith("miRNAiso"))
		{
			dataType = "miRNA-isoform-txt";
		}
		else if (manifestType.endsWith("miRNAtxt"))
		{
			dataType = "miRNA-txt";
		}
		else if (manifestType.endsWith("MuSE_Maf"))
		{
			dataType = "MuSE-maf-gz";
		}
		else if (manifestType.endsWith("MuTe2Maf"))
		{
			dataType = "MuTect2-maf-gz";
		}
		else if (manifestType.endsWith("RNASeqHT"))
		{
			dataType = "RNASeq-HTSeq-counts-gz";
		}
		else if (manifestType.endsWith("SNP6mask"))
		{
			dataType = "SNP6-masked-txt";
		}
		else if (manifestType.endsWith("SniprMaf"))
		{
			dataType = "SomaticSniper-maf-gz";
		}
		else if (manifestType.endsWith("VarScMaf"))
		{
			dataType = "VarScan2-maf-gz";
		}
		String altStr = "PRI";
		if (isAlternateP)
		{
			altStr = "SEC";
		}
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
		String timestamp = dateFormat.format(calendar.getTime());
		File manifestDir = new File(new File(uploadDir, altStr + "-" + releaseVer), "manifests");
		manifestDir.mkdirs();
		log("passed in jobId is " + jobId);
		log("passed in dataType is " + dataType);
		log("manifestDir is " + manifestDir);
		//
		final String tempManifest = new File(manifestDir, "tempManifest.txt").getAbsolutePath();
		final String tempBiospecimen = new File(manifestDir, "tempBiospecimen.txt").getAbsolutePath();
		/**
		 * message will be the ultimate response text. A message equal to the
		 * jobId is interpreted as successful file upload, and anything else is
		 * interpreted as failure (with failure message).
		 */
		String message = jobId;
		try
		{
			String result1 = null;
			String result2 = null;
			result1 = saveFileUpload(request.getPart("manifest"), tempManifest, this);
			result2 = saveFileUpload(request.getPart("biospecimen"), tempBiospecimen, this);
			// remove one for header row
			long manifestLen = Files.lines(Paths.get(tempManifest)).count()-1;
			long bioLen = Files.lines(Paths.get(tempBiospecimen)).count()-1;
			final String savePathManifest = new File(manifestDir, dataType + "." + timestamp + "." + manifestLen + ".txt").getAbsolutePath();
			final String savePathBiospecimen = new File(manifestDir, "biospecimen-xml." + timestamp + "." + bioLen + ".txt").getAbsolutePath();
			Files.move(Paths.get(tempManifest), Paths.get(savePathManifest));
			Files.move(Paths.get(tempBiospecimen), Paths.get(savePathBiospecimen));
			if ((result1.equals(successfulUpload))&&(result2.equals(successfulUpload)))
			{
				if (isAlternateP)
				{
					JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_GDC_MANIFEST, request, this);
				}
				else
				{
					JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_GDC_MANIFEST, request, this);
				}
			}
			else
			{
				message = "";
				if (!result1.equals(successfulUpload))
				{
					message += "File Upload Failure: " + result1 + ". ";
				}
			}
		}
		catch (IOException | ServletException exp)
		{
			message = exp.getMessage();
			log("Problem in file upload for job " + jobId + ". Error: " + exp.getMessage(), exp);
			throw new Exception("Problem in file upload for job " + jobId + ". Error: " + exp.getMessage(), exp);
		}
		theBuffer.append(message);
	}

	// TODO: Evaluate whether its possible/wise to call the saveFileUpload in UploadBatch/UploadMatrix rather than have this method defined here
	public static String saveFileUpload(Part thePart, String theSavePath, HttpServlet theServlet) throws Exception
	{
		String message = "";
		// TODO: replace with automatic try open/close
		OutputStream out = null;
		InputStream filecontent = null;
		try
		{
			theServlet.log("name is =" + thePart.getName());
			theServlet.log("size is =" + thePart.getSize());
			theServlet.log("subname is =" + thePart.getSubmittedFileName());
			if (null == thePart.getSubmittedFileName())
			{
				theServlet.log("File not provided");
				message = "File not provided";
			}
			else
			{
				//long size = 0;
				//String machineName = InetAddress.getLocalHost().getHostName();
				//final String fileName = getFileName(thePart, theServlet);
				out = new FileOutputStream(theSavePath);
				filecontent = thePart.getInputStream();

				int read = 0;
				final byte[] bytes = new byte[1024];

				while ((read = filecontent.read(bytes)) != -1)
				{
					//size = size + 1024;
					out.write(bytes, 0, read);
				}
				theServlet.log("File being uploaded to " + theSavePath);
				message = successfulUpload;
			}
		}
		catch (FileNotFoundException exp)
		{
			message = "You either did not specify a file to upload or are trying to upload a file to a protected or nonexistent location. "
					+ exp.getMessage();
			theServlet.log("Problems during file upload. Error: " + exp.getMessage(), exp);
			throw new Exception(message, exp);
		}
		catch (Exception exp)
		{
			message = exp.getMessage();
			theServlet.log("Problems during file upload. Error: " + exp.getMessage(), exp);
			throw new Exception(message, exp);
		}
		finally
		{
			if (out != null)
			{
				try
				{
					out.close();
				}
				catch (Exception ignore)
				{
					//
				}
			}
			if (filecontent != null)
			{
				try
				{
					filecontent.close();
				}
				catch (Exception ignore)
				{
					//
				}
			}
		}
		return message;
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
