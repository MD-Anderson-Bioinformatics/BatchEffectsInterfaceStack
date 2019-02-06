package edu.mda.bcb.sv;

import edu.mda.bcb.matrix.Header;
import edu.mda.bcb.matrix.Matrix;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;

/**
 *
 * @author cjacoby
 */
public class SampleValidator {
    SampleValidatorParser optionsParser;
    public static final String VERSION = "1.0.0";
        
    public SampleValidator() {
        this.optionsParser = new SampleValidatorParser();
    }
    
    public void printHelpAndUsage() {
        this.printHelpAndUsage(null);
    }
    
    public void printHelpAndUsage(String errorMessage) {
        if (errorMessage != null) {
            System.out.println("Error: " + errorMessage);
        }
        HelpFormatter help = new HelpFormatter();
        String header = "";
        String footer = "";
        String title = "SampleValidator";
        help.printHelp(title, header, this.optionsParser.getOptions(), footer, true);
    }
    
    public ArrayList<String> validate(Matrix matrix1, Matrix matrix2) {
        return new ArrayList<>();
    }
    
    public static void main(String args[]) throws Exception {
        SampleValidator validator  = new SampleValidator();
        CommandLine cmd = validator.optionsParser.parse(args);
        try {
            // Check system exiting optional arguments
            if (cmd.hasOption("version")) {
                System.out.println("version: " + SampleValidator.VERSION);
                System.exit(0);
            }
            if (cmd.hasOption("help")) {
                validator.printHelpAndUsage();
                System.exit(0);
            }
            // No system exit, check matrix and batch args
            if (!cmd.hasOption("matrix")) {
                throw new SampleValidatorException("Did not supply matrix data file argument");
            }
            if (!cmd.hasOption("batch")) {
                throw new SampleValidatorException("Did not supply batch file argument");
            }
            // Instantaite Matrix for matrix_data and batches
            String matrixPath = cmd.getOptionValue("matrix");
            String batchPath = cmd.getOptionValue("batch");
            Matrix matrix = new Matrix.Builder(matrixPath).build();
            Matrix batch = new Matrix.Builder(batchPath).build();
            boolean matrixChanged = false;
            boolean batchChanged = false;
            // Do filter if flag passed
            if (cmd.hasOption("filter")) {
                String[] filterFiles = cmd.getOptionValues("filter");
                for (String filterFile : filterFiles) {
                    if (filterFile.equals(matrixPath) || filterFile.equals("matrix")) {
                        Matrix.filterMatrix(matrix, batch.getRowSet(), 1);
                        matrixChanged = true;
                    } else if (filterFile.equals(batchPath)) {
                        Matrix.filterMatrix(batch, matrix.getColumnSet(), 0);
                        batchChanged = true;
                    } else {
                        System.out.println("Recieved filter argument that was neiter the matirx path, batch path, or 'matrix' or 'batch'");
                    }
                }
            }
            // Do create missing batches if flag passed
            if (cmd.hasOption("create")) {
                ArrayList<Header> columns = matrix.getColumns();
                for (int i = 0; i < columns.size(); i++) {
                    Header h = columns.get(i);
                    if (!batch.hasRow(h.label)) {
                        batch.addRow(h.label);
                    }
                }
                batchChanged = true;
            }
            // Write changes, if changes were made
            if (matrixChanged) {
                Matrix.replaceExisting(matrix);
            }
            if (batchChanged) {
                Matrix.replaceExisting(batch);
            }
        } catch (Exception e) {
            validator.printHelpAndUsage(e.getMessage());
        }
    }
}
