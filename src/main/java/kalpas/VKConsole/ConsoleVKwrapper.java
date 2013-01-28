package kalpas.VKConsole;

import java.util.Map;

import kalpas.VKCore.VKModule;
import kalpas.VKCore.simple.DO.User;
import kalpas.VKCore.simple.helper.GMLHelper;
import kalpas.VKCore.simple.helper.HttpClientContainer;
import kalpas.VKCore.stats.GroupStats;
import kalpas.VKCore.stats.WallStats;
import kalpas.VKCore.stats.DO.EdgeProperties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import com.google.common.collect.Multimap;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * VKConsole
 * 
 */
@SuppressWarnings("static-access")
public class ConsoleVKwrapper {
    private static Logger            logger    = LogManager.getLogger(ConsoleVKwrapper.class.getName());

    private static HelpFormatter     formatter = new HelpFormatter();
    private static CommandLineParser parser    = new PosixParser();

    private static Options           options   = null;

    // Options
    private static Option            graph     = null;
    private static Option            user      = null;
    private static Option            group     = null;
    private static Option            file      = null;
    private static Option            dynamics  = null;

    private static Injector          injector  = Guice.createInjector(new VKModule());

    public static void main(String[] args) {
        options = createOptions();
        CommandLine line = null;

        try {
            line = parser.parse(options, args);
            if (line.getOptions().length == 0) {
                throw new IllegalArgumentException("no options passed. pls see usage");
            }

            if (line.hasOption(graph.getOpt())) {
                createGraph(line);
            } else if (line.hasOption(dynamics.getOpt())) {
                getDynamics(line);
            } else {
                logger.info("no options was passed to select activity\ngraph would be created");
                createGraph(line);
            }

        } catch (ParseException | IllegalArgumentException e) {
            logger.error(e.getMessage());
            formatter.printHelp("VK", options);
        }
    }

    private static void createGraph(CommandLine line) {
        if (line.getOptionValue(group.getOpt()) != null) {

            String gid = line.getOptionValue(group.getOpt());
            String fileName = line.getOptionValue(file.getOpt());
            if (fileName == null) {
                fileName = "club" + gid + "timestamp" + new DateTime().getMillis();
                logger.warn("file name wasn't specified. generating default - {}", fileName);
            }

            GroupStats stats = injector.getInstance(GroupStats.class);
            Multimap<User, User> multimap = stats.getMemberNetwork(gid);
            GMLHelper.writeToFile(fileName, multimap);
            injector.getInstance(HttpClientContainer.class).shutdown();

        } else if (line.getOptionValues(user.getOpt()) != null) {
            throw new UnsupportedOperationException("getting user graph is not implemented yet");
        } else {
            throw new IllegalArgumentException("neither group id nor user id were specified");
        }
    }
    
    private static void getDynamics(CommandLine line) {
        if (line.getOptionValue(group.getOpt()) != null) {

            String gid = line.getOptionValue(group.getOpt());
            String fileName = line.getOptionValue(file.getOpt());
            if (fileName == null) {
                fileName = "club" + gid + "timestamp" + new DateTime().getMillis();
                logger.warn("file name wasn't specified. generating default - {}", fileName);
            }

            WallStats stats = injector.getInstance(WallStats.class);

            // TODO remove count
            Multimap<User, Map.Entry<EdgeProperties, User>> multimap = stats.getRepostsNet(gid);
            GMLHelper.writeToFileM("reposts" + fileName, multimap);

            // TODO remove count
            multimap = stats.getInteractions(gid, null);
            GMLHelper.writeToFileM("interactions" + fileName, multimap);

            stats.saveDynamics(gid, null);

            injector.getInstance(HttpClientContainer.class).shutdown();

        } else if (line.getOptionValues(user.getOpt()) != null) {
            throw new UnsupportedOperationException("getting user graph is not implemented yet");
        } else {
            throw new IllegalArgumentException("neither group id nor user id were specified");
        }
    }

    private static Options createOptions() {
        Options options = new Options();

        graph = new Option("G", "graph", false, "generate connections graph");
        dynamics = new Option("D", "dynamics", false, "generate dynamics");
        user = OptionBuilder.withArgName("uid").hasArg().withDescription("user id").withLongOpt("user").create("u");
        group = OptionBuilder.withArgName("gid").hasArg().withDescription("group id").withLongOpt("group").create("g");
        file = OptionBuilder.withArgName("file").hasArg().withDescription("output file").withLongOpt("file")
                .create("f");

        options.addOption(group);
        options.addOption(user);
        options.addOption(graph);
        options.addOption(file);
        options.addOption(dynamics);
        return options;
    }
}
