package views;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import model.Painter;
import views.clocks.Clocks;
import views.viewing.View;
import views.joystick.Joystick;
import views.user.User;
import viewmodel.ViewModel;

import java.io.File;
import java.util.Observable;
import java.util.Observer;


public class MainWindowController implements Observer {

    private ViewModel vm;
    private Stage stage;
    private Painter painter;
    private @FXML User myPlayer;
    private @FXML Joystick myJoystick;
    private @FXML Label appStatus;
    private @FXML Clocks myClocks;
    private @FXML View myDisplay;
    private @FXML MenuItem loadAlgorithm;
    private String currentFeature;
    private BooleanProperty isPlayed;

    public void loadProperties(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Load Project Properties");
        fc.setInitialDirectory(new File("./Sources"));
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(
                "XML Files (*.xml)", "*.xml");
        fc.getExtensionFilters().add(extensionFilter);
        File chosenFile = fc.showOpenDialog(stage);

        if(chosenFile!=null){
            vm.setAppProperties(chosenFile.getAbsolutePath());
        }
    }

    public void loadAlgorithm(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Load Detection Algorithm");
        fc.setInitialDirectory(new File("./algorithms"));
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(
                "Class Files (*.class)", "*.class");
        fc.getExtensionFilters().add(extensionFilter);
        File chosenFile = fc.showOpenDialog(stage);

        if(chosenFile!=null){
            vm.setAlgorithm(chosenFile.getAbsolutePath());
        }
    }

    public void initialize(ViewModel vm) {
        this.vm = vm;
        currentFeature = "";
        isPlayed = new SimpleBooleanProperty();
        loadAlgorithm.setDisable(true);
        appStatus.textProperty().bindBidirectional(ApplicationStatus.getAppStatusProp());
        appStatus.textFillProperty().bindBidirectional(ApplicationStatus.getAppStatus().textFillProperty());
        appStatus.styleProperty().bindBidirectional(ApplicationStatus.getAppStyleProp());
        ApplicationStatus.setPauseDuration(15);
        ApplicationStatus.setPauseOnFinished(event-> {
            appStatus.setText("");
            appStatus.setStyle("");
            ApplicationStatus.setAppFillColor("transparent");
        });

        vm.csvPath.bindBidirectional(myPlayer.controller.timeSeriesPath);

        myPlayer.controller.onPlay = vm.onPlay;
        myPlayer.controller.onStop = vm.onStop;
        myPlayer.controller.onPause = vm.onPause;
        myPlayer.controller.onFastForward = vm.onFastForward;
        myPlayer.controller.onSlowForward =vm.onSlowForward;
        myPlayer.controller.onToEnd = vm.onToEnd;
        myPlayer.controller.onToStart = vm.onToStart;
        myPlayer.controller.flightTime.textProperty().bind(vm.flightTime);
        myPlayer.controller.slider.valueProperty().bindBidirectional(vm.timeStep);
        myPlayer.controller.playSpeed.textProperty().bindBidirectional(vm.playSpeed);
        this.isPlayed.bind(myPlayer.controller.isPlayed);

        myJoystick.aileron.bindBidirectional(vm.getProperty("aileron"));
        myJoystick.elevator.bindBidirectional(vm.getProperty("elevators"));
        myJoystick.rudder.bind(vm.getProperty("rudder"));
        myJoystick.throttle.bind(vm.getProperty("throttle"));

        myClocks.headingDeg.bind(vm.getProperty("heading"));
        myClocks.pitch.bind(vm.getProperty("pitch"));
        myClocks.roll.bind(vm.getProperty("roll"));
        myClocks.altimeter.bind(vm.getProperty("altitude"));
        myClocks.yaw.bind(vm.getProperty("yaw"));
        myClocks.airspeed.bind(vm.getProperty("airspeed"));
        registerListeners();

    }

    private void registerListeners(){
        //Time-Step Listener
        vm.timeStep.addListener((o,ov,nv)->{
            int timeStep= vm.timeStep.get();
            if(myDisplay.controller.list.getSelectionModel().getSelectedItem()!=null) {
                String selectedFeature = myDisplay.controller.list.getSelectionModel().getSelectedItem().toString();
                ObservableList<Float> leftListItem,rightListItem;
                //new Time-Step <= old Time-Step
                if(nv.intValue()<=ov.intValue()||!currentFeature.equals(selectedFeature)){
                    leftListItem= vm.getListItem(selectedFeature,0,timeStep);
                    rightListItem = vm.getCorrelatedListItem(selectedFeature,0,timeStep);
                    Platform.runLater(()->myDisplay.controller.display(leftListItem,rightListItem));
                }
                else {
                    leftListItem= vm.getListItem(selectedFeature, ov.intValue(), nv.intValue());
                    rightListItem = vm.getCorrelatedListItem(selectedFeature,ov.intValue(), nv.intValue());
                    Platform.runLater(() -> myDisplay.controller.updateDisplay(leftListItem,rightListItem, ov.intValue()));
                }
                if(painter!=null){
                    Platform.runLater(()->painter.paint(myDisplay.controller.stackPane, ov.intValue(), nv.intValue(), selectedFeature));
                }
                currentFeature = selectedFeature;
            }
        });

        //ListItem Listener
        myDisplay.controller.list.getSelectionModel().selectedItemProperty().addListener((o,ov,nv)->{
            if(nv!=null) {
                myDisplay.controller.leftGraph.setTitle(nv.toString());
                myDisplay.controller.rightGraph.setTitle(vm.getCorrelatedFeature(nv.toString()));
                if(!isPlayed.get()){
                    ObservableList<Float> leftListItem = vm.getListItem(nv.toString(), 0, vm.timeStep.get());
                    ObservableList<Float> rightListItem = vm.getCorrelatedListItem(nv.toString(), 0, vm.timeStep.get());
                    Platform.runLater(() -> myDisplay.controller.display(leftListItem,rightListItem));
                    if(painter!=null) {
                        Platform.runLater(() -> painter.paint(myDisplay.controller.stackPane, 0, vm.timeStep.get(), nv.toString()));
                    }
                    currentFeature="";
                }
            }
        });

        stage.setOnCloseRequest(e->vm.close());

    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o.getClass().equals(ViewModel.class)){
            switch (arg.toString()) {
                case "FileNotFound": {
                    setButtonsDisabled();
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.RED);
                    ApplicationStatus.setAppStatusValue("File not found");
                    ApplicationStatus.pausePlayFromStart();
                    vm.csvPath.set("");
                    break;
                }
                case "IllegalValues": {
                    setButtonsDisabled();
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.RED);
                    ApplicationStatus.setAppStatusValue("Data is missing or invalid");
                    ApplicationStatus.pausePlayFromStart();
                    vm.csvPath.set("");
                    break;
                }
                case "XMLFormatDamaged": {
                    setButtonsDisabled();
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.RED);
                    ApplicationStatus.setAppStatusValue("XML Format is damaged");
                    ApplicationStatus.pausePlayFromStart();
                    vm.csvPath.set("");
                    break;
                }

                case "LoadedSuccessfully":{
                    setButtonsDisabled();
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.GREEN);
                    ApplicationStatus.setAppStatusValue("Properties resource has been loaded successfully");
                    ApplicationStatus.pausePlayFromStart();
                    vm.csvPath.set("");
                    break;
                }
                case "LoadedCSVSuccessfully":{
                    setButtonsDisabled();
                    ApplicationStatus.setAppColor(Color.GREEN);
                    ApplicationStatus.setAppStatusValue("CSV-File has been loaded successfully");
                    ApplicationStatus.pausePlayFromStart();
                    assert myDisplay.controller != null;
                    myDisplay.controller.list.getItems().setAll(vm.getFeatures());
                    setButtonsEnabled();
                    assert myPlayer.controller != null;
                    myPlayer.controller.slider.setMin(0);
                    myPlayer.controller.slider.setMax(vm.getTsSize());
                    myPlayer.controller.slider.setBlockIncrement(1);
                    myPlayer.controller.slider.setMajorTickUnit(1);
                    myPlayer.controller.slider.setMinorTickCount(0);
                    myPlayer.controller.slider.setSnapToTicks(true);
                    break;
                }
                case "missingProperties":{
                    setButtonsDisabled();
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.RED);
                    ApplicationStatus.setAppStatusValue("CSV-File is missing properties");
                    ApplicationStatus.pausePlayFromStart();
                    vm.csvPath.set("");
                    break;
                }
                case "incorrectFormat": {
                    setButtonsDisabled();
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.RED);
                    ApplicationStatus.setAppStatusValue("Incorrect CSV-File format");
                    ApplicationStatus.pausePlayFromStart();
                    vm.csvPath.set("");
                    break;
                }
                case "dataOutOfRange":{
                    setButtonsDisabled();
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.RED);
                    ApplicationStatus.setAppStatusValue("One or more data values is out of feature's legal range");
                    ApplicationStatus.pausePlayFromStart();
                    vm.csvPath.set("");
                    break;
                }

                case "doubleFeature":{
                    setButtonsDisabled();
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.RED);
                    ApplicationStatus.setAppStatusValue("CSV-File cannot contain columns with the same name");
                    ApplicationStatus.pausePlayFromStart();
                    vm.csvPath.set("");
                    break;
                }
                case "LoadedClassSuccessfully":{
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.GREEN);
                    ApplicationStatus.setAppStatusValue("Class-File has been loaded successfully");
                    ApplicationStatus.pausePlayFromStart();
                    painter = vm.getPainter();
                    break;
                }
                case "FailedToLoadClass":{
                    ApplicationStatus.setAppFillColor("transparent");
                    ApplicationStatus.setAppColor(Color.RED);
                    ApplicationStatus.setAppStatusValue("Failed to load the class file");
                    ApplicationStatus.pausePlayFromStart();
                    break;
                }
            }
        }
    }

    private void setButtonsDisabled(){
        vm.onStop.run();
        myPlayer.controller.slider.valueProperty().setValue(0);
        loadAlgorithm.setDisable(true);

        assert myPlayer.controller != null;
        myPlayer.controller.slider.setDisable(true);
        myPlayer.controller.play.setDisable(true);
        myPlayer.controller.stop.setDisable(true);
        myPlayer.controller.pause.setDisable(true);
        myPlayer.controller.fastForward.setDisable(true);
        myPlayer.controller.slowForward.setDisable(true);
        myPlayer.controller.toEnd.setDisable(true);
        myPlayer.controller.toStart.setDisable(true);
        myPlayer.controller.playSpeed.clear();
        vm.flightTime.set("00:00:00");

        assert myDisplay.controller != null;
        myDisplay.controller.list.getItems().clear();
        myDisplay.controller.leftGraph.getData().clear();
        myDisplay.controller.rightGraph.getData().clear();
        myDisplay.controller.leftGraph.setTitle("Feature");
        myDisplay.controller.rightGraph.setTitle("Correlated Feature");
        myDisplay.controller.leftGraph.setStyle("-fx-font-size: 12px");
        myDisplay.controller.rightGraph.setStyle("-fx-font-size: 12px");
        if(myDisplay.controller.stackPane.getChildren().size()>0){
            myDisplay.controller.stackPane.getChildren().remove(0,myDisplay.controller.stackPane.getChildren().size());
        }

        ApplicationStatus.setAppFillColor("transparent");
        ApplicationStatus.setAppStatusValue("");
    }

    private void setButtonsEnabled(){
        assert myPlayer.controller != null;
        myPlayer.controller.slider.setDisable(false);
        myPlayer.controller.play.setDisable(false);
        myPlayer.controller.stop.setDisable(false);
        myPlayer.controller.pause.setDisable(false);
        myPlayer.controller.fastForward.setDisable(false);
        myPlayer.controller.slowForward.setDisable(false);
        myPlayer.controller.toEnd.setDisable(false);
        myPlayer.controller.toStart.setDisable(false);
        myPlayer.controller.playSpeed.setText("1.0");
        loadAlgorithm.setDisable(false);
        myDisplay.controller.leftGraph.setStyle("-fx-font-size: 8.5px");
        myDisplay.controller.rightGraph.setStyle("-fx-font-size: 8.5px");
    }

    public void close() {
        vm.close();
        stage.close();
    }
}
