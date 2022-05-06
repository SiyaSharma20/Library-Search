package cs1302.omega;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import java.util.ArrayList;
import javafx.geometry.Pos;
import java.io.*;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import java.util.List;
import javafx.geometry.HPos;
import javafx.scene.layout.Priority;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;


/**
 * This class extends the Application class. It is the main class which contains the
 * stage and the scene.
 */

public class OmegaApp extends Application {
    // Toolbar; container for search textbox and buttons
    ToolBar toolBar;

    // The container for progress bar in the bottom section
    HBox hBox;

    Scene scene;
    Stage stage;

    ProgressBar progressBar = new ProgressBar();

    // Button for getting data
    Button getData;

    // Button for exiting the application
    Button exitButton;

    // Textbox for Search Criterion
    TextField textField;

    // Grid for showing buttons carrying books cover page in the center of the borderpane
    GridPane grid = new GridPane();

    // centerBooksRunInfo container for information from openlibrary.org and booksrun.com APIs
    HBox centerBooksRunInfo = new HBox();
    TextArea areaOpenLibInfo = new TextArea("Information on the selected book -" +
        " From OpenLibrary.org API");
    TextArea areaBooksRunInfo = new TextArea("Resale Information on the selected book -" +
        " From BooksRun.com API");

    // Label for showing current status on search
    Label status = new Label("Type in a Title or Author of a book, " +
        "then click the \"Search\" button.");

    // OpenLibraty HttpClient search object
    HttpOpenLibClient httpLibClient = new HttpOpenLibClient();

    // BooksRun.com HttpClient search object
    HttpBooksRunClient httpBooksRunClient = new HttpBooksRunClient();

    // OpenLibrary HttpResult[] searchresults
    HttpResult[] iLibSearchResults;

    // Booksrun HttpResultBooksRun searchresult
    HttpResultBooksRun oBooksRunResult;

    // ArrayList of a ArrayList carrying buttons of cover pages of search results from APIs
    private final List<List<Button>> list = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {

        try {
            // Borderpane framework for the main screen
            BorderPane border = new BorderPane();

            // vbox_top to set it up at the top of the borderpane
            VBox vbox_top = addToolBar();
            border.setTop(vbox_top);

            // hbox_bottom to setup progressbar at the bottom of the borderpane
            HBox hbox_bottom = addProgressBar();
            border.setBottom(hbox_bottom);

            // center part of the borderpane is stacked up;
            // grid and lower part for showing results from openlibrary.org and booksrun.com APIs
            VBox center = new VBox();

            center.setFillWidth(true);

            HBox.setHgrow(centerBooksRunInfo, Priority.ALWAYS);
            centerBooksRunInfo.setVisible(false);
            centerBooksRunInfo.setSpacing(20);
            centerBooksRunInfo.setPadding(new Insets(20, 50, 50, 60));
            HBox.setHgrow(areaOpenLibInfo, Priority.ALWAYS);
            HBox.setHgrow(areaBooksRunInfo, Priority.ALWAYS);
            areaOpenLibInfo.setStyle("-fx-font: 16 arial;");
            areaBooksRunInfo.setStyle("-fx-font: 16 arial;");

            // setting up both components as children of centerBooksRunInfo component
            centerBooksRunInfo.getChildren().addAll(areaOpenLibInfo, areaBooksRunInfo);

            //setting up grid and centerBooksRunInfo as components of center vbox
            center.getChildren().addAll(grid, centerBooksRunInfo);

            // setting up center vbox as center component of borderpane
            border.setCenter(center);

            // Creates a seperate thread to populate the search results
            //if iLibSearchResults.length > 0
            Thread task = new Thread(() -> {

                Platform.runLater(() -> {
                    if (iLibSearchResults != null && iLibSearchResults.length > 0) {
                        updateGridPane();
                    }
                });
            });
            task.setDaemon(true);
            task.start();
            scene = new Scene(border);
            stage = new Stage();
            stage.setTitle("Books Search");
            stage.setScene(scene);
            stage.setMaximized(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method returns a Button component stored in
     * ArrayList of ArrayList called list.
     * @param r int
     * @param c int
     * @return Button
     */
    private Button getGridButton(int r, int c) {
        return list.get(r).get(c);
    }

    /**
     * This method creates and returns a button based on column and row along with url
     * of the cover page jpg returned from API search.
     * @param col
     * @param row
     * @param urlString
     * @return Button
     */
    private Button createGridButton(int col, int row, String urlString) {

        Button button;
        if (urlString.contains("null")) {
            button = new Button("Image Unavailable");
        } else {
            button = new Button();
        }
        // receive byte[] from httpLibClient object based on urlstring of the jpg file
        byte[] byteArr = httpLibClient.readCoverImg(urlString);
        Image image = new Image(new ByteArrayInputStream(byteArr));
        button.setMaxSize(155, 100);
        BackgroundImage bImage = new BackgroundImage(image,
            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
            BackgroundPosition.CENTER,
            new BackgroundSize(button.getWidth(), button.getHeight(), true, true, true, false));
        Background backGround = new Background(bImage);
        // adds background image to the button based on url sent
        button.setBackground(backGround);

        // adds a click event handler to pass index of the clicked button to
        // pass ISBN of the selected cover page to booksrunclient object to
        // get information of resale value of the book.
        // BooksRun.com site may not be buying everybook that you select and results of
        // $0 value will suggest that
        button.setOnAction((ActionEvent event) -> {
            if (event.getSource() == getGridButton(row, col)) {

                // Remove highlight border from all the buttons before selected
                //button is highlighted
                for (int i = 0; i < list.size(); i++) {
                    for (int j = 0; j < list.get(i).size(); j++) {
                        getGridButton(i, j).setStyle("-fx-border-color: transparent");
                    }
                }
                getGridButton(row, col).setStyle("-fx-border-color: red");
                Thread task = new Thread(() -> {
                    try {
                        int index = ((row + 1) * (col + 1) - 1);
                        // httpBooksRunClient object is passed with the selected
                        //ISBN to get resale value from its APIs
                        oBooksRunResult = httpBooksRunClient.getResults
                            (iLibSearchResults[index].getIsbn());
                        Platform.runLater(() -> {
                            // This would update lower part of the center frame with
                            //updated values from booksrun APIs on resale values  of the book if any
                            updateBooksRunPanel(index);
                        });
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                });
                task.setDaemon(true);
                task.start();
            }
        });
        return button;
    } //createGridButton

    /**
     *This method calls createToolBarParts function that create search components
     * and creates action handler on getData Button.
     * @return Vbox
     */
    public VBox addToolBar() {
        VBox vbox = createToolBarParts();

        // getData button handler
        getData.setOnAction(e -> {
            cleanGrid();

            // starts ProgressBar on INDETERMINATE_PROGRESS setting
            Platform.runLater(() -> {
                progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                status.setText("Getting data " + httpLibClient.getURI());
            });

            // Starts a new search thread to get results from httpLibClient object
            Thread task = new Thread(() -> {
                try {
                    iLibSearchResults = httpLibClient.getResults(textField.getText());
                    Platform.runLater(() -> {
                        // upon receiving the results it trigers update of the tile/grid pane
                        updateGridPane();
                    });
                } catch (IOException e1) {
                    e1.printStackTrace();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            });
            task.setDaemon(true);
            task.start();

        });
        return vbox;
    } //addToolBar


    /**
     * This method creates parts that are used in the header bar.
     * @return VBox
     */
    public VBox createToolBarParts() {
        // Creates tool bar and adds buttons and text field
        VBox vbox = new VBox();
        toolBar = new ToolBar();
        toolBar.setStyle("-fx-padding: 20px; -fx-spacing: 50px;");

        // Create label for search text field
        Label searchQuery = new Label("Search millons of books on " +
            "https://openlibrary.org: (on Author and Topic) ");
        searchQuery.setStyle("-fx-font: 18 arial; ");
        searchQuery.setTextFill(Color.BLUE);

        // Create search text field
        textField = new TextField("the lord of the rings");
        textField.setAlignment(Pos.CENTER_LEFT);// Align text to center
        textField.setPrefWidth(250);// Set width
        textField.setPrefHeight(20);// Set width
        textField.setStyle("-fx-font: 16 arial; ");

        // Create button to get data
        getData = new Button("Search");
        getData.setStyle("-fx-font: 16 arial;");
        getData.setTextFill(Color.BLUE);
        getData.setPrefHeight(25);
        getData.setPrefWidth(100);

        // Create button to exit application
        exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font: 16 arial;");
        exitButton.setTextFill(Color.RED);
        exitButton.setPrefHeight(25);
        exitButton.setPrefWidth(100);
        exitButton.setOnAction((ActionEvent event) -> {
            Platform.exit();
        });

        status.setStyle("-fx-font: 15 arial; ");
        status.setTextFill(Color.RED);

        // Add label, text box, search and exit button to toolbar
        toolBar.getItems().addAll(searchQuery, textField, getData, exitButton);

        VBox.setMargin(status, new Insets(5, 10, 5, 10));
        // return vbox after adding toolbar and status label to be attached to the
        //top of the boderpane
        vbox.getChildren().addAll(toolBar, status);

        return vbox;
    } //createToolBarParts


    /**
     * This method returns a hbox with a progressbar and label attached to it.
     * @return HBox
     */
    public HBox addProgressBar() {
        hBox = new HBox();
        progressBar.setLayoutX(25.0);
        progressBar.setLayoutY(550.0);

        Label label = new Label("Images provided by OpenLibrary.org Search API.");


        progressBar.setProgress(0);
        HBox.setMargin(progressBar, new Insets(10, 10, 10, 10));
        HBox.setMargin(label, new Insets(10, 10, 10, 10));
        progressBar.prefWidthProperty().bind(hBox.widthProperty().subtract(300));

        hBox.getChildren().addAll(progressBar, label);
        return hBox;
    } //addProgressBar

    /**
     * This method returns an updated grid with cover pages of the books
     * returned by APIs or appropriate error message if no results found.
     * @return GridPane
     */
    public GridPane updateGridPane() {
        if (iLibSearchResults == null || iLibSearchResults.length == 0) {
            progressBar.setProgress(0);

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(
                "Search failed to meet the criterion. No Records found. " +
                "Please try searching another book.");
            status.setText("Last attempt to get images failed...");
            alert.showAndWait();
        } else {
            populatedGrid(iLibSearchResults);
        }
        return grid;
    } //updateGridPane

    /**
     * This method calls createButton method to populate grid with books returned by API
     * after cleaning it up first.
     * @param iLibSearchResults
     */
    private void populatedGrid(HttpResult[] iLibSearchResults) {

        grid.setPrefHeight(400);
        grid.setPrefWidth(500);
        int numberCol = 5;
        int numberRow = iLibSearchResults.length / numberCol;

        list.removeAll(list);

        progressBar.setProgress(0);
        for (int row = 0; row < numberRow; row++) {
            list.add(new ArrayList<>());

            for (int col = 0; col < numberCol; col++) {
                int index = ((row + 1) * (col + 1) - 1);
                Button gb = createGridButton(col, row, iLibSearchResults[index].getPicUrl());
                gb.setStyle(null);
                list.get(row).add(gb);
                grid.add(gb, col, row);

                GridPane.setHalignment(gb, HPos.CENTER);
                GridPane.setHgrow(gb, Priority.ALWAYS);
                GridPane.setValignment(gb, VPos.CENTER);
                GridPane.setVgrow(gb, Priority.ALWAYS);
            }

        }
        getGridButton(0,0).fire();
        status.setText("Click on the different book covers below to get more information " +
            "and book resale prices from BooksRun.com");
        progressBar.setProgress(100);

    } //populatedGrid

    /**
     * This method removes all the grid nodes and hides container carrying information
     * on selected book.
     */
    private void cleanGrid() {
        grid.getChildren().clear();
        list.clear();
        centerBooksRunInfo.setVisible(false);
        progressBar.setProgress(0);

    } //cleanGrid

    /**
     * This method populates information returned from openlib.org and booksrun.com
     * and sets container visible.
     * @param index
    */
    public void updateBooksRunPanel(Integer index) {

        areaOpenLibInfo.setText("Information on the selected book - From OpenLibrary.org API\n");

        areaOpenLibInfo.appendText("Title: " + iLibSearchResults[index].getTitle() + "\n");
        areaOpenLibInfo.appendText("Suggested Title: " + iLibSearchResults[index].getTitleSuggest()
            + "\n");
        areaOpenLibInfo.appendText("ISBN: " + iLibSearchResults[index].getIsbn() + "\n");

        areaBooksRunInfo.setText("Resale Information based on the quality of" +
            " the selected book - From BooksRun.com API\n");
        areaBooksRunInfo.appendText("If price is $0, BooksRun.com is currently not buying " +
            "selected book from users\n");
        areaBooksRunInfo.appendText("Status: " + oBooksRunResult.result.status + "\n");

        areaBooksRunInfo.appendText("Info: " +
            oBooksRunResult.result.text.toString().replace("=", " = $") + "\n");
        centerBooksRunInfo.setVisible(true);

    } //updateBooksRunPanel


    public static void main(String[] args) {
        launch(args);
    }


}
