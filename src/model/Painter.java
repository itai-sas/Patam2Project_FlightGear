package model;
import javafx.scene.layout.StackPane;
import java.util.HashMap;
import java.util.HashSet;

public interface Painter
{
    void paint(StackPane stackPane, int oldTimeStep, int timeStep, String selectedFeature);
    void setAll(TimeSeries normalTs, TimeSeries anomalyTs, HashMap<String, HashSet<Integer>> anomalies);
}
