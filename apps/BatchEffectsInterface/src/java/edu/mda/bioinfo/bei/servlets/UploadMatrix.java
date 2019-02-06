/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets;

import edu.mda.bcb.matrix.Matrix;
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
import java.nio.file.StandardCopyOption;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

/**
 *
 * @author linux
 */
@MultipartConfig
public class UploadMatrix extends BEIServletMixin
{

	public UploadMatrix()
	{
		super("application/text;charset=UTF-8", true, UploadMatrix.class);
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
		boolean keepOriginalFlag = paramStringToBool(request.getParameter("keepOriginal"));
		// No rectangle flag should be logically inverted
		boolean noRectangleFlag = !paramStringToBool(request.getParameter("noRectangle"));
		boolean sortRowsFlag = paramStringToBool(request.getParameter("sortRows"));
		boolean sortColsFlag = paramStringToBool(request.getParameter("sortCols"));
		String isAlternate = request.getParameter("isAlternate");
		boolean isAlternateP = request.getParameter("isAlternate").equals("YES");
		File jobDir = new File(BEISTDDatasets.M_OUTPUT, jobId);
		String uploadDir = jobDir.getAbsolutePath();
		new File(uploadDir).mkdirs();
		final String savePath = new File(uploadDir, (isAlternateP ? "matrix_data2.tsv" : "matrix_data.tsv")).getAbsolutePath();
		log("passed in jobId is " + jobId);
		log("passed in isAlternate is " + isAlternate);
		log("fileLocation is " + uploadDir);
		/**
		 * message will be the ultimate response text. A message equal to the
		 * jobId is interpreted as successful file upload, and anything else is
		 * interpreted as failure (with failure message).
		 */
		String message = jobId;
		try
		{
			// used in debugging button disable/enable
			//log("sleep 30 seconds");
			//try
			//{
			//	Thread.sleep(1000*30);
			//}
			//catch(Exception ignore)
			//{
			//	log("woke up");
			//}
			String result1 = null;
			String result2 = null;
			result1 = saveFileUpload(request.getPart("file"), savePath, this);
			if (result1.equals(successfulUpload))
			{
				result2 = matrixPostProcessing(savePath, keepOriginalFlag, noRectangleFlag, sortRowsFlag, sortColsFlag);
			}
			if (successfulUpload.equals(result1) && successfulPostProcessing.equals(result2))
			{
				if (isAlternateP)
				{
					JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_SECONDARY_USER_MATRIX, request, this);
				}
				else
				{
					JobStatus.setJobStatus(jobId, JOB_STATUS.NEWJOB_PRIMARY_USER_MATRIX, request, this);
				}
			}
			else
			{
				message = "";
				if (!result1.equals(successfulUpload))
				{
					message += "File Upload Failure: " + result1 + ". ";
				}
				if (!result2.equals(successfulPostProcessing))
				{
					message += "File Post-Processing Failure: " + result2;
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

	// Utility to interpret "false" and "true" flag argument values from request parameters.
	public static boolean paramStringToBool(String param)
	{
		return param.equals("true");
	}

	public static String matrixPostProcessing(String matrixPath, boolean keepOriginalFlag, boolean noRectangleFlag, boolean sortRowsFlag, boolean sortColsFlag) throws Exception
	{
		try
		{
			Matrix matrix = new Matrix.Builder(matrixPath)
					.allowNonRectangle(noRectangleFlag)
					.build();
			boolean changed = false;
			if (sortRowsFlag)
			{
				matrix.sortRows();
				changed = true;
			}
			if (sortColsFlag)
			{
				matrix.sortColumns();
				changed = true;
			}
			if (changed)
			{
				File matrixFile = new File(matrixPath);
				String baseDir = matrixFile.getParent();
				String tempMatrixPath = new File(baseDir, "matrix_data_temp.tsv").getAbsolutePath();
				matrix.write(tempMatrixPath);
				if (keepOriginalFlag)
				{
					File originalFile = new File(matrixPath.replace(".tsv", "_original.tsv"));
					matrixFile.renameTo(originalFile);
				}
				Files.copy(Paths.get(tempMatrixPath), Paths.get(matrixPath), StandardCopyOption.REPLACE_EXISTING);
				new File(tempMatrixPath).delete();
			}
			return successfulPostProcessing;
		}
		catch (Exception e)
		{
			return e.getMessage();
		}
	}

	public static String getFileName(final Part part, HttpServlet theServlet)
	{
		final String partHeader = part.getHeader("content-disposition");
		theServlet.log("Part Header = " + partHeader);
		for (String content : part.getHeader("content-disposition").split(";"))
		{
			if (content.trim().startsWith("filename"))
			{
				return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
			}
		}
		return null;
	}
}
