package ptui;

import model.LasersModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * This class represents the controller portion of the plain text UI.
 * It takes the model from the view (LasersPTUI) so that it can perform
 * the operations that are input in the run method.
 *
 * @author Sean Strout @ RIT CS
 * @author Alex Williams
 * @author Jethro Masangya
 */
public class ControllerPTUI  {
    /** The UI's connection to the model */
    private LasersModel model;

    /**
     * Construct the PTUI.  Create the model and initialize the view.
     * @param model The laser model
     */
    public ControllerPTUI(LasersModel model) {
        this.model = model;
    }

    /**
     * Run the main loop.  This is the entry point for the controller
     * @param inputFile The name of the input command file, if specified
     */
    public void run(String inputFile) throws FileNotFoundException {
        //If there's an input file, a scanner opens to read it as commands.
        if(inputFile!=null) {
            Scanner initial = new Scanner(new File(inputFile));
            while (initial.hasNextLine()) {
                String command = initial.nextLine();
                System.out.println("> " + command);

                model.command(command);
            }
            initial.close();
        }
        //Opens a scanner for user input, and closes the scanner when the user is finished.
            Scanner in = new Scanner(System.in);
            System.out.print("> ");
            while(in.hasNextLine()){
                String command = in.nextLine();
                model.command(command);
                System.out.print("> ");
            }
        in.close();
        }
}
