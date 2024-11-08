package me.the1withspaghetti.examples;
// AP Computer Science
// Programming Assignment #2: PrioritizingPatients
// This program takes in different console inputs representing patient
// information and calculates a priority score to rank their priority of
// help/treatment to other patients.

import java.util.Scanner;

public class PrioritizingPatients {

    public static final int HOSPITAL_ZIP = 12345;

    public static void main(String[] args) {

        Scanner in = new Scanner(System.in);

        printIntro();

        int count = 0;
        int maxScore = 0;
        String name;
        int score;

        name = getName(in);
        while (!name.equalsIgnoreCase("quit")) {

            score = collectInfo(in);
            printPriority(name, score);

            //System.out.println();
            System.out.println("\nThank you for using our system!");
            System.out.println("We hope we have helped you do your best!\n");
            //System.out.println();

            count++;
            maxScore = Math.max(maxScore, score);
            name = getName(in);
        }

        printStats(count, maxScore);
    }

    /**
     * Prints an intro message and asks user to answer questions
     */
    static void printIntro() {
        System.out.println("Hello! We value you and your time, so we will help\n" +
                "you prioritize which patients to see next!");
        System.out.println("Please answer the following questions about the next patient so\n" +
                "we can help you do your best work :)");
        System.out.println();
    }

    /**
     * Prompts user for a patient name and returns
     * @param in - The scanner to use for user input
     * @return - The returned name
     */
    static String getName(Scanner in) {
        System.out.println("Please enter the next patient's name or \"quit\" to end the program.");
        System.out.print("Patient's name: ");
        return in.next();
    }

    /**
     * Prompts user for a patient info (age, zip, in network, pain level, temp) and returns a calculated score
     * @param in - The scanner to use for user input
     * @return - The patient priority score
     */
    static int collectInfo(Scanner in) {
        System.out.print("Patient age: ");
        int age = in.nextInt();

        System.out.print("Patient zip code: ");
        int zip = in.nextInt();
        while (!(10000 <= zip && zip <= 99999)) {
            System.out.print("Invalid zip code, enter valid zip code: ");
            zip = in.nextInt();
        }

        System.out.print("Is our hospital \"in network\" for the patient's insurance? ");
        String inNetwork = in.next();

        System.out.print("Patient pain level (1-10): ");
        int painLevel = in.nextInt();
        while (!(1 <= painLevel && painLevel <= 10)) {
            System.out.print("Invalid pain level, enter valid pain level (1-10): ");
            painLevel = in.nextInt();
        }

        System.out.print("Patient temperature (in degrees Fahrenheit): ");
        double temp = in.nextDouble();

        System.out.println();

        return computeScore(age, zip, inNetwork, painLevel, temp);
    }

    /**
     * Calculates a priority score based on collected info about the patient
     * @param age - patient age
     * @param zip - patient zip code, must be a 5 digit int
     * @param inNetwork - Whether the patient is in-network, "y" or "yes" for true, anything else for false
     * @param painLevel - Pain level from 1 to 10
     * @param temp - Temp in F
     * @return The priority score
     */
    static int computeScore(int age, int zip, String inNetwork, int painLevel, double temp) {
        int score = 100;
        if (age < 12 || age >= 75) score += 50;
        if (zip / 10000 == HOSPITAL_ZIP / 10000) {
            score += 25;
            if (zip / 1000 % 10 == HOSPITAL_ZIP / 1000 % 10) score += 15;
        }
        if (inNetwork.equalsIgnoreCase("y") || inNetwork.equalsIgnoreCase("yes")) score += 50;
        score += painLevel * 10;
        if (temp > 99.5) score += 8;
        return score;
    }

    /**
     * Prints the priority score and recommended action
     * @param name - The patient name for the message
     * @param score - The patient score
     */
    static void printPriority(String name, int score) {
        System.out.println("We have found patient "+name+" to have a priority score of: "+score);
        if (score >= 332) {
            System.out.println("We have determined this patient is high priority,\n" +
                    "and it is advised to call an appropriate medical provider ASAP.");
        } else if (score >= 166) {
            System.out.println("We have determined this patient is medium priority.\n" +
                    "Please assign an appropriate medical provider to their case\n" +
                    "and check back in with the patient's condition in a little while.");
        } else {
            System.out.println("We have determined this patient is low priority.\n" +
                    "Please put them on the waitlist for when a medical provider becomes available.");
        }
    }

    /**
     * Prints overall stats when all patients have been scored
     * @param count - The count of patients
     * @param maxScore - The max score for the message
     */
    static void printStats(int count, int maxScore) {
        System.out.println("Statistics for the day:");
        System.out.println("..."+count+" patients were helped");
        System.out.println("...the highest priority patient we saw had a score of "+maxScore);
        System.out.println("Good job today!");
    }
}
