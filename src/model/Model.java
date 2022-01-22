package model;
import javafx.beans.property.IntegerProperty;
import other.Properties;

public interface Model
{

    void setRegularTimeSeries(TimeSeries ts);
    void setAnomalyTimeSeries(TimeSeries ts);
    void setProperties(String path);
    void setTimeStep(IntegerProperty timeStep);
    <V> Properties getProperties();
    void play();
    void pause();
    void stop();
    void skipToStart();
    void skipToEnd();
    void fastForward();
    void slowForward();
    String uploadCsv(String path);
    void setAnomalyDetector(String path);
    void close();
    Painter getPainter();

}
