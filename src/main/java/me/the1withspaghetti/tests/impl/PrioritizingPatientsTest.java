package me.the1withspaghetti.tests.impl;

import me.the1withspaghetti.compiler.ClassCompiler;
import me.the1withspaghetti.panals.ExpandableTest;
import me.the1withspaghetti.panals.TestPanel;
import me.the1withspaghetti.tests.AbstractTest;
import me.the1withspaghetti.tests.TestException;
import me.the1withspaghetti.util.ConsoleBuffer;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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


        test1.executeTest((out, err, consoleIn, consoleOut) -> {
            runTest(code, consoleIn, consoleOut, err, 12345, new Patient[] {
                    new Patient("Jun", 5, 44467, "yes", 2, 99.8),
                    new Patient("Emma", 77, 12487, "y", 10, 101.7),
                    new Patient("Johan", 22, 92107, "no", 5, 104.3),
            });
        });

        test2.executeTest((out, err, consoleIn, consoleOut) -> {
            runTest(code, consoleIn, consoleOut, err, 12345, new Patient[] {
                    new Patient("Issa", 14, new int[] {998, 1, 715498, 66548}, "yes", new int[] {-8, 0, 3}, 97.9)
            });
        });

        test3.executeTest((out, err, consoleIn, consoleOut) -> {
            runTest(code, consoleIn, consoleOut, err, 98103, new Patient[] {
                    new Patient("Billy", 3, new int[] {981, 1234, Integer.MAX_VALUE, 98548}, "yes", new int[] {100000000, 10}, 102.3),
                    new Patient("Sarah", 18, 92509, "nah", 3, 97.6)
            });
        });
    }

    public void runTest(File code, PrintStream consoleIn, ConsoleBuffer consoleOut, PrintStream err, int HOSPITAL_ZIP, Patient[] patients) throws Exception {
        String source = Files.readString(Path.of(code.toURI()));

        source = source.replaceAll("(public)? +class +(\\w+) +\\{", "public class PrioritizingPatients {");

        Matcher m = Pattern.compile("int HOSPITAL_ZIP *= *\\d+ *;").matcher(source);
        if (!m.find()) throw new TestException("Could not find the class constant HOSPITAL_ZIP, please make sure it is spelled correctly according to the spec and is of data type INT.");
        source = m.replaceAll("int HOSPITAL_ZIP = "+HOSPITAL_ZIP+";");
        Class<?> clazz = ClassCompiler.compileCode("PrioritizingPatients", source);

        ExecutorService exe = Executors.newSingleThreadExecutor(r -> {
            Thread t = Executors.defaultThreadFactory().newThread(r);
            t.setDaemon(true);
            t.setPriority(Thread.currentThread().getPriority() - 1);
            return t;
        });
        exe.submit(()->{
            try {
                Method main = clazz.getDeclaredMethod("main", String[].class);
                main.invoke(null, (Object) new String[] {});
            } catch (NoSuchMethodException | IllegalAccessException e) {
                err.println("There was an error compiling your code: ");
                e.printStackTrace(err);
            } catch (InvocationTargetException e) {
                if (e.getCause() != null) {
                    err.println("\nYour program threw the following error while running: ");
                    e.getCause().printStackTrace(err);
                }
            }
        });
        
        try {

            assertEquals(consoleOut.nextLine(), "Hello! We value you and your time, so we will help you prioritize which patients to see next!");
            assertEquals(consoleOut.nextLine(), "Please answer the following questions about the next patient so we can help you do your best work :)");
            assertEquals(consoleOut.nextLine(), "");

            int count = 0;
            int maxScore = 0;

            for (Patient p : patients) {
                assertEquals(consoleOut.nextLine(), "Please enter the next patient's name or \"quit\" to end the program.");

                assertEquals(consoleOut.nextLine(), "Patient's name: ");
                consoleIn.println(p.name);

                assertEquals(consoleOut.nextLine(), "Patient age: ");
                consoleIn.println(p.age);

                assertEquals(consoleOut.nextLine(), "Patient zip code: ");
                consoleIn.println(p.zip[0]);
                for (int i = 1; i < p.zip.length; i++) {
                    assertEquals(consoleOut.nextLine(), "Invalid zip code, enter valid zip code: ");
                    consoleIn.println(p.zip[i]);
                }

                assertEquals(consoleOut.nextLine(), "Is our hospital \"in network\" for the patient's insurance? ");
                consoleIn.println(p.inNetwork);

                assertEquals(consoleOut.nextLine(), "Patient pain level (1-10): ");
                consoleIn.println(p.painLevel[0]);
                for (int i = 1; i < p.painLevel.length; i++) {
                    assertEquals(consoleOut.nextLine(), "Invalid pain level, enter valid pain level (1-10): ");
                    consoleIn.println(p.painLevel[i]);
                }

                assertEquals(consoleOut.nextLine(), "Patient temperature (in degrees Fahrenheit): ");
                consoleIn.println(p.temp);

                assertEquals(consoleOut.nextLine(), "");

                int score = computeScore(HOSPITAL_ZIP, p);
                assertEquals(consoleOut.nextLine(), "We have found patient " + p.name + " to have a priority score of: " + score);
                if (score >= 332) {
                    assertEquals(consoleOut.nextLine(), "We have determined this patient is high priority, and it is advised to call an appropriate medical provider ASAP.");
                } else if (score >= 166) {
                    assertEquals(consoleOut.nextLine(), "We have determined this patient is medium priority. Please assign an appropriate medical provider to their case and check back in with the patient's condition in a little while.");
                } else {
                    assertEquals(consoleOut.nextLine(), "We have determined this patient is low priority. Please put them on the waitlist for when a medical provider becomes available.");
                }

                assertEquals(consoleOut.nextLine(), "");
                assertEquals(consoleOut.nextLine(), "Thank you for using our system!");
                assertEquals(consoleOut.nextLine(), "We hope we have helped you do your best!");
                assertEquals(consoleOut.nextLine(), "");

                count++;
                maxScore = Math.max(maxScore, score);
            }

            assertEquals(consoleOut.nextLine(), "Please enter the next patient's name or \"quit\" to end the program.");
            assertEquals(consoleOut.nextLine(), "Patient's name: ");
            consoleIn.println("quit");

            assertEquals(consoleOut.nextLine(), "Statistics for the day:");
            assertEquals(consoleOut.nextLine(), "..." + count + " patients were helped");
            assertEquals(consoleOut.nextLine(), "...the highest priority patient we saw had a score of " + maxScore);
            assertEquals(consoleOut.nextLine(), "Good job today!");

        } catch (Throwable e) {
            exe.shutdownNow();
            exe.awaitTermination(3, TimeUnit.SECONDS);
            throw e;
        }
        exe.awaitTermination(3, TimeUnit.SECONDS);
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
