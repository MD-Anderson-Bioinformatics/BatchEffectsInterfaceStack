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

package edu.mda.bcb.bei.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.mda.bcb.gdc.api.SingleDataset;
import java.io.IOException;
import java.util.TreeSet;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Tod-Casasent
 */
@WebServlet(name = "GDCdatasets", urlPatterns =
{
	"/GDCdatasets"
}, loadOnStartup=1)
public class GDCdatasets extends BEIServletMixin
{
	static private TreeSet<String> M_DATASETS = null;
	static private long M_TIMESTAMP = 0;
	
	public GDCdatasets()
	{
		super("application/json;charset=UTF-8", false, null);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		long start = System.currentTimeMillis();
		theBuffer.append(getResponseString(this));
		long finish = System.currentTimeMillis();
		//log("BEIproperties completed in " + (finish-start)/1000 + " seconds");
	}

	synchronized public static String getResponseString(HttpServlet theServlet) throws IOException, Exception
	{
		// TODO: for the timeout case, put that into a separate thread, perha[s using a listener instead of servlet load
		theServlet.log("GDCdatasets getResponseString");
		if ((null==M_DATASETS)||
			((null!=M_DATASETS)&&((M_TIMESTAMP-System.currentTimeMillis())>(1000*60*60*24))))
		{
			theServlet.log("GDCdatasets call GDC for list");
			SingleDataset sd = new SingleDataset();
			M_DATASETS = sd.getDatasets();
			M_TIMESTAMP = System.currentTimeMillis();
		}
		theServlet.log("GDCdatasets build JSON");
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();
		String result = gson.toJson(M_DATASETS);
		return result;
	}
}
