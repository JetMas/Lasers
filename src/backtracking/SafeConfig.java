package backtracking;

import javafx.geometry.Insets;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

/**
 * The class represents a single configuration of a safe.  It is
 * used by the backtracker to generate successors, check for
 * validity, and eventually find the goal.
 *
 * This class is given to you here, but it will undoubtedly need to
 * communicate with the model.  You are free to move it into the model
 * package and/or incorporate it into another class.
 *
 * @author Sean Strout @ RIT CS
 * @author Alex Williams
 * @author Jethro Masangya
 */
public class SafeConfig implements Configuration {
    private char[][] Safe;

    public final static char EMPTY = '.';
    public final static char LASER = 'L';
    public final static char BEAM = '*';
    public final static char XPILLAR = 'X';

    private int rownum;
    private int colnum;

    public int R;
    public int C;

    public SafeConfig(String filename) throws FileNotFoundException {
        Scanner in = new Scanner(new File(filename));

        String[] line = in.nextLine().split(" ");
        this.rownum = Integer.parseInt(line[0]);
        this.colnum = Integer.parseInt(line[1]);

        this.Safe = new char[colnum][rownum];

        for (int i = 0; i < rownum; i++) {
            char[] row = in.nextLine().replaceAll(" ", "").toCharArray();
            for (int k = 0; k < colnum; k++) {
                Safe[k][i] = row[k];
            }
        }
        in.close();
    }

    public SafeConfig(SafeConfig other){
        this.Safe = new char[other.colnum][other.rownum];
        this.rownum = other.rownum;
        this.colnum = other.colnum;
        this.R = other.R;
        this.C = other.C;
            for(int i = 0; i < rownum; i++){
                for(int c = 0; c < colnum; c++){
                    this.Safe[c][i] = other.getSafe()[c][i];
                }
            }
    }


    @Override
    public Collection<Configuration> getSuccessors() {
        ArrayList<Configuration> successors = new ArrayList<>();
        if(C == colnum-1){
            R++;
            C = -1;
        }
        if(R == rownum){
            return successors;
        }
        C++;
        if (Safe[C][R] == XPILLAR || isInteger(Safe[C][R])){
            SafeConfig pillarConfig = new SafeConfig(this);
            successors.add(pillarConfig);
            return successors;
        }
        SafeConfig laserConfig = new SafeConfig(this);
        laserConfig.addLaser(laserConfig.R, laserConfig.C);
        if(laserConfig.isValid()){
            successors.add(laserConfig);
        }
        SafeConfig emptyConfig = new SafeConfig(this);
        if(emptyConfig.isValid()){
            successors.add(emptyConfig);
        }
        return successors;
    }

    /**
     * Checks if the given char is a digit, if it is return true.
     * @param c char
     * @return boolean
     */
    public boolean isInteger(char c){
        try {
            Integer.parseInt(String.valueOf(c));
            return true;
        }
        catch (NumberFormatException e){
            return false;
        }
    }


    @Override
    public boolean isValid() {
        char[][] currGrid = this.copyWithBeams();
        for (int r = 0; r < colnum; r++) {
            for (int c = 0; c < rownum; c++) {
                char curr = currGrid[r][c];
                if (curr == LASER) {
                    if (laserNeighbor(r, c)) {
                        this.R = r;
                        this.C = c;
                        return false;
                    }
                    else if (!PillarNeigbor(r,c)){
                        return false;
                    }
                } else if (Character.isDigit(curr)) {
                    int lasers = countLasers(r, c);
                    if (lasers > Character.getNumericValue(curr)) {
                        this.R = r;
                        this.C = c;
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean isGoal() {
        int emptyCount = 0;
        char[][] currGrid = this.copyWithBeams();
        for (int c = 0; c < colnum; c++) {
            for (int r = 0; r < rownum; r++) {
                char curr = currGrid[c][r];
                if (curr == EMPTY) {
                    emptyCount++;
                }
                else if(Character.isDigit(curr)){
                    int lasers = countLasers(c, r);
                    if (lasers != Character.getNumericValue(curr)){
                        return false;
                    }
                }
            }
        }
        if (emptyCount > 0) {
            return false;
        }
        return true;
    }

    /**
     * With the current Safe grid, the method creates a copy with the Beams for the lasers tiles
     * added in
     *
     * @return char[][]
     */
    public char[][] copyWithBeams() {
        char[][] copy = new char[this.colnum][this.rownum];
        for (int i = 0; i < colnum; i++) {
            for (int j = 0; j < rownum; j++) {
                copy[i][j] = this.Safe[i][j];
            }
        }

        for (int i = 0; i < rownum; i++) {
            for (int j = 0; j < colnum; j++) {
                char curr = copy[j][i];
                if (curr == LASER) {
                    addBeams(i, j, copy);
                }
            }
        }
        return copy;
    }

    /**
     * With a row, column coordinate and a char[][], the method adds beam tiles originating from the
     * given coordinate.
     *
     * @param row  row coordinate
     * @param col  column coordinate
     * @param grid 2D array representing the safe
     */
    public void addBeams(int row, int col, char[][] grid) {
        int colChange = 1;
        while (col + colChange < colnum) {
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
        while (row + rowChange < rownum) {
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
     * Adds a laser to a specified coordinate spot in the Safe. If the space is not taken up by an empty spot or a beam, then an error
     * message is printed. Otherwise, that space is replaced with a laser.
     *
     * @param row The row that the coordinates are pointing to. On an (x,y) coordinate grid, row would refer to the y-value.
     * @param col The column that the coordinates are pointing to. On an (x,y) coordinate grid, col would refer to the x-value.
     */
    public void addLaser(int row, int col) {
        if (Safe[col][row] != EMPTY && Safe[col][row] != BEAM) {
            System.out.println("Error adding laser at: (" + row + ", " + col + ")");
        } else {
            Safe[col][row] = LASER;
        }
    }

    /**
     * Removes a laser from the Safe object. If the the coordinates specified do not point to a laser, then the method prints
     * an error message. If the coordinates do point to a laser, then it is removed and the space becomes an empty spot.
     *
     * @param row The row that the coordinates are pointing to. On an (x,y) coordinate grid, row would refer to the y-value.
     * @param col The column that the coordinates are pointing to. On an (x,y) coordinate grid, col would refer to the x-value.
     */
    public void removeLaser(int row, int col) {
        if (Safe[col][row] != LASER) {
            System.out.println("Error removing laser at: (" + row + ", " + col + ")");
        } else {
            Safe[col][row] = EMPTY;
            System.out.println("Laser removed at: (" + row + ", " + col + ")");
        }
    }

    /**
     * Counts the number of lasers around a x,y coordinate
     *
     * @param x x or column coordinate on a grid
     * @param y y or row coordinate on a grid
     * @return The number of lasers
     */
    public int countLasers(int x, int y) {
        char n1, n2, n3, n4;

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
        if (x + 1 >= colnum) {
            n3 = 0;
        } else {
            n3 = Safe[x + 1][y];
        }
        nLst.add(n3);
        if (y + 1 >= rownum) {
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
     * Takes in a coordinate to a current Laser tile, checks if there is another laser in the
     * same horizontal and vertical line and returns true, false otherwise.
     *
     * @param row row number
     * @param col column number
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
        for (int i = col + 1; i < colnum; i++) {
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
        for (int i = row + 1; i < rownum; i++) {
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
     * Returns a string representation of the Safe
     * @return String
     */
    public String toString(){
        char[][] grid = this.copyWithBeams();
        String display = "  ";
        for (int col = 0; col < this.colnum; col++) {
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
        for (int divider = 0; divider < ((this.colnum * 2) - 1); divider++) {
            display += "-";
        }
        display += "\n";
        for (int r = 0; r < this.rownum; r++) {
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
            for (int c = 0; c < this.colnum; c++) {
                display += grid[c][r] + " ";
            }
            if(r != this.rownum-1) {
                display += "\n";
            }
        }
        return display;
    }

    /**
     * Returns the two-dimensional char array that represents the Safe
     * @return char[][]
     */
    public char[][] getSafe(){
        return this.Safe;
    }

    /**
     * Checks if the coordinate is next to a pillar, if it is return true else false
     * @param x X coordinate on a grid
     * @param y Y coordinate on a grid
     * @return boolean
     */
    private boolean PillarNeigbor(int x, int y){
        char n1, n2, n3, n4, n5, n6, n7, n8;

        ArrayList<Character> nLst = new ArrayList<Character>();
        if (x - 1 < 0) {
            n1 = 'x';
        } else {
            n1 = Safe[x - 1][y];
        }
        nLst.add(n1);
        if (y - 1 < 0) {
            n2 = 'x';
        } else {
            n2 = Safe[x][y - 1];
        }
        nLst.add(n2);
        if (x + 1 >= colnum) {
            n3 = 'x';
        } else {
            n3 = Safe[x + 1][y];
        }
        nLst.add(n3);
        if (y + 1 >= rownum) {
            n4 = 'x';
        } else {
            n4 = Safe[x][y + 1];
        }
        nLst.add(n4);
        if(x - 1 < 0 || y - 1 < 0){
            n5 = 'x';
        } else{
            n5 = Safe[x-1][y-1];}
        nLst.add(n5);
        if(x + 1 >= colnum || y - 1 < 0){
            n6 = 'x';
        } else{
            n6 = Safe[x+1][y-1];}
        nLst.add(n6);
        if(x + 1 >= colnum || y + 1 >= rownum){
            n7 = 'x';
        } else{
            n7 = Safe[x+1][y+1];}
        nLst.add(n7);
        if(x - 1 < 0 || y + 1 >= rownum){
            n8 = 'x';
        } else{
            n8 = Safe[x-1][y+1];}
        nLst.add(n8);
        for (char i : nLst) {
            if(Character.isDigit(i) || i == 'X'){
                return true;
            }
        }
        return false;
    }

}
