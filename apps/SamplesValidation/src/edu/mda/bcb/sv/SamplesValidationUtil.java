/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.mda.bcb.sv;

import edu.mda.bcb.matrix.Header;
import edu.mda.bcb.matrix.Matrix;
import java.util.ArrayList;

/**
 *
 * @author cjacoby
 */
public class SamplesValidationUtil {
    
    public static void createMissingBatchEntries(Matrix batches, ArrayList<Header> samples) {
        for (Header h : samples) {
            if (!batches.hasRow(h.label)) {
                batches.addRow(h.label);
            }
        }
    }
    
    public static boolean containsSamples(Matrix m, ArrayList<Header> samples) throws SampleValidatorException {
        Header sample;
        for (int i = 0; i < samples.size(); i++) {
            sample = samples.get(i);
            if (!m.hasRow(sample.label)) {
                return false;
            }
        }
        return true;
    }
    
}
