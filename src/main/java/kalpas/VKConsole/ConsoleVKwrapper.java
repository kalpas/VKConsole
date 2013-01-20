package kalpas.VKConsole;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * VKConsole
 * 
 */
public class ConsoleVKwrapper {
    private static Logger            logger    = LogManager.getLogger(ConsoleVKwrapper.class.getName());
    private static HelpFormatter     formatter = new HelpFormatter();
    private static CommandLineParser parser    = new PosixParser();
    private static Options           options   = createOptions();

    public static void main(String[] args) {
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
            
        } catch (ParseException | IllegalArgumentException e) {
            formatter.printHelp("VK", options);
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        Option graph = new Option("G", "graph", false, "generate connections graph");
        Option user = new Option("u", "user", true, "user id");
        Option group = new Option("g", "group", true, "group id");
        options.addOption(group);
        options.addOption(user);
        options.addOption(graph);
        return options;
    }
}
