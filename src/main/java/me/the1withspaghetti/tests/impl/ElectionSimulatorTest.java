package me.the1withspaghetti.tests.impl;

import me.the1withspaghetti.compiler.CompiledCode;
import me.the1withspaghetti.panals.ExpandableTest;
import me.the1withspaghetti.panals.TestPanel;
import me.the1withspaghetti.tests.AbstractTest;
import me.the1withspaghetti.util.ConsoleReader;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.the1withspaghetti.tests.Assertions.*;

public class ElectionSimulatorTest implements AbstractTest {

    private static final String couldNotFindMessage = "Could not find %CONSTANT%, make sure the class constants are named exactly as they are in the spec and are correctly formatted!";

    @Override
    public String getTestName() {
        return "#1: Election Simulator";
    }

    @Override
    public void run(TestPanel panel, File code) {
        ExpandableTest test1 = panel.getNewTest("Test 1: 5 sims, 10 districts, 52% poll average, 7% error");
        ExpandableTest test2 = panel.getNewTest("Test 2: 5 sims, 10 districts, 52% poll average, 7% error");
        ExpandableTest test3 = panel.getNewTest("Test 3: 5 sims, 10 districts, 52% poll average, 7% error");
        ExpandableTest test4 = panel.getNewTest("Test 4: 5 sims, 12 districts, 48% poll average, 15% error");
        ExpandableTest test5 = panel.getNewTest("Test 5: 15 sims, 3 districts, 72% poll average, 1% error");
        ExpandableTest test6 = panel.getNewTest("Test 6: 7 sims, 1 district, 48% poll average, 73% error");

        test1.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 5, 10, 0.52, 0.07, 1245634575);
        });

        test2.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 5, 10, 0.52, 0.07, 572638965);
        });

        test3.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 5, 10, 0.52, 0.07, 845728635);
        });

        test4.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 5, 12, 0.48, 0.15, 245687634);
        });

        test5.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 15, 3, 0.72, 0.01, 902835749);
        });

        test6.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 7, 1, 0.48, 0.03, 258674524);
        });
    }

    private void runTest(
            File code,
            PrintStream out,
            PrintStream inout,
            PrintStream err,
            int NUM_SIMS,
            int NUM_DISTS,
            double POLL_AVG,
            double POLL_ERR,
            long seed) throws Exception {

        String source = Files.readString(Path.of(code.toURI()));

        source = forcedReplaceText(source,"int NUM_SIMS *= *\\d+ *;", "int NUM_SIMS = "+NUM_SIMS+";",
                couldNotFindMessage.replaceAll("%CONSTANT%", "NUM_SIMS"));
        source = forcedReplaceText(source, "int NUM_DISTS *= *\\d+ *;", "int NUM_DISTS = "+NUM_DISTS+";",
                couldNotFindMessage.replaceAll("%CONSTANT%", "NUM_DISTS"));
        source = forcedReplaceText(source,"double POLL_AVG *= *[\\d.]+ *;", "double POLL_AVG = "+POLL_AVG+";",
                couldNotFindMessage.replaceAll("%CONSTANT%", "POLL_AVG"));
        source = forcedReplaceText(source,"double POLL_ERR *= *[\\d.]+ *;", "double POLL_ERR = "+POLL_ERR+";",
                couldNotFindMessage.replaceAll("%CONSTANT%", "POLL_ERR"));
        source = forcedReplaceText(source,"new Random *(.*) *;", "new Random("+seed+");",
                "Could not locate your random class, make sure you have only 1 instance and you initialize it with \"new Random();\"");

        source = source.replaceAll("(public)? +class +(\\w+) +\\{", "public class ElectionSimulator {");
        source = source.replaceAll("package +[\\w.]+ *;", "");

        try (CompiledCode compiledCode = new CompiledCode("ElectionSimulator", source, out, inout, err)) {

            compiledCode.runCode();

            ConsoleReader consoleOut = compiledCode.getProcessOutput();
            OutputStream consoleIn = compiledCode.getProcessInput();

            // Start program testing

            final Random rand = new Random(seed);

            assertEquals("Welcome to the Election Simulator!", consoleOut.nextLine(3000));
            assertEquals("Running "+NUM_SIMS+" simulations of "+NUM_DISTS+" districts.", consoleOut.nextLine(100));

            final Pattern POLLING_PATTERN = Pattern.compile("Our candidate is polling at (\\d+.?\\d*)% with a (\\d+.?\\d*)% margin of error.");
            String pollingLine = consoleOut.nextLine(100);
            assertNotNull(pollingLine, "Reached end of console output when still expecting more out.");
            Matcher m1 = POLLING_PATTERN.matcher(pollingLine);
            assertTrue(m1.find(), "Could not parse line \""+pollingLine+"\" \n*Make sure it is properly formatted according to the spec!*");
            String msg1 = "Testing line \""+pollingLine+"\"";
            assertEquals(POLL_AVG*100, Double.parseDouble(m1.group(1)), 0.1, msg1);
            assertEquals(POLL_ERR*100, Double.parseDouble(m1.group(2)), 0.1, msg1);

            assertEquals("", consoleOut.nextLine(100), "A blank line is required before simulation results.");

            double totalAverage = 0;
            for (int s = 1; s <= NUM_SIMS; s++) {
                assertEquals("Running simulation #"+s+":", consoleOut.nextLine(100));

                int positiveVotes = 0;
                int totalVotes = 0;
                // For each district
                for (int i = 0; i < NUM_DISTS; i++) {
                    int voters = rand.nextInt(1000) + 1;
                    double voteError = rand.nextGaussian() * 0.5 * POLL_ERR;
                    positiveVotes += (int) (voters * (POLL_AVG + voteError));
                    totalVotes += voters;
                }
                double percent = (double)positiveVotes/totalVotes * 100;
                totalAverage += percent;

                assertEquals("  Win? " + (percent > 50.0 ), consoleOut.nextLine(100), "Incorrect result during simulation #"+s);

                final Pattern RESULTS_PATTERN = Pattern.compile("^  Results: (\\d+) \\((\\d+.\\d+)%\\) - (\\d+) \\((\\d+.\\d+)%\\)$");
                String resultsLine = consoleOut.nextLine(100);
                assertNotNull(resultsLine, "Reached end of console output when still expecting more simulations.");
                Matcher m2 = RESULTS_PATTERN.matcher(resultsLine);

                assertTrue(m2.find(), "Could not parse line \""+resultsLine+"\" \n*Make sure it is properly formatted according to the spec!*");

                int finalPositiveVotes = positiveVotes;
                int finalTotalVotes = totalVotes;
                String msg2 = "Testing line \""+resultsLine+"\" in simulation "+s+".\n" +
                                "IF the random number is different than expected, make sure you have done your math right and you always calculate the people in a district BEFORE you calculate the percent who voted for your candidate. \n" +
                                "Also make sure you are generating a random number of people from 1 - 1000, refer back to the codestepbystep exit ticket for how to do this.";
                assertEquals(finalPositiveVotes, Integer.parseInt(m2.group(1)), msg2);
                assertEquals(percent, Double.parseDouble(m2.group(2)), 0.01, msg2);
                assertEquals(finalTotalVotes - finalPositiveVotes, Integer.parseInt(m2.group(3)), msg2);
                assertEquals(100-percent, Double.parseDouble(m2.group(4)), 0.01, msg2);
                //assertEquals("  Results: "+positiveVotes+" ("+percent+"%) - "+(totalVotes - positiveVotes)+" ("+(100 - percent)+"%)", consoleOut.nextLine(100));

                String plus = CharBuffer.allocate(positiveVotes / 100).toString().replace( '\0', '+');
                String minus = CharBuffer.allocate((totalVotes - positiveVotes) / 100).toString().replace( '\0', '-');

                assertEquals("  Visualization: "+plus, consoleOut.nextLine(100), "Error on simulation "+s);
                assertEquals("                 "+minus, consoleOut.nextLine(100), "Error on simulation "+s);
            }

            assertEquals("", consoleOut.nextLine(100), "A blank line is required before your final average");

            final Pattern AVERAGE_PATTERN = Pattern.compile("^Average vote percentage: (\\d+.\\d+)%$");
            String averageLine = consoleOut.nextLine(100);
            assertNotNull(averageLine, "Reached end of console output when still expecting a total average!");
            Matcher m3 = AVERAGE_PATTERN.matcher(averageLine);

            assertTrue(m3.find(), "Could not parse line \""+averageLine+"\" \n*Make sure it is properly formatted according to the spec!*");
            double finalTotalAverage = totalAverage;
            String msg3 = "Testing line \""+averageLine+"\" at the end of the program\n" +
                            "IF the random number is different than expected, make sure you have done your math right and you always calculate the people in a district BEFORE you calculate the percent who voted for your candidate. \n" +
                            "Also make sure you are generating a random number of people from 1 - 1000, refer back to the codestepbystep exit ticket for how to do this.";
            assertEquals(finalTotalAverage / NUM_SIMS, Double.parseDouble(m3.group(1)), 0.01, msg3);
        }
    }

    private String forcedReplaceText(String source, String regex, String replacement, String msg) {
        Matcher m = Pattern.compile(regex).matcher(source);
        assertTrue(m.find(), msg);
        return m.replaceAll(replacement);
    }
}
