/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bioinfo.bei.processes;

import java.util.TreeMap;

/**
 *
 * @author linux
 */
public class BatchdataObj
{
	public String mBatchType;
	public TreeMap<String, Integer> mBatches;
	
	public BatchdataObj(String theBatchType)
	{
		mBatchType = theBatchType;
		mBatches = new TreeMap<>();
	}
}
