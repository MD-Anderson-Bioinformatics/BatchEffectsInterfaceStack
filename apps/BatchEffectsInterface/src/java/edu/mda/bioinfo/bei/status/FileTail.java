/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.status;

//import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author linux
 */
public class FileTail
{
/*
	static public String[] tailLinux(String theFile, int theLines) throws FileNotFoundException, IOException, InterruptedException
	{
		ArrayList<String> lines = new ArrayList<>();
		ArrayList<String> executeStrings = new ArrayList<>();
		executeStrings.add("tail");
		executeStrings.add("-" + theLines);
		executeStrings.add(theFile);
		ProcessBuilder pb = new ProcessBuilder(executeStrings);
		pb.redirectErrorStream(true);
		// start proceess from pb
		Process process = pb.start();
		;
		String line;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream())))
		{
			process.waitFor();
			while ((line = br.readLine()) != null)
			{
				lines.add(line);
			}
		}
		return lines.toArray(new String[0]);
	}
*/
	static public String[] tail(String theFile, int theLines) throws FileNotFoundException, IOException, InterruptedException
	{
		// cleaner implementation for our purposes of http://stackoverflow.com/questions/686231/quickly-read-the-last-line-of-a-text-theFile
		ArrayList<String> lines = new ArrayList<>();
		try(java.io.RandomAccessFile fileHandler = new java.io.RandomAccessFile(theFile, "r"))
		{
			long fileLength = fileHandler.length() - 1;
			StringBuilder sb = new StringBuilder();
			int line = 0;
			for (long filePointer = fileLength; filePointer != -1; filePointer--)
			{
				fileHandler.seek(filePointer);
				int readByte = fileHandler.readByte();
				if (readByte == 0xA)
				{
					lines.add(0, StringEscapeUtils.escapeJson(sb.reverse().toString()));
					sb.setLength(0);
					if (filePointer < fileLength)
					{
						line = line + 1;
					}
				}
				else if (readByte == 0xD)
				{
					lines.add(0, StringEscapeUtils.escapeJson(sb.reverse().toString()));
					sb.setLength(0);
					if (filePointer < fileLength - 1)
					{
						line = line + 1;
					}
				}
				if (line >= theLines)
				{
					break;
				}
				if ((readByte != 0xA)&&(readByte != 0xD))
				{
					sb.append((char) readByte);
				}
			}
			while(true==lines.remove(""))
			{
				// keep removing
			}
			return lines.toArray(new String[0]);
		}
	}
}