package model;
public interface TimeSeriesAnomalyDetector
{
    void learnNormal(TimeSeries ts);
    void detect(TimeSeries ts);
    Painter getPainter();
}