// AP Computer Science
// Programming Assignment #1: Election Simulator
// This program runs a simulation of an election of NUM_SIM districts, NUM_SIM times
// Assumes 2 candidates and POLL_AVG percent voting for ours, with POLL_ERR percent error

package me.the1withspaghetti.examples;

import java.util.Random;

public class ElectionSimulator {

    public static final int NUM_SIMS = 5;
    public static final int NUM_DISTS = 10;
    public static final double POLL_AVG = 0.52;
    public static final double POLL_ERR = 0.07;

    public static void main(String[] args) {
        final Random rand = new Random();

        // Prints welcome message
        System.out.println("Welcome to the Election Simulator!");
        System.out.printf("Running %d simulations of %d districts.\n", NUM_SIMS, NUM_DISTS);
        System.out.printf("Our candidate is polling at %.2f%% with a %.2f%% margin of error.\n\n", POLL_AVG*100, POLL_ERR*100);

        // Runs each simulation
        double avgTotal = 0;
        for (int i = 1; i <= NUM_SIMS; i++) {
            System.out.println("Running simulation #"+i+":");
            avgTotal += runSimulation(rand);
        }

        System.out.println();
        System.out.println("Average vote percentage: "+(avgTotal/NUM_SIMS) + "%");
    }

    /**
     *
     * @param rand - Random class used for generating random numbers
     * @return double percent - The percent of people in this simulation that voted for our candidate
     */
    static double runSimulation(Random rand) {
        int positiveVotes = 0;
        int totalVotes = 0;

        // Calculates voters for each district
        for (int i = 0; i < NUM_DISTS; i++) {
            int voters = rand.nextInt(1000) + 1;
            double voteError = rand.nextGaussian() * 0.5 * POLL_ERR;
            positiveVotes += (int) (voters * (POLL_AVG + voteError));
            totalVotes += voters;
        }
        double percent = (double)positiveVotes/totalVotes * 100;

        // Prints results
        System.out.println("  Win? " + (percent > 50.0 ));
        System.out.printf("  Results: %d (%.2f%%) - %d (%.2f%%)\n", positiveVotes, percent, totalVotes - positiveVotes, 100-percent);
        System.out.print("  Visualization: ");
        for (int i = 100; i <= positiveVotes; i += 100) System.out.print("+");
        System.out.print("\n                 ");
        for (int i = 100; i <= totalVotes - positiveVotes; i += 100) System.out.print("-");
        System.out.println();

        return percent;
    }
}