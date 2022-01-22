package model;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import utils.Calculate;
/*import views.ApplicationStatus;*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ZscorePainter implements Painter
{
    LineChart myChart;
    TimeSeries normalTs,anomalyTs;
    HashMap<String, HashSet<Integer>> anomalyReports;
    HashMap<String,ArrayList<Float>> zArrAnomalyMap;
    HashMap<String,Float> thresholdMap;
    boolean init;
    XYChart.Series normalSeries;
    XYChart.Series anomalySeries;
    String currFeature;
    final CategoryAxis xAxis;
    final NumberAxis yAxis;

    public ZscorePainter()
    {
        currFeature="";
        normalSeries = new XYChart.Series();
        anomalySeries = new XYChart.Series();
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();
        myChart = new LineChart(xAxis,yAxis);
        myChart.setAnimated(false);
        myChart.setCreateSymbols(false);
        myChart.setLegendVisible(false);
    }

    @Override
    public void paint(StackPane pane, int oldTimeStep, int timeStep, String selectedFeature)
    {
        if(!init)
        {
            pane.getChildren().remove(0,pane.getChildren().size());
            myChart.getData().clear();
            pane.getChildren().add(myChart);
            init=true;
        }

        if(!currFeature.equals(selectedFeature))
        {
            updateGraph(myChart, timeStep, selectedFeature);
            currFeature = selectedFeature;
        }
        else{
            if(timeStep<=oldTimeStep)
            {
                updateGraph(myChart, timeStep, selectedFeature);
            }
            else {
                ObservableList<Float> points = FXCollections.observableArrayList(zArrAnomalyMap.get(selectedFeature).subList(oldTimeStep, timeStep));
                int len = points.size();
                int j = oldTimeStep;
                for (int i = 0; i < len; i++, j++)
                {
                    anomalySeries.getData().add(new XYChart.Data<>(Calculate.getTimeString(j / 10), points.get(i)));
                }
                checkAnomaly(timeStep, selectedFeature);
            }
        }
    }

    private void checkAnomaly(int timeStep, String selectedFeature)
    {
        if(anomalyReports.containsKey(selectedFeature))
        {
            if(anomalyReports.get(selectedFeature).contains(timeStep))
            {
                ApplicationStatus.setAppColor(Color.BLACK);
                ApplicationStatus.setAppFillColor("red");
                ApplicationStatus.setAppStatusValue("Anomaly has been detected in "+selectedFeature+" feature, at "+ Calculate.getTimeString(timeStep/10));
                ApplicationStatus.pausePlayFromStart();
            }
        }
    }

    private void updateGraph(LineChart chart, int timeStep, String selectedFeature)
    {
        normalSeries.getData().clear();
        anomalySeries.getData().clear();
        chart.getData().clear();
        Float threshold = thresholdMap.get(selectedFeature);
        int len = normalTs.getRowSize();
        for(int i=0;i<len;i++)
        {
            normalSeries.getData().add(new XYChart.Data<>(Calculate.getTimeString(i/10),threshold));
        }
        chart.getData().add(normalSeries);
        Node node =chart.lookup(".series0.chart-series-line");
        node.setStyle("-fx-stroke: grey");
        ObservableList<Float> points =  FXCollections.observableArrayList(zArrAnomalyMap.get(selectedFeature).subList(0,timeStep));
        for(int i=0;i<timeStep;i++)
        {
            anomalySeries.getData().add(new XYChart.Data<>(Calculate.getTimeString(i/10),points.get(i)));
        }
        chart.getData().add(anomalySeries);
        Node node2 =chart.lookup(".series1.chart-series-line");
        node2.setStyle("-fx-stroke: red");

        checkAnomaly(timeStep, selectedFeature);
    }

    @Override
    public void setAll(TimeSeries normalTs, TimeSeries anomalyTs, HashMap<String, HashSet<Integer>> anomalies)
    {
        this.normalTs = normalTs;
        this.anomalyTs = anomalyTs;
        this.anomalyReports = anomalies;
    }


}
