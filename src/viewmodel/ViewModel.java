package viewmodel;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.FGModel;
import model.Model;
import model.Painter;
import model.TimeSeries;
import utils.Calculate;
import utils.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

public class ViewModel extends Observable implements Observer
{

    Model m;
    TimeSeries anomalyTimeSeries,normalTimeSeries;
    Properties appProp;
    private HashMap<String, DoubleProperty> displayVariables;
    public IntegerProperty timeStep;
    public StringProperty csvPath,playSpeed, flightTime;
    public final Runnable onPlay,onStop,onPause,onFastForward, onSlowForward,onToStart,onToEnd;


    public ViewModel(Model m)
    {
        this.m = m;
        displayVariables = new HashMap<>();
        csvPath= new SimpleStringProperty();
        timeStep = new SimpleIntegerProperty();
        playSpeed = new SimpleStringProperty();
        flightTime = new SimpleStringProperty("00:00:00");
        m.setTimeStep(this.timeStep);
        appProp = m.getProperties();
        normalTimeSeries = new TimeSeries(appProp.getRegularFlightCSV());
        m.setRegularTimeSeries(normalTimeSeries);
        initDisplayVariables();

        csvPath.addListener((o,ov,nv)->{
            String res = this.m.uploadCsv(nv);
            if (res.equals("LoadedCSVSuccessfully"))
            {
                anomalyTimeSeries = new TimeSeries(nv);
                this.m.setAnomalyTimeSeries(anomalyTimeSeries);
            }
            setChanged();
            notifyObservers(res);
        });

        timeStep.addListener((o,ov,nv)->
        {
            ArrayList<Float> row = anomalyTimeSeries.getRow((Integer) nv);
            Platform.runLater(() -> {
                displayVariables.get("aileron").set(row.get(appProp.getMap().get("aileron").getIndex()));
                displayVariables.get("elevators").set(row.get(appProp.getMap().get("elevators").getIndex()));
                displayVariables.get("rudder").set(row.get(appProp.getMap().get("rudder").getIndex()));
                displayVariables.get("throttle").set(row.get(appProp.getMap().get("throttle").getIndex()));
                displayVariables.get("altitude").set(row.get(appProp.getMap().get("altitude").getIndex()));
                displayVariables.get("heading").set(-(row.get(appProp.getMap().get("heading").getIndex())));
                displayVariables.get("pitch").set(row.get(appProp.getMap().get("pitch").getIndex()));
                displayVariables.get("roll").set(-(row.get(appProp.getMap().get("roll").getIndex())));
                displayVariables.get("yaw").set(row.get(appProp.getMap().get("yaw").getIndex()));
                displayVariables.get("airspeed").set(row.get(appProp.getMap().get("airspeed").getIndex()));
                displayVariables.get("longitude").set(row.get(appProp.getMap().get("longitude").getIndex()));
                displayVariables.get("latitude").set(row.get(appProp.getMap().get("latitude").getIndex()));
                flightTime.set(updateFlightTime());
            });

        });

        onPlay = ()->m.play();
        onStop = ()->m.stop();
        onPause = ()->m.pause();
        onFastForward = ()->m.fastForward();
        onSlowForward = ()->m.slowForward();
        onToStart = ()->m.skipToStart();
        onToEnd = ()->m.skipToEnd();

    }
    public ObservableList<Float> getListItem(String feature, int oldVal,int newVal)
    {
        ObservableList<Float> listItem = FXCollections.observableArrayList(anomalyTimeSeries.getFeatureData(feature).subList(oldVal,newVal));
        return listItem;
    }

    public ObservableList<Float> getCorrelatedListItem(String selectedFeature, int oldVal, int newVal) {
        String corlFeature = getCorrelatedFeature(selectedFeature);
        ObservableList<Float> listItem = FXCollections.observableArrayList(anomalyTimeSeries.getFeatureData(corlFeature).subList(oldVal,newVal));
        return listItem;
    }

    public String getCorrelatedFeature(String feature)
    {
        return normalTimeSeries.getCorMap().get(feature).getCorFeature();
    }

    public String updateFlightTime()
    {
        int timeInSeconds = timeStep.get()/appProp.getHertzRate();
        return Calculate.getTimeString(timeInSeconds);
    }


    public DoubleProperty getProperty(String name){
        if(displayVariables.containsKey(name))
            return displayVariables.get(name);
        else return null;
    }

    private void initDisplayVariables(){
        if(displayVariables.keySet().size()!=0){
            displayVariables.clear();
        }
        for(String feature : appProp.getMap().keySet()){
            displayVariables.put(feature, new SimpleDoubleProperty());
        }
    }
    public void setAppProperties(String path)
    {
        m.setProperties(path);
    }

    public void setAlgorithm(String path)
    {
        m.setAnomalyDetector(path);
    }

    public ArrayList<String> getFeatures()
    {
        return anomalyTimeSeries.getFeatures();
    }

    public int getTsSize()
    {
        return anomalyTimeSeries.getRowSize()-1;
    }

    @Override
    public void update(Observable o, Object arg)
    {
        if(o.getClass().equals(FGModel.class)){

            switch (arg.toString())
            {
                case "FileNotFound":
                {
                    setChanged();
                    notifyObservers("FileNotFound");
                    break;
                }
                case "IllegalValues":
                {
                    setChanged();
                    notifyObservers("IllegalValues");
                    break;
                }
                case "XMLFormatDamaged":
                {
                    setChanged();
                    notifyObservers("XMLFormatDamaged");
                    break;
                }
                case "LoadedSuccessfully":
                {
                    setChanged();
                    notifyObservers("LoadedSuccessfully");
                    break;
                }
                case "FailedToLoadClass":
                {
                    setChanged();
                    notifyObservers("FailedToLoadClass");
                    break;
                }
                case "LoadedClassSuccessfully":
                {
                    setChanged();
                    notifyObservers("LoadedClassSuccessfully");
                    break;
                }
                case "SLOWEST":
                {
                    playSpeed.set("0.25");
                    break;
                }
                case "SLOWER":
                {
                    playSpeed.set("0.5");
                    break;
                }
                case "NORMAL":
                {
                    playSpeed.set("1.0");
                    break;
                }
                case "FASTER":
                {
                    playSpeed.set("2.0");
                    break;
                }
                case "FASTEST":
                {
                    playSpeed.set("4.0");
                    break;
                }
            }

            if(arg.getClass().equals(Properties.class))
            {

                appProp = (Properties) arg;
                normalTimeSeries = new TimeSeries(appProp.getRegularFlightCSV());
                m.setRegularTimeSeries(normalTimeSeries);
                initDisplayVariables();
            }

        }
    }

    public Painter getPainter()
    {
        return m.getPainter();
    }
    public void close()
    {
        m.close();
    }

}
