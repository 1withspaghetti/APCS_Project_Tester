package me.the1withspaghetti.tests.impl;

import me.the1withspaghetti.compiler.CompiledCode;
import me.the1withspaghetti.panals.ExpandableTest;
import me.the1withspaghetti.panals.TestPanel;
import me.the1withspaghetti.tests.AbstractTest;
import me.the1withspaghetti.tests.TestException;
import me.the1withspaghetti.util.ConsoleReader;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.the1withspaghetti.tests.Assertions.assertEquals;

public class PrioritizingPatientsTest implements AbstractTest {
    @Override
    public String getTestName() {
        return "#2: Prioritizing Patients";
    }

    @Override
    public void run(TestPanel panel, File code) {
        ExpandableTest test1 = panel.getNewTest("Test #1: Jun, Emma, and Johan");
        ExpandableTest test2 = panel.getNewTest("Test #2: Issa with invalid user input");
        ExpandableTest test3 = panel.getNewTest("Test #3: Billy and Sarah with a hospital zip code of 98103");


        test1.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 12345, new Patient[] {
                    new Patient("Jun", 5, 44467, "yes", 2, 99.8),
                    new Patient("Emma", 77, 12487, "y", 10, 101.7),
                    new Patient("Johan", 22, 92107, "no", 5, 104.3),
            });
        });

        test2.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 12345, new Patient[] {
                    new Patient("Issa", 14, new int[] {998, 1, 715498, 66548}, "yes", new int[] {-8, 0, 3}, 97.9)
            });
        });

        test3.executeTest((out, inout, err) -> {
            runTest(code, out, inout, err, 98103, new Patient[] {
                    new Patient("Billy", 3, new int[] {981, 1234, Integer.MAX_VALUE, 98548}, "yes", new int[] {100000000, 10}, 102.3),
                    new Patient("Sarah", 18, 92509, "nah", 3, 97.6)
            });
        });
    }

    public void runTest(File code, PrintStream out, PrintStream inout, PrintStream err, int HOSPITAL_ZIP, Patient[] patients) throws Exception {
        String source = Files.readString(Path.of(code.toURI()));

        source = source.replaceAll("(public)? +class +(\\w+) +\\{", "public class PrioritizingPatients {");
        source = source.replaceAll("package +[\\w.]+ *;", "");

        Matcher m = Pattern.compile("int HOSPITAL_ZIP *= *\\d+ *;").matcher(source);
        if (!m.find()) throw new TestException("Could not find the class constant HOSPITAL_ZIP, please make sure it is spelled correctly according to the spec and is of data type INT.");
        source = m.replaceAll("int HOSPITAL_ZIP = "+HOSPITAL_ZIP+";");
        
        try (CompiledCode compiledCode = new CompiledCode("PrioritizingPatients", source, out, inout, err)) {

            compiledCode.runCode();

            ConsoleReader consoleOut = compiledCode.getProcessOutput();
            OutputStream consoleIn = compiledCode.getProcessInput();

            assertEquals(consoleOut.readLine(1000, TimeUnit.MILLISECONDS), "Hello! We value you and your time, so we will help you prioritize which patients to see next!");
            assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Please answer the following questions about the next patient so we can help you do your best work :)");
            assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "");

            int count = 0;
            int maxScore = 0;

            for (Patient p : patients) {
                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Please enter the next patient's name or \"quit\" to end the program.");

                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Patient's name: ");
                consoleIn.write((p.name+"\n").getBytes(StandardCharsets.US_ASCII));
                consoleIn.flush();

                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Patient age: ");
                consoleIn.write((p.age+"\n").getBytes(StandardCharsets.US_ASCII));
                consoleIn.flush();

                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Patient zip code: ");
                consoleIn.write((p.zip[0]+"\n").getBytes(StandardCharsets.US_ASCII));
                consoleIn.flush();
                for (int i = 1; i < p.zip.length; i++) {
                    assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Invalid zip code, enter valid zip code: ");
                    consoleIn.write((p.zip[i]+"\n").getBytes(StandardCharsets.US_ASCII));
                    consoleIn.flush();
                }

                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Is our hospital \"in network\" for the patient's insurance? ");
                consoleIn.write((p.inNetwork+"\n").getBytes(StandardCharsets.US_ASCII));
                consoleIn.flush();

                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Patient pain level (1-10): ");
                consoleIn.write((p.painLevel[0]+"\n").getBytes(StandardCharsets.US_ASCII));
                consoleIn.flush();
                for (int i = 1; i < p.painLevel.length; i++) {
                    assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Invalid pain level, enter valid pain level (1-10): ");
                    consoleIn.write((p.painLevel[i]+"\n").getBytes(StandardCharsets.US_ASCII));
                    consoleIn.flush();
                }

                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Patient temperature (in degrees Fahrenheit): ");
                consoleIn.write((p.temp+"\n").getBytes(StandardCharsets.US_ASCII));
                consoleIn.flush();

                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "");

                int score = computeScore(HOSPITAL_ZIP, p);
                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "We have found patient " + p.name + " to have a priority score of: " + score);
                if (score >= 332) {
                    assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "We have determined this patient is high priority, and it is advised to call an appropriate medical provider ASAP.");
                } else if (score >= 166) {
                    assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "We have determined this patient is medium priority. Please assign an appropriate medical provider to their case and check back in with the patient's condition in a little while.");
                } else {
                    assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "We have determined this patient is low priority. Please put them on the waitlist for when a medical provider becomes available.");
                }

                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "");
                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Thank you for using our system!");
                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "We hope we have helped you do your best!");
                assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "");

                count++;
                maxScore = Math.max(maxScore, score);
            }

            assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Please enter the next patient's name or \"quit\" to end the program.");
            assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Patient's name: ");
            consoleIn.write("quit\n".getBytes(StandardCharsets.US_ASCII));
            consoleIn.flush();

            assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Statistics for the day:");
            assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "..." + count + " patients were helped");
            assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "...the highest priority patient we saw had a score of " + maxScore);
            assertEquals(consoleOut.readLine(100, TimeUnit.MILLISECONDS), "Good job today!");

        }
    }

    static int computeScore(int HOSPITAL_ZIP, Patient p) {
        int score = 100;
        if (p.age < 12 || p.age >= 75) score += 50;
        if (p.zip[p.zip.length-1] / 10000 == HOSPITAL_ZIP / 10000) {
            score += 25;
            if (p.zip[p.zip.length-1] / 1000 % 10 == HOSPITAL_ZIP / 1000 % 10) score += 15;
        }
        if (p.inNetwork.equalsIgnoreCase("y") || p.inNetwork.equalsIgnoreCase("yes")) score += 50;
        score += p.painLevel[p.painLevel.length-1] * 10;
        if (p.temp > 99.5) score += 8;
        return score;
    }

    public static class Patient {
        public String name;
        public int age;
        public int[] zip;
        public String inNetwork;
        public int[] painLevel;
        public double temp;

        public Patient(String name, int age, int zip, String inNetwork, int painLevel, double temp) {
            this.name = name;
            this.age = age;
            this.zip = new int[] {zip};
            this.inNetwork = inNetwork;
            this.painLevel = new int[] {painLevel};
            this.temp = temp;
        }

        public Patient(String name, int age, int[] zip, String inNetwork, int[] painLevel, double temp) {
            this.name = name;
            this.age = age;
            this.zip = zip;
            this.inNetwork = inNetwork;
            this.painLevel = painLevel;
            this.temp = temp;
        }
    }
}
