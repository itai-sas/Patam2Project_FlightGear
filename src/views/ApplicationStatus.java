package views;

import javafx.animation.PauseTransition;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.util.Duration;


public final class ApplicationStatus
{
    private static Label appStatus = new Label();
    private static PauseTransition pause = new PauseTransition();

    public static void setPauseDuration(double duration){ pause.setDuration(Duration.seconds(duration)); }
    public static void setPauseOnFinished(EventHandler<ActionEvent> e){ pause.setOnFinished(e); }
    public static void pausePlayFromStart(){ pause.playFromStart(); }
    public static void setAppStatusValue(String status){ appStatus.textProperty().set(status);}
    public static StringProperty getAppStatusProp(){return appStatus.textProperty();}
    public static StringProperty getAppStyleProp(){return appStatus.styleProperty();}
    public static Label getAppStatus(){return appStatus;}
    public static void setAppColor(Color color){ appStatus.setTextFill(color); }
    public static void setAppFillColor(String color){ appStatus.setStyle("-fx-background-color: "+color); }


}
