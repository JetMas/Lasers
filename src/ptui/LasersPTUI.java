package ptui;

import java.io.FileNotFoundException;
import java.util.Observable;
import java.util.Observer;

import model.LasersModel;

/**
 * This class represents the view portion of the plain text UI.  It
 * is initialized first, followed by the controller (ControllerPTUI).
 * You should create the model here, and then implement the update method.
 *
 * @author Sean Strout @ RIT CS
 * @author Alex Williams
 * @author Jethro Masangya
 */
public class LasersPTUI implements Observer {
    /** The UI's connection to the model */
    private LasersModel model;

    /**
     * Construct the PTUI.  Create the model and initialize the view.
     * @param filename the safe file name
     * @throws FileNotFoundException if file not found
     */
    public LasersPTUI(String filename) throws FileNotFoundException {
        try {
            this.model = new LasersModel(filename);
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe.getMessage());
            System.exit(-1);
        }
        this.model.addObserver(this);
        model.display();
    }

    public LasersModel getModel() { return this.model; }

    /**
     * Updates the display by calling the model's display method.
     * @param o The observable Object
     * @param arg The argument object.
     */
    @Override
    public void update(Observable o, Object arg) {
        model.display();
    }
}
