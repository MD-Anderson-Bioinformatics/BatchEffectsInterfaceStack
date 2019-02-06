package edu.mda.bcb.sv;

import java.util.Collection;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.ParseException;


/**
 *
 * @author cjacoby
 */
public class SampleValidatorParser {
    private final CommandLineParser parser;
    private final Options options;
    
    public SampleValidatorParser() {
        this.parser = new DefaultParser();
        this.options = new Options();
        Option version = Option.builder("v")
                .longOpt("version")
                .desc("Get the version of SampleValidtor.")
                .build();
        this.options.addOption(version);
        Option help = Option.builder("h")
                .longOpt("help")
                .desc("Print the usage and options descriptions")
                .build();
        this.options.addOption(help);
        Option matrix = Option.builder("m")
                .longOpt("matrix")
                .hasArg()
                .desc("The matrix_data file.")
                .required()
                .build();
        this.options.addOption(matrix);
        Option batch = Option.builder("b")
                .longOpt("batch")
                .hasArg()
                .desc("The batches file.")
                .required()
                .build();
        this.options.addOption(batch);
        Option filter = Option.builder("f")
                .longOpt("filter")
                .argName("FILTER-FILE")
                .desc("Filter non-common sample-ids from the matrix file, the batches file, or both.")
                .hasArgs()
                .numberOfArgs(2)
                .optionalArg(true)
                .build();
        this.options.addOption(filter);
        Option create = Option.builder("c")
                .longOpt("create")
                .desc("Create missing batch entries for sample-ids in matrix_data which dont already have entries in the batches file.")
                .build();
        this.options.addOption(create);
    }
    
    public CommandLine parse(String[] args) throws ParseException {
        return this.parser.parse(this.options, args, true);
    }
    
    public Options getOptions() {
        return this.options;
    }
    
}
