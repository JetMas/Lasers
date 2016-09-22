package gui;

import backtracking.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Observable;
import java.util.Observer;

import javafx.stage.WindowEvent;
import model.*;
/**
 * The main class that implements the JavaFX UI.   This class represents
 * the view/controller portion of the UI.  It is connected to the model
 * and receives updates from it.
 *
 * @author Sean Strout @ RIT CS
 * @author Alex Williams
 * @author Jethro Masangya
 */
public class LasersGUI extends Application implements Observer {
    /** The UI's connection to the model */
    private LasersModel model;

    /** this can be removed - it is used to demonstrates the button toggle */
    private static boolean status = true;


    /**The Border Pane that will be hold the entire GUI*/
    private BorderPane BP = new BorderPane();

    /**Variable that will hold the GUI Message*/
    private Label GUIMessage = new Label();

    /**Variable that will hold the safe*/
    private GridPane Safe = new GridPane();

    /**Reference to the stage object used by the GUI.*/
    private Stage stage;


    @Override
    public void init() throws Exception {
        // the init method is run before start.  the file name is extracted
        // here and then the model is created.
        try {
            Parameters params = getParameters();
            String filename = params.getRaw().get(0);
            this.model = new LasersModel(filename);
        } catch (FileNotFoundException fnfe) {
            System.out.println(fnfe.getMessage());
            System.exit(-1);
        }
        this.model.addObserver(this);
    }

    /**
     * A private utility function for setting the background of a button to
     * an image in the resources subdirectory.
     *
     * @param button the button control
     * @param bgImgName the name of the image file
     */
    private void setButtonBackground(Button button, String bgImgName) {
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image( getClass().getResource("resources/" + bgImgName).toExternalForm()),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                BackgroundSize.DEFAULT);
        Background background = new Background(backgroundImage);
        button.setBackground(background);
    }

    /**
     * This is a private demo method that shows how to create a button
     * and attach a foreground image with a background image that
     * toggles from yellow to red each time it is pressed.
     *
     * @param stage the stage to add components into
     */
    private void buttonDemo(Stage stage) {
        // this demonstrates how to create a button and attach a foreground and
        // background image to it.
        Button button = new Button();
        Image laserImg = new Image(getClass().getResourceAsStream("resources/laser.png"));
        ImageView laserIcon = new ImageView(laserImg);
        button.setGraphic(laserIcon);
        setButtonBackground(button, "yellow.png");
        button.setOnAction(e -> {
            // toggles background between yellow and red
            if (!status) {
                setButtonBackground(button, "yellow.png");
            } else {
                setButtonBackground(button, "red.png");
            }
            status = !status;
        });

        Scene scene = new Scene(button);
        stage.setScene(scene);
    }

    /**
     * @param stage the stage to add UI components into
     */
    private void init(Stage stage) {
        // Done
        //buttonDemo(stage);  // this can be removed/altered
    }

    /**
     * Start's the GUI by creating a thread to get the solution, and setting the GUIMessage field. The scene is created and set, the stage field is assigned as the primary stage,
     * and is shown. If the window is closed, then the solution thread is stopped.
     * @param primaryStage The stage that will be displayed to the user.
     * @throws Exception An exception that would prevent the method from performing.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Done
        //init(primaryStage);  // do all your UI initialization here

        model.getSolution();

        this.GUIMessage.setPrefSize(150,50);
        this.GUIMessage.setText(model.getFilename() + " loaded");

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                try{
                    stop();
                    model.close();
                }catch (Exception e){}
            }
        });

        makeBP();
        primaryStage.setScene(new Scene(this.BP));
        primaryStage.setTitle("Lasers");
        stage = primaryStage;
        primaryStage.show();
    }

    /**
     * Updates the GUI by creating a new BorderPane object, using it as the scene, and resetting the window size to that scene.
     * @param o The observable object.
     * @param arg The argument object being passed in.
     */
    @Override
    public void update(Observable o, Object arg) {
        this.BP.getChildren().clear();
        //this.BP = new BorderPane();
        makeBP();
        stage.sizeToScene();
    }

    /**
     * Adds the GUI components to the BorderPane attribute.
     */
    private void makeBP(){
        HBox center = new HBox();
        center.getChildren().addAll(makeLeft(), makeRight());
        center.setAlignment(Pos.CENTER);
        this.BP.setCenter(center);
    }

    /**
     * Creates the left portion of the overall GUI,
     * which is graphical representation of the safe itself.
     * @return Node Left portion of the GUI
     */
    private Node makeLeft(){
        char[][] grid = this.model.getSafe();
        for(int r = 0; r < this.model.getRownum(); r++){
            for (int c = 0; c < this.model.getColnum(); c++){
                char curr = grid[c][r];
                Button btn = makeSafeButtons(curr, c, r);
                btn.setOnAction(event -> {
                    int row = Safe.getRowIndex(btn);
                    int col = Safe.getColumnIndex(btn);
                    model.resetErrorRC();
                    //The following block if if/else statements prints out messages to the user based on where they click to add a laser.
                    if(this.model.isLaser(col,row)){
                        if(this.model.removeLaser(col,row)){
                            GUIMessage.setText("Laser removed at: ("+row+", "+col+")");
                        }
                        else {
                            GUIMessage.setText("Error removing laser at: ("+row+", "+col+")");
                            setButtonBackground(btn,"red.png");
                        }
                    }
                    else {
                        if (this.model.addLaser(col,row)){
                            GUIMessage.setText("Laser added at: ("+row+", "+col+")");
                        }
                        else {
                            GUIMessage.setText("Error adding laser at: ("+row+", "+col+")");
                            setButtonBackground(btn,"red.png");
                        }
                    }
                });
                Safe.add(btn, c, r);
            }
        }
        Safe.setAlignment(Pos.CENTER);
        Safe.setPadding(new Insets(10,10,10,10));
        Safe.setBackground(new Background( new BackgroundFill(Color.LIGHTGRAY,CornerRadii.EMPTY,Insets.EMPTY)));
        return Safe;
    }

    /**
     * Returns a button with the proper image as its background, which is determined by the char tile, i.e.
     * if tile = 'L' then the button has the image of a laser on it and so on.
     * @param tile char
     * @return button
     */
    private Button makeSafeButtons(char tile, int c, int r){
        Button btn = new Button();
        double prefSize = 50;
        btn.setMinSize(prefSize,prefSize);
        switch (tile){
            //If the button is being pointed to by the error coordinates, its background it set to red, indicating a check error.
            case 'L':
                btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/laser.png"))));
                if(c == model.getErrorC() && r == model.getErrorR()){
                    setButtonBackground(btn, "red.png");
                }
                else{
                    setButtonBackground(btn, "yellow.png");
                }
                break;
            case '*':
                btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/beam.png"))));
                if(c == model.getErrorC() && r == model.getErrorR()){
                    setButtonBackground(btn, "red.png");
                }
                else{
                    setButtonBackground(btn, "yellow.png");
                }
                break;
            case '0':
                btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/pillar0.png"))));
                if(c == model.getErrorC() && r == model.getErrorR()){
                    setButtonBackground(btn, "red.png");
                }
                break;
            case '1':
                btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/pillar1.png"))));
                if(c == model.getErrorC() && r == model.getErrorR()){
                    setButtonBackground(btn, "red.png");
                }
                break;
            case '2':
                btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/pillar2.png"))));
                if(c == model.getErrorC() && r == model.getErrorR()){
                    setButtonBackground(btn, "red.png");
                }
                break;
            case '3':
                btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/pillar3.png"))));
                if(c == model.getErrorC() && r == model.getErrorR()){
                    setButtonBackground(btn, "red.png");
                }
                break;
            case '4':
                btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/pillar4.png"))));
                if(c == model.getErrorC() && r == model.getErrorR()){
                    setButtonBackground(btn, "red.png");
                }
                break;
            case 'X':
                btn.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("resources/pillarX.png"))));
                break;
            default:
                if(c == model.getErrorC() && r == model.getErrorR()){
                    setButtonBackground(btn, "red.png");
                }
                break;
        }
        return btn;
    }

    /**
     * Creates the right portion of the overall GUI,
     * where the check, hint, solve, restart, and load buttons are located.
     * @return Node right portion of the GUI
     */
    private Node makeRight(){
        VBox menu = new VBox();

        Label gameState = this.GUIMessage;
        gameState.setAlignment(Pos.CENTER);

        //Check Button
        Button Check = new Button("Check");Check.setMinSize(60,40);
        Check.setOnAction(event -> {
            if(model.isValid()){
                GUIMessage.setText("Safe is fully verified!");
            }
            else {

                GUIMessage.setText("Error verifying at: ("+model.getR()+", "+model.getC()+")");
            }
        });

        //Hint Button
        Button Hint = new Button("Hint");Hint.setMinSize(60,40);
        Hint.setOnAction(event -> {
                if(model.ThreadRunning()){
                    GUIMessage.setText("Still Calculating Solution.");
                }
                else if(model.isGoal()){
                    GUIMessage.setText(model.getFilename() + " solved!");
                }
                else if(!model.SolutionIsPresent()){
                    GUIMessage.setText("No solution present.");
                }
                else{
                    try {
                        if(!model.runHint(model.getSolvedSafe())){
                            GUIMessage.setText("Hint: no next step");
                        }
                        else{
                            GUIMessage.setText("Hint: added laser to (" + model.getR() + "," + model.getC() + ")");
                        }

                    } catch (NullPointerException e){
                        System.out.println("array out of bound");
                    }
                }
        });

        //Solve Button
        Button Solve = new Button("Solve");Solve.setMinSize(60,40);
        Solve.setOnAction(event1 -> {
            if(model.ThreadRunning()){
                GUIMessage.setText("Still Calculating Solution.");
            }
            else if(model.SolutionIsPresent()){
                try {
                    model.solve();
                }catch (Exception e){
                    e.printStackTrace();
                }
                GUIMessage.setText(model.getFilename() + " solved!");
            }
            else {
                GUIMessage.setText(model.getFilename() + "\nHas No Solution.");
            }



        });

        //Restart Button
        Button Restart = new Button("Restart");Restart.setMinSize(60,40);
        Restart.setOnAction(event -> {
            model.restart();
            this.GUIMessage.setText(model.getFilename() + " loaded");
        });

        //Load Button
        Button Load = new Button("Load");Load.setMinSize(60,40);
        Load.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select safe file...");
            File file = fileChooser.showOpenDialog(new Stage());
            try {
                this.model.loadNewFile(file.getName());
            }catch (Exception e){
                return;
            }
            Safe.getChildren().clear();
            this.GUIMessage.setText(file.getName()+ " loaded");
            makeBP();
        });

        menu.getChildren().addAll(gameState, Check, Hint, Solve, Restart, Load);
        menu.setAlignment(Pos.CENTER);
        menu.setSpacing(10);
        menu.setPadding(new Insets(10,10,10,10));
        menu.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY,Insets.EMPTY)));

        return menu;
    }
}
