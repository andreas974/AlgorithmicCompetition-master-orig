package de.unipassau.simulation;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

class Firm {

    private final QLearning qlearning;                // Holds the firm's individual Q-learning algorithm.
    private final SimulationRun simulationRun;        // Holds the simulation run it belongs to.
    private final Competition competition;            // Holds the competition type it is placed in.


    /**
     * @param simulationRun object of class SimulationRun, which has created the class.
     *                      Necessary for identifying all other firms in the market.
     * @param seed          seed for Random-class to reproduce randomness of Q-Learning exploration.
     */
    Firm(SimulationRun simulationRun, int seed) {
        this.simulationRun = simulationRun;
        this.competition = SimulationManager.COMPETITION_TYPE.getCompetition().clone();
        qlearning = new QLearning(seed);
    }

    /**
     * @return price or quantity of the firm in accordance with the type of competition.
     */
    int getAction() {
        return competition.getIndependentVariable();
    }

    double getProfit(){
        return (competition.getIndependentVariable()*competition.getDependentVariable());
    }

    /**
     * @return price or quantity of the firm in accordance with the type of competition.
     */
    int getActionfromQMatrix() {
        return qlearning.getMaxActionIndex(qlearning.getState(getActionsOfOtherFirms()));
    }

    /**
     * @param action price or quantity of the firm to set in accordance with the type of competition.
     */
    void setAction(int action) {
        competition.setIndependentVariable(action);
    }

    /**
     * Instructs this firm's Q-Learning algorithm to run one episode.
     * All competitors' actions are passed at the method-call, since they are necessary to determine this firm's state.
     *
     * @return action result (= price or quantity) that has been chosen by the Q-Learning algorithm.
     */
    int chooseAction() {
        // Call Q-Learning.
        return qlearning.runEpisode(getActionsOfOtherFirms());
    }

    /**
     * Instructs this firm's Q-Learning algorithm to update the Q-matrix based on the type of interaction.
     */
    public void updateQlearning() {
        List<Double> profits = getProfits();

        if (SimulationManager.TIMING == SimulationRun.Timing.SIMULTANEOUS) {
            // Only the last period's profit is necessary to update the Q-matrix.
            qlearning.updateMatrix(getAction(), profits.get(profits.size() - 1), getActionsOfOtherFirms());
        } else if (SimulationManager.TIMING == SimulationRun.Timing.SEQUENTIAL) {
            // All profits after choosing an action are necessary to update the Q-matrix.
            profits = profits.subList(profits.size() - SimulationManager.MARKET_SIZE, profits.size());
            qlearning.updateMatrix(getAction(), profits, getActionsOfOtherFirms());
        }
    }

    /**
     * Write Q-Matrix into .csv File
     * Alternative print methods in comments
     */
    public void MatrixOutput(Firm firm) {

        /*for (int i = 0; i < qlearning.returnMatrix().length; i++) {
                for (int j = 0; j < qlearning.returnMatrix().length; j++) {
                    System.out.println(i+". Zeile und " + j + ".Spalte, Wert:"+ (qlearning.returnMatrix()[i][j]) + " ");
                }
        }*/

    //for (int m = 0; m < SimulationManager.MARKET_SIZE; m++) {
        try {

            //File csvFile = new File("out/SEQ-P-2/QMatrix_with" + simulationRun.getDegreeOfTacitCollusion() + "_collusion_of_Firm_with_ID_" + firm + "_of_Sim_No_" + (simulationRun.getSimulationRunNumber()+1) + ".csv");
            //Snapshot with No. of periods in filename
            File csvFile = new File("out/SEQ-P-2/QMatrix" + "_after_" + simulationRun.getNumberOfPeriods() + "_periods_with" + simulationRun.getDegreeOfTacitCollusion() + "_collusion_of_Firm_with_ID_" + firm + "_of_Sim_No_" + (simulationRun.getSimulationRunNumber()+1) + ".csv");


            PrintWriter out = new PrintWriter(csvFile);
            //out.println("Degree of Tacit Collusion of this matrix: " + simulationRun.getDegreeOfTacitCollusion());
            // Print whole qmatrix
            // for (int i = 0; i < qlearning.getQMatrix().length; i++) {
            for (int i = 0; i < ((qlearning.getQMatrix().length+1)/SimulationManager.actionSetRedFactor)+1; i++) {
                //for (int j = 0; j < qlearning.getQMatrix().length; j++) {
                for (int j = 0; j < ((qlearning.getQMatrix().length+1)/SimulationManager.actionSetRedFactor)+1; j++) {
                    //out.println(i + ". Zeile und " + j + ".Spalte, Wert:" + (qlearning.getQMatrix()[i][j]) + " ");
                    out.print((qlearning.getQMatrix()[i][j]) + ",");
                }
                out.println();
            }
            out.close();

        } catch (Exception ex) {

        }
    //}
    }


    /**
     * Delegates the calculation of all relevant firm data to its Competition object.
     * The firm's action and all competitor's actions are passed, since they are necessary to calculate the profit.
     */
    void calculateData() {
        competition.calculateData(getAction(),getActionsOfOtherFirms());
    }

    /**
     * Returns Q-Matrix from qlearning
     */
    double[][] getQMatrix() {
        double[][] qMatrix = qlearning.getQMatrix();
        return qMatrix;
    }


    /**
     * @return array of all other firms' actions from the point of view of this particular firm
     */
    private Integer[] getActionsOfOtherFirms() {
        // Get all competitors.
        List<Firm> otherFirms = new ArrayList<>(simulationRun.getFirms());
        otherFirms.remove(this);

        Integer[] actionsOfOtherFirms = new Integer[otherFirms.size()];

        // Get actions of competitors.
        for (int i = 0; i < otherFirms.size(); i++) {
            actionsOfOtherFirms[i] = otherFirms.get(i).getAction();
        }

        return actionsOfOtherFirms;
    }

    /**
     * @return quantities of this firm as stored in its corresponding competition instance.
     */
    public List<Double> getQuantities() {
        return competition.getQuantities();
    }

    /**
     * @return profits of this firm as stored in its corresponding competition instance.
     */
    public List<Double> getProfits() {
        return competition.getProfits();
    }

    /**
     * @return prices of this firm as stored in its corresponding competition instance.
     */
    public List<Double> getPrices() {
        return competition.getPrices();
    }

    /**
     * Calls the corresponding competition instance to check whether the actions of this firm's actions of the last two
     * periods are identical or not.
     *
     * @return true if this firm's actions of the last two periods are identical
     */
    public boolean actionsOfLastTwoPeriodsAreIdentical() {
        return competition.actionsOfLastTwoPeriodsAreIdentical();
    }
}

