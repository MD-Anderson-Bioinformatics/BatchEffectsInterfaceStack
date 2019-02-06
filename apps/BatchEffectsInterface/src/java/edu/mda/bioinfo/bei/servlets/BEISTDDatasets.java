/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.servlets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 *
 * @author linux
 */
public class BEISTDDatasets extends BEIServletMixin
{
	// TODO: review putting these paths here?
	final public static String M_INDICES = "/BEI/INDICES";
	final public static String M_ARCHIVES = "/BEI/ARCHIVES";
	final public static String M_OUTPUT = "/BEI/OUTPUT";
	final public static String M_PROPS = "/BEI/PROPS";
	final public static String M_GENOMICS_FILETO = "/BEI/GENOMICS/FILETO";
	final public static String M_GENOMICS_MAPS = "/BEI/GENOMICS/MAPS";
	final public static String M_WEBSITE = "/BEI/WEBSITE";
	public static String M_DATASET_JSON = null;
	public static long M_TIMESTAMP = 0;
	public static final String M_VERSION = "2019-01-28-1525";

	public BEISTDDatasets()
	{
		super("application/json;charset=UTF-8", true, null);
	}

	@Override
	protected void internalProcess(HttpServletRequest request, StringBuffer theBuffer) throws Exception
	{
		log("BEISTDDatasets call find datasets");
		log("BEISTDDatasets::processRequest M_TIMESTAMP=" + M_TIMESTAMP);
		log("BEISTDDatasets::processRequest time=" + System.currentTimeMillis());
		if ((null == M_DATASET_JSON)
				|| ((null != M_DATASET_JSON) && ((M_TIMESTAMP - System.currentTimeMillis()) > (1000 * 60 * 60))))
		{
			log("find M_INDICES:" + M_INDICES);
			TreeSet<String> indexList = findFiles(M_INDICES, "*.tsv");
			log("found length:" + indexList.size());
			// datasource -> convert date -> program/disease type -> projects/data type -> workflow/platform -> data type/level
			TreeMap<String, TreeMap<String, TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<String>>>>>> dataRelations = new TreeMap<>();
			//
			for (String indexFile : indexList)
			{
				File myfile = new File(indexFile);
				String filename = myfile.getName();
				filename = filename.replace(".tsv", "");
				String[] splitName = filename.split("-", -1);
				String datasource = splitName[0];
				try (BufferedReader br = java.nio.file.Files.newBufferedReader(Paths.get(myfile.getAbsolutePath()), Charset.availableCharsets().get("UTF-8")))
				{
					// header line
					String line = br.readLine();
					line = br.readLine();
					while (line != null)
					{
						String[] splitted = line.split("\t", -1);
						String date = splitted[0];
						String programDiseasetype = splitted[1];
						String projectDatatype = splitted[2];
						String workflowPlatform = splitted[3];
						String datatypeLevel = splitted[4];
						TreeMap<String, TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<String>>>>> sourceTo = dataRelations.get(datasource);
						if (null == sourceTo)
						{
							sourceTo = new TreeMap<>(Collections.reverseOrder());
						}
						TreeMap<String, TreeMap<String, TreeMap<String, TreeSet<String>>>> dateTo = sourceTo.get(date);
						if (null == dateTo)
						{
							dateTo = new TreeMap<>();
						}
						TreeMap<String, TreeMap<String, TreeSet<String>>> programTo = dateTo.get(programDiseasetype);
						if (null == programTo)
						{
							programTo = new TreeMap<>();
						}
						TreeMap<String, TreeSet<String>> projectTo = programTo.get(projectDatatype);
						if (null == projectTo)
						{
							projectTo = new TreeMap<>();
						}
						TreeSet<String> workflowTo = projectTo.get(workflowPlatform);
						if (null == workflowTo)
						{
							workflowTo = new TreeSet<>();
						}
						workflowTo.add(datatypeLevel);
						projectTo.put(workflowPlatform, workflowTo);
						programTo.put(projectDatatype, projectTo);
						dateTo.put(programDiseasetype, programTo);
						sourceTo.put(date, dateTo);
						dataRelations.put(datasource, sourceTo);
						//////////////////////
						line = br.readLine();
					}
				}
			}
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();
			M_TIMESTAMP = System.currentTimeMillis();
			M_DATASET_JSON = gson.toJson(dataRelations);
			M_DATASET_JSON = M_DATASET_JSON.replace("]},", "]},\n");
			if (("".equals(M_DATASET_JSON))||("{}".equals(M_DATASET_JSON))||(null==M_DATASET_JSON))
			{
				// TODO: find more graceful way to do this, {} [] {[]} "" and null all cause errors in $.ajax json interpreter
				M_DATASET_JSON = "{\"data\": \"no data\"}";
			}
		}
		theBuffer.append(M_DATASET_JSON);
	}

	public TreeSet<String> findFiles(String theDir, String theFilePattern)
	{
		TreeSet<String> dirnames = new TreeSet<>();
		Collection<File> fileCollection = FileUtils.listFiles(new File(theDir), new WildcardFileFilter(theFilePattern), TrueFileFilter.INSTANCE);
		for (File myfile : fileCollection)
		{
			dirnames.add(myfile.getAbsolutePath());
		}
		return (dirnames);
	}
}
