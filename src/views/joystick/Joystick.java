package views.joystick;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
import java.io.IOException;

public class Joystick extends AnchorPane
{

    public DoubleProperty aileron,elevator,rudder,throttle;

    public Joystick()
    {
        super();
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Joystick.fxml"));
            AnchorPane joy = loader.load();
            JoystickController joystickController = loader.getController();
            joystickController.vertical.setShowTickMarks(true);
            joystickController.horizontal.setShowTickMarks(true);
            aileron = joystickController.joystick.xProperty();
            elevator = joystickController.joystick.yProperty();
            rudder = joystickController.horizontal.valueProperty();
            throttle = joystickController.vertical.valueProperty();
            this.getChildren().add(joy);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
