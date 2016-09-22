package model;

import backtracking.Backtracker;
import backtracking.Configuration;
import backtracking.SafeConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Optional;
import java.util.Scanner;

/**
 * The model that is used to represent the safe in the GUI. The data is represented by a 2D Array, and is manipulated by
 * methods called by the controller.
 * @author Alex Williams
 * @author Jethro Masangya
 */

public class LasersModel extends Observable {

    /** Character representing an empty space on the grid.*/
    public final static char EMPTY = '.';
    /** Character representing a laser on the grid.*/
    public final static char LASER = 'L';

    /** Character representing a beam on the grid.*/
    public final static char BEAM = '*';

    /** Character representing a non-integer based pillar on the grid.*/
    public final static char XPILLAR = 'X';

    /**2-D Array used to represent the safe in the puzzle.*/
    private char[][] Safe;

    /**The number of rows present in the safe.*/
    private int Rownum;

    /**The number of columns present in the safe.*/
    private int Colnum;

    /**An integer that is used to point to a row in the Safe.*/
    private int R;

    /**An integer that is used to point to a column in the safe.*/
    private int C;

    /**Integer pointing to the row on the safe where an error occured.*/
    private int errorR;

    /**Integer pointing to the column on the safe where an error occured.*/
    private int errorC;

    /**The name of the file that the Safe is being read from.*/
    private String filename;

    /**A 2-D Array that is used to store the solution to the puzzle.*/
    private char[][] SolvedSafe = null;

    /**A boolean that indicates whether the safe has been checked to determine a solution.*/
    private boolean checkedSafe = false;

    /**A thread that is created to find the solution to the puzzle. It is created when the puzzle is initialized.*/
    private Thread SolutionThread;

    /**
     * Initializes the model: creates the grid for the safe, saves the number and rows and column,
     * and saves the filename.
     * @param filename String for the safe filename
     * @throws FileNotFoundException
     */
    public LasersModel(String filename) throws FileNotFoundException {
        this.filename = filename;
        Scanner in = new Scanner(new File(filename));

        String[] line = in.nextLine().split(" ");
        this.Rownum = Integer.parseInt(line[0]);
        this.Colnum = Integer.parseInt(line[1]);
        this.R = 0;
        this.C = 0;
        this.errorC = -1;
        this.errorR = -1;
        this.Safe = new char[this.Colnum][this.Rownum];

        for (int i = 0; i < Rownum; i++) {
            char[] row = in.nextLine().replaceAll(" ", "").toCharArray();
            for (int k = 0; k < Colnum; k++) {
                Safe[k][i] = row[k];
            }
        }
        in.close();

        LoadThread(filename);
    }

    /**
     * A utility method that indicates the model has changed and
     * notifies observers
     */
    private void announceChange() {
        setChanged();
        notifyObservers();
    }

    /**
     * Adds a laser to a specified coordinate spot in the Safe. If the space is not taken up by an empty spot or a beam, then an error
     * message is printed. Otherwise, that space is replaced with a laser.
     * @param row The row that the coordinates are pointing to. On an (x,y) coordinate grid, row would refer to the y-value.
     * @param col The column that the coordinates are pointing to. On an (x,y) coordinate grid, col would refer to the x-value.
     */
    public boolean addLaser(int col, int row) {
        if (Safe[col][row] != EMPTY && Safe[col][row] != BEAM) {
            announceChange();
            return false;
        } else {
            Safe[col][row] = LASER;
            announceChange();
            return true;
        }
    }

    /**
     * Removes a laser from the Safe object. If the the coordinates specified do not point to a laser, then the method prints
     * an error message. If the coordinates do point to a laser, then it is removed and the space becomes an empty spot.
     * @param row The row that the coordinates are pointing to. On an (x,y) coordinate grid, row would refer to the y-value.
     * @param col The column that the coordinates are pointing to. On an (x,y) coordinate grid, col would refer to the x-value.
     */
    public boolean removeLaser(int col, int row) {
        if (Character.isDigit(Safe[col][row]) || Safe[col][row] == XPILLAR) {
            announceChange();
            return false;
        } else {
            Safe[col][row] = EMPTY;
            announceChange();
            return true;
        }
    }

    /**
     * With the current Safe grid, the method creates a copy with the Beams for the lasers tiles
     * added in.
     * @return char[][]
     */
    public char[][] copyWithBeams() {
        char[][] copy = new char[this.Colnum][this.Rownum];
        for(int i=0; i < Colnum; i++) {
            for (int j = 0; j < Rownum; j++) {
                copy[i][j] = this.Safe[i][j];
            }
        }

        for (int i= 0; i < Rownum; i++){
            for (int j = 0; j < Colnum; j++){
                char curr = copy[j][i];
                if(curr == LASER){
                    addBeams(i,j,copy);
                }
            }
        }
        return copy;
    }

    /**
     * With a row, column coordinate and a char[][], the method adds beam tiles originating from the
     * given coordinate.
     * @param row row coordinate.
     * @param col column coordinate.
     * @param grid 2D array representing the safe.
     */
    public void addBeams(int row, int col, char[][] grid) {
        int colChange = 1;
        while (col + colChange < this.Colnum) {
            if (grid[col + colChange][row] == EMPTY || grid[col + colChange][row] == BEAM) {
                grid[col + colChange][row] = BEAM;
                colChange++;
            } else {
                break;
            }
        }
        colChange = 1;
        while (col - colChange >= 0) {
            if (grid[col - colChange][row] == EMPTY || grid[col - colChange][row] == BEAM) {
                grid[col - colChange][row] = BEAM;
                colChange++;
            } else {
                break;
            }
        }
        int rowChange = 1;
        while (row + rowChange < this.Rownum) {
            if (grid[col][row + rowChange] == EMPTY || grid[col][row + rowChange] == BEAM) {
                grid[col][row + rowChange] = BEAM;
                rowChange++;
            } else {
                break;
            }
        }
        rowChange = 1;
        while (row - rowChange >= 0) {
            if (grid[col][row - rowChange] == EMPTY || grid[col][row - rowChange] == BEAM) {
                grid[col][row - rowChange] = BEAM;
                rowChange++;
            } else {
                break;
            }
        }
    }

    /**
     * Returns a boolean if the Safe is Valid.
     * @return boolean
     */
    public boolean isValid() {
        char[][] currGrid = this.copyWithBeams();
        for (int r = 0; r < this.Rownum; r++) {
            for (int c = 0; c < this.Colnum; c++) {
                char curr = currGrid[c][r];
                if (curr == EMPTY) {
                    this.R = r;
                    this.C = c;
                    errorC = c;
                    errorR = r;
                    announceChange();
                    return false;
                } else if (curr == LASER) {
                    if (laserNeighbor(c, r)) {
                        this.R = r;
                        this.C = c;
                        errorC = c;
                        errorR = r;
                        announceChange();
                        return false;
                    }
                } else if (Character.isDigit(curr)) {
                    int lasers = countLasers(c, r);
                    if (lasers != Character.getNumericValue(curr)) {
                        this.R = r;
                        this.C = c;
                        errorC = c;
                        errorR = r;
                        announceChange();
                        return false;
                    }
                }
            }
        }
        announceChange();
        return true;
    }
    /**
     * Takes in a coordinate to a current Laser tile, checks if there is another laser in the
     * same horizontal and vertical line and returns true, false otherwise.
     *
     * @param row row number.
     * @param col column number.
     * @return boolean
     */
    public boolean laserNeighbor(int col, int row) {
        //Checks if there is any lasers left of the coordinate.
        boolean isPillarLeft = false;
        for (int i = col - 1; i >= 0; i--) {
            char curr = Safe[i][row];
            if (Character.isDigit(curr) || curr == XPILLAR) {
                isPillarLeft = true;
            } else if (curr == LASER && !isPillarLeft) {
                return true;
            }
        }
        //Checks if there is any lasers right of the coordinate
        boolean isPillarRight = false;
        for (int i = col + 1; i < this.Colnum; i++) {
            char curr = Safe[i][row];
            if (Character.isDigit(curr) || curr == XPILLAR) {
                isPillarRight = true;
            } else if (curr == LASER && !isPillarRight) {
                return true;
            }
        }

        //Checks if there is any lasers above of the coordinate
        boolean isPillarAbove = false;
        for (int i = row - 1; i >= 0; i--) {
            char curr = Safe[col][i];
            if (Character.isDigit(curr) || curr == XPILLAR) {
                isPillarAbove = true;
            } else if (curr == LASER && !isPillarAbove) {
                return true;
            }
        }

        //Checks if there is any lasers below of the coordinate
        boolean isPillarBelow = false;
        for (int i = row + 1; i < this.Rownum; i++) {
            char curr = Safe[col][i];
            if (Character.isDigit(curr) || curr == XPILLAR) {
                isPillarBelow = true;
            } else if (curr == LASER && !isPillarBelow) {
                return true;
            }
        }
        return false;

    }
    /**
     * Counts the number of lasers around a x,y coordinate.
     *
     * @param x x or column coordinate on a grid.
     * @param y y or row coordinate on a grid.
     * @return The number of lasers.
     */
    public int countLasers(int x, int y) {
        char n1,n2,n3,n4;

        ArrayList<Character> nLst = new ArrayList<Character>();
        if (x - 1 < 0) {
            n1 = 0;
        } else {
            n1 = Safe[x - 1][y];
        }
        nLst.add(n1);
        if (y - 1 < 0) {
            n2 = 0;
        } else {
            n2 = Safe[x][y - 1];
        }
        nLst.add(n2);
        if (x + 1 >= this.Colnum) {
            n3 = 0;
        } else {
            n3 = Safe[x + 1][y];
        }
        nLst.add(n3);
        if (y + 1 >= this.Rownum) {
            n4 = 0;
        } else {
            n4 = Safe[x][y + 1];
        }
        nLst.add(n4);
        int LaserCount = 0;
        for (char i : nLst) {
            if (i == LASER) {
                LaserCount++;
            }
        }

        return LaserCount;
    }

    /**
     * Returns whether the current configuration is the goal configuration. The method assumes that the config is valid, so this method checks to see if there are any empty spaces
     * that are not covered by the beams. Returns true if the config is the goal.
     * @return
     */
    public boolean isGoal() {
        int emptyCount = 0;
        char[][] currGrid = this.copyWithBeams();
        for (int c = 0; c < this.Colnum; c++){
            for(int r = 0; r < this.Rownum; r++){
                char curr = currGrid[c][r];
                if (curr == EMPTY){
                    emptyCount++;
                }
            }
        }
        if (emptyCount > 0){
            return false;
        }
        return true;
    }

    /**
     * Returns the grid that represents the Safe with the beams added in.
     * @return char[][]
     */
    public char[][] getSafe(){
        return this.copyWithBeams();
    }

    /**
     * Returns the number of rows in the safe.
     * @return int
     */
    public int getColnum(){
        return this.Colnum;
    }

    /**
     * Returns the number of rows in the safe.
     * @return int
     */
    public int getRownum(){
        return this.Rownum;
    }

    @Override
    public String toString(){
        char[][] grid = this.copyWithBeams();
        String display = "  ";
        for (int col = 0; col < this.Colnum; col++) {
            if (col >= 10) {
                int colCopy = col;
                while (colCopy >= 10) {
                    colCopy -= 10;
                }
                display+= colCopy+" ";
            }
            else {
                display += col + " ";
            }
        }
        display += "\n  ";
        for (int divider = 0; divider < ((this.Colnum * 2) - 1); divider++) {
            display += "-";
        }
        display += "\n";
        for (int r = 0; r < this.Rownum; r++) {
            if(r >= 10){
                int row = r;
                while(row >= 10){
                    row -= 10;
                }
                display+= row+"|";
            }
            else {
                display += r + "|";
            }
            for (int c = 0; c < this.Colnum; c++) {
                display += grid[c][r] + " ";
            }
            if(r != this.Rownum-1) {
                display += "\n";
            }
        }
        return display;
    }

    /**
     * Prints out a list of commands that the user can give.
     */
    public void helpMessage() {
        String message = "a|add r c: Add laser to (r,c)\nd|display: Display safe\nh|help: Print this help message\nq|quit: Exit program\n" +
                "r|remove r c: Remove laser from (r,c)\nv|verify: Verify safe correctness";
        System.out.println(message);
    }

    /**
     * Prints the Safe grid with the beams added in.
     */
    public void display(){
        System.out.println(this.toString());
    }

    /**
     * Takes a string command input from a file or the user, and calls other methods based on the string's command.
     * If an incorrect command or coordinate is given, then an error message is printed out.
     * @param com
     */
    public void command(String com) {
        String[] action = com.split(" ");
        String command = action[0];
        if (command.toLowerCase().startsWith("a")){
            if (action.length > 3 || action.length < 3){
                System.out.println("Incorrect coordinates");
            }
            else {
                if (Integer.parseInt(action[1]) >= this.Rownum || Integer.parseInt(action[2]) >= this.Colnum || Integer.parseInt(action[2]) < 0 || Integer.parseInt(action[1]) < 0){
                    announceChange();
                    System.out.println("Error adding laser at: (" + action[2] + ", " + action[1] +")");

                }
                else {
                    this.addLaser(Integer.parseInt(action[2]), Integer.parseInt(action[1]));

                    System.out.println("Laser added at: (" + action[2] + ", " + action[1] +")");
                }
            }
        }
        else if (command.toLowerCase().startsWith("d")){
            display();
        }
        else if (command.toLowerCase().startsWith("h")){
            this.helpMessage();
        }
        else if (command.toLowerCase().startsWith("q")){
            System.exit(0);
        }
        else if (command.toLowerCase().startsWith("r")){
            if (action.length > 3 || action.length < 3){
                System.out.println("Incorrect coordinates");
            }
            else {
                if (Integer.parseInt(action[1]) >= this.Rownum || Integer.parseInt(action[2]) >= this.Colnum || Integer.parseInt(action[2]) < 0 || Integer.parseInt(action[1]) < 0){
                    System.out.println("Error removing laser at: (" + action[2] + ", " + action[1] +")");
                }
                else {
                    if(!(this.removeLaser(Integer.parseInt(action[2]), Integer.parseInt(action[1])))){
                        System.out.println("Error removing laser at: (" + action[2] + ", " + action[1] +")");
                    }
                    else{
                        System.out.println("Laser removed at: (" + action[2] + ", " + action[1] + ")");
                    }
                }
            }
        }
        else if (command.toLowerCase().startsWith("v")){
            if (this.isValid()) {
                System.out.println("Safe is fully verified!");
            }
            else {
                System.out.println("Error verifying at: "+"("+this.R+", " +this.C +")");
            }
        }
        else if(!com.equals("")){
            System.out.println("Unrecognized command: " + com);
        }
    }

    /**
     * Returns the filename that was used to create the model.
     * @return String
     */
    public String getFilename(){
        return this.filename;
    }

    /**
     * Returns true is the character at the given column row coordinate is a laser.
     * @param col int column coordinate
     * @param row int row coordinate
     * @return boolean
     */
    public boolean isLaser(int col, int row){
        return this.Safe[col][row] == LASER;
    }


    /**
     * Returns the R variable, which holds the row coordinate of a validation error.
     * @return int
     */
    public int getR(){
        return this.R;
    }

    /**
     * Gets the errorR field, which points to the row where the error occured.
     * @return integer of errorR.
     */
    public int getErrorR(){
        return this.errorR;
    }

    /**
     * Gets the errorC field, which points to the row where the error occured.
     * @return integer of errorC.
     */
    public int getErrorC(){
        return this.errorC;
    }

    /**
     * Resets the errorR and errorC fields to -1. The only purpose of these fields is to point out any errors, and since -1 cannot be represented on the grid,
     * their default positions are set to this when there is no error.
     */
    public void resetErrorRC(){
        this.errorR = -1;
        this.errorC = -1;
    }

    /**
     * Returns the C variable, which holds the column coordinate of a validation error.
     * @return int
     */
    public int getC(){
        return this.C;
    }

    /**
     * Solves the puzzle automatically, by setting the safe to equal the solution array. If the solution has not been set, for whatever reason,
     * then nothing happens.
     * @throws FileNotFoundException If there is no file found, then an exception is thrown.
     */
    public void solve() throws FileNotFoundException {
        if(!(this.SolvedSafe == null)){
            this.Safe = this.SolvedSafe;
        }
        announceChange();
    }

    /**
     * Runs a hint for the Safe by looping through both the solution config and the Safe config. If a Laser is found in the solution, then
     * the Safe gets a laser added to the same coordinates. If adding the laser would yield an invalid configuration, then the laser is NOT added.
     * In either case, the loop is broken out of by returning true if the laser could be added, and false otherwise. If the loop is completed, then the solution has been found,
     * and the method returns true.
     * @param other An other 2-D array being used as the solution safe.
     * @return boolean indicating whether the laser could be added correctly.
     */
    public boolean runHint(char[][] other){
        for(int row = 0; row < this.getRownum(); row++) {
            for (int col = 0; col < this.getColnum(); col++) {
                this.R = row;
                this.C = col;
                if (other[col][row] == LASER && this.Safe[col][row] == EMPTY) {
                    this.Safe[col][row] = LASER;
                    announceChange();
                    return true;
                } else if (this.Safe[col][row] == LASER && other[col][row] != LASER) {
                    return false;
                }
            }
        }
        return true;
        }

    /**
     * Re-Loads the same file again reverting the model back to its initial state.
     */
    public void restart(){
        try {
            Scanner in = new Scanner(new File(this.filename));
            String[] line = in.nextLine().split(" ");
            this.Rownum = Integer.parseInt(line[0]);
            this.Colnum = Integer.parseInt(line[1]);
            resetErrorRC();

            this.Safe = new char[this.Colnum][this.Rownum];

            for (int i = 0; i < Rownum; i++) {
                char[] row = in.nextLine().replaceAll(" ", "").toCharArray();
                for (int k = 0; k < Colnum; k++) {
                    Safe[k][i] = row[k];
                }
            }
            in.close();
        }catch (Exception e){
            System.out.println(e);
        }
        announceChange();
    }

    /**
     * With a new filename, a new safe is loaded into the model, replacing the old one.
     * @param filename string
     * @throws FileNotFoundException
     */
    public void loadNewFile(String filename) throws FileNotFoundException{
        if (SolutionThread.isAlive()) {
            try {
                SolutionThread.stop();
            } catch (Exception e) {

            }
        }
        try {
            Scanner in = new Scanner(new File(filename));

            this.filename = filename;
            resetErrorRC();
            SolvedSafe = null;

            String[] line = in.nextLine().split(" ");
            this.Rownum = Integer.parseInt(line[0]);
            this.Colnum = Integer.parseInt(line[1]);

            this.Safe = new char[this.Colnum][this.Rownum];

            for (int i = 0; i < Rownum; i++) {
                char[] row = in.nextLine().replaceAll(" ", "").toCharArray();
                for (int k = 0; k < Colnum; k++) {
                    Safe[k][i] = row[k];
                }
            }
            in.close();
        }catch (Exception e){
            System.out.println(e);
        }
       // display();
        LoadThread(filename);
        getSolution();
        checkedSafe = false;
        SolvedSafe = null;
        announceChange();
    }

    /**
     * Returns the Solved version of the Safe.
     * @return char[][]
     */
    public char[][] getSolvedSafe() {
        return SolvedSafe;
    }
    /**
     * Creates a new thread and runs the backtracking for the puzzle.
     */
    public void getSolution(){
        SolutionThread.start();
    }

    /**
     * Returns true is the a solution to the puzzle exist.
     * @return boolean
     */
    public boolean SolutionIsPresent(){
        return (SolvedSafe != null) && checkedSafe;
    }

    /**
     * Returns true if the thread for the backtracking is still running.
     * @return boolean
     */
    public boolean ThreadRunning(){
        return !checkedSafe;
    }

    /**
     * Method used when a new file is loaded to the GUI and model.
     * It sets the Solution Thread to a new thread for the new file.
     * @param filename
     */
    public void LoadThread(String filename){
        SolutionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Backtracker bc = new Backtracker(false);
                Optional<Configuration> Solution;
                try {
                    Solution = bc.solve(new SafeConfig(filename));
                    if (Solution.isPresent()){
                        SolvedSafe = Solution.get().getSafe();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                checkedSafe = true;
            }
        });
    }

    /**
     * Method used when the GUI is closing.
     * Terminates the Solution Thread if it is still alive.
     */
    public void close(){
        if (SolutionThread.isAlive()){
            try {
                SolutionThread.stop();
            }catch (Exception e){}
        }
    }
}
