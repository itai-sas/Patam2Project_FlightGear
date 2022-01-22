package model;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import utils.Calculate;
import utils.CorrelatedFeatures;
import utils.Line;
/*
import views.ApplicationStatus;
*/
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


public class LinearRegPainter implements Painter
{
    LineChart myChart;
    TimeSeries normalTs,anomalyTs;
    HashMap<String, HashSet<Integer>> anomalyReports;
    ArrayList<CorrelatedFeatures> corFeatures;
    boolean init;
    XYChart.Series normalSeries,anomalySeries, lineSeries;
    String currFeature;
    final NumberAxis xAxis;
    final NumberAxis yAxis;
    public LinearRegPainter()
    {
       xAxis = new NumberAxis();
       yAxis = new NumberAxis();
       normalSeries = new XYChart.Series();
       anomalySeries = new XYChart.Series();
       lineSeries = new XYChart.Series();
       myChart = new LineChart(xAxis,yAxis);
       myChart.setAnimated(false);
       myChart.setLegendVisible(false);
       currFeature = "";
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
        boolean flag = false;
        String correlatedFeature = null;
        Line line = null;
        for(CorrelatedFeatures c : corFeatures)
        {
            if(c.feature1.equals(selectedFeature))
            {
                correlatedFeature=c.feature2;
                line = c.lineReg;
                flag = true;
            }
        }
        if(!flag)
        {
            ApplicationStatus.setAppColor(Color.BLACK);
            ApplicationStatus.setAppFillColor("orange");
            ApplicationStatus.setAppStatusValue(selectedFeature + " does not have a correlated feature that fits the Linear-Regression Algorithm");
            myChart.getData().clear();
            return;
        }
        else
        {
            ApplicationStatus.setAppStatusValue("");
            ApplicationStatus.setAppFillColor("transparent");
        }

        if(!currFeature.equals(selectedFeature))
        {
            updateGraph(timeStep, selectedFeature, correlatedFeature, line);
            currFeature = selectedFeature;
        }

        else
        {
            if(timeStep<=oldTimeStep)
            {
                updateGraph(timeStep, selectedFeature, correlatedFeature, line);
            }

            else
            {
                if(timeStep==oldTimeStep+1)
                {
                    Float x=anomalyTs.getFeatureData(selectedFeature).get(timeStep);
                    Float y=anomalyTs.getFeatureData(correlatedFeature).get(timeStep);
                    XYChart.Data dataPoint = new XYChart.Data<>(x,y);
                    anomalySeries.getData().add(dataPoint);
                    Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
                    lineSymbol.setStyle("-fx-background-color: red, red;-fx-background-radius: 3px;-fx-padding: 3px;");
                    if(timeStep>30){
                        anomalySeries.getData().remove(0);
                    }
                    checkAnomaly(timeStep,selectedFeature,correlatedFeature);
                }
                else
                {
                    updateGraph(timeStep, selectedFeature, correlatedFeature, line);
                }
            }
        }
    }

    private void updateGraph(int timeStep, String selectedFeature, String correlatedFeature, Line line)
    {
        myChart.getData().clear();
        normalSeries.getData().clear();
        anomalySeries.getData().clear();
        lineSeries.getData().clear();
        ArrayList<Float> xValues = normalTs.getFeatureData(selectedFeature);
        ArrayList<Float> yValues = normalTs.getFeatureData(correlatedFeature);
        int len=xValues.size();
        for(int i=0;i<len;i++)
        {
            normalSeries.getData().add(new XYChart.Data<>(xValues.get(i),yValues.get(i)));
        }
        myChart.getData().add(normalSeries);

        ObservableList<Float> pointsX,pointsY;
        if(timeStep>30)
        {
            pointsX = FXCollections.observableArrayList(anomalyTs.getFeatureData(selectedFeature).subList(timeStep-30,timeStep));
            pointsY = FXCollections.observableArrayList(anomalyTs.getFeatureData(correlatedFeature).subList(timeStep-30,timeStep));

        }
        else
        {
            pointsX = FXCollections.observableArrayList(anomalyTs.getFeatureData(selectedFeature).subList(0,timeStep));
            pointsY = FXCollections.observableArrayList(anomalyTs.getFeatureData(correlatedFeature).subList(0,timeStep));
        }
        len=pointsX.size();
        for(int i=0;i<len;i++)
        {
            anomalySeries.getData().add(new XYChart.Data<>(pointsX.get(i),pointsY.get(i)));
        }
        myChart.getData().add(anomalySeries);

        Float max = Collections.max(normalTs.getFeatureData(selectedFeature));
        Float min = Collections.min(normalTs.getFeatureData(selectedFeature));
        lineSeries.getData().add(new XYChart.Data<>(min,line.f(min)));
        lineSeries.getData().add(new XYChart.Data<>(max,line.f(max)));
        myChart.getData().add(lineSeries);
        Node node =myChart.lookup(".series0.chart-series-line");
        node.setStyle("-fx-stroke: transparent;");
        Node node1 =myChart.lookup(".series1.chart-series-line");
        node1.setStyle("-fx-stroke: transparent;");
        Node node2=myChart.lookup(".series2.chart-series-line");
        node2.setStyle("-fx-stroke: #000080;");
        
        for (int i = 0; i < anomalySeries.getData().size(); i++)
        {
            XYChart.Data dataPoint =  (Data) anomalySeries.getData().get(i);
            Node lineSymbol = dataPoint.getNode().lookup(".chart-line-symbol");
            lineSymbol.setStyle("-fx-background-color: red, red;-fx-background-radius: 3px;-fx-padding: 3px;");
        }
        for (int i = 0; i < normalSeries.getData().size(); i++)
        {
            XYChart.Data dataPoint1 = (Data) normalSeries.getData().get(i);
            Node lineSymbol1 = dataPoint1.getNode().lookup(".chart-line-symbol");
            lineSymbol1.setStyle("-fx-background-color: grey, grey;-fx-background-radius: 3px;-fx-padding: 3px;");
        }
        
        for (int i = 0; i < lineSeries.getData().size(); i++)
        {
            XYChart.Data dataPoint2 = (Data) lineSeries.getData().get(i);
            Node lineSymbol2 = dataPoint2.getNode().lookup(".chart-line-symbol");
            lineSymbol2.setStyle("-fx-background-color: transparent, transparent;");
        }

        checkAnomaly(timeStep,selectedFeature,correlatedFeature);
    }

    private void checkAnomaly(int timeStep, String selectedFeature,String correlatedFeature)
    {
        if(anomalyReports.containsKey(selectedFeature))
        {
            if(anomalyReports.get(selectedFeature).contains(timeStep))
            {
                ApplicationStatus.setAppColor(Color.BLACK);
                ApplicationStatus.setAppFillColor("red");
                ApplicationStatus.setAppStatusValue("Anomaly has been detected in ["+selectedFeature+"   ,   " + correlatedFeature+"] at " + Calculate.getTimeString(timeStep/10));
                ApplicationStatus.pausePlayFromStart();
            }
        }
    }

    @Override
    public void setAll(TimeSeries normalTs, TimeSeries anomalyTs, HashMap<String, HashSet<Integer>> anomalies)
    {
        this.normalTs = normalTs;
        this.anomalyTs = anomalyTs;
        this.anomalyReports = anomalies;
    }

}
