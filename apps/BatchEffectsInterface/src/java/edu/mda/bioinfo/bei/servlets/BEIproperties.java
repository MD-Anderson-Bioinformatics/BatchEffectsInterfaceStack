/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author linux
 */
@WebServlet(loadOnStartup=1)
public class BEIproperties extends BEIServletMixin
{
	static private String M_PROPERTIES_JSON = null;
	static private Properties M_PROPERTIES = null;
	static private long M_TIMESTAMP = 0;
	
	static public String getProperty(String theProperty, HttpServlet theServlet) throws IOException
	{
		if (null==M_PROPERTIES)
		{
			getResponseString(theServlet);
		}
		return M_PROPERTIES.getProperty(theProperty);
	}
	
	static public boolean isLoginAllowed(HttpServlet theServlet) throws IOException
	{
		if (null==M_PROPERTIES)
		{
			getResponseString(theServlet);
		}
		return "true".equals(M_PROPERTIES.getProperty("allowLogin"));
	}

	public BEIproperties()
	{
		super("application/json;charset=UTF-8", true, null);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		long start = System.currentTimeMillis();
		theBuffer.append(getResponseString(this));
		long finish = System.currentTimeMillis();
		//log("BEIproperties completed in " + (finish-start)/1000 + " seconds");
	}

	synchronized public static String getResponseString(HttpServlet theServlet) throws IOException
	{
		//theServlet.log("BEIproperties::getResponseString");
		//theServlet.log("BEIproperties::getResponseString M_TIMESTAMP="+M_TIMESTAMP);
		//theServlet.log("BEIproperties::getResponseString M_TIMESTAMP="+System.currentTimeMillis());
		// TODO: for the timeout case, put that into a separate thread, perha[s using a listener instead of servlet load
		String result = M_PROPERTIES_JSON;
		if ((null==M_PROPERTIES_JSON)||
			((null!=M_PROPERTIES_JSON)&&((M_TIMESTAMP-System.currentTimeMillis())>(1000*60*60))))
		{
			M_PROPERTIES = new Properties();
			try (FileInputStream is = new FileInputStream(new File(BEISTDDatasets.M_PROPS, "bei.properties")))
			{
				M_PROPERTIES.loadFromXML(is);
			}
			StringWriter out = new StringWriter();
			out.append("{");
			boolean wrote = false;
			for (String name : M_PROPERTIES.stringPropertyNames())
			{
				if (true==wrote)
				{
					out.append(",");
				}
				out.append("\n\"" + name + "\":\"" + M_PROPERTIES.getProperty(name) + "\"");
				wrote = true;
			}
			out.append("\n}\n");
			M_PROPERTIES_JSON = out.toString();
			M_TIMESTAMP = System.currentTimeMillis();
			result = M_PROPERTIES_JSON;
		}
		return result;
	}
}
