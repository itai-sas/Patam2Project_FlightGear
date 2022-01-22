package views.joystick;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Slider;
import java.net.URL;
import java.util.ResourceBundle;

public class JoystickController implements Initializable
{
    @FXML
    SpaceXJoystick joystick;
    @FXML
    Slider vertical;
    @FXML
    Slider horizontal;

    public JoystickController() { }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        joystick.yProperty().addListener(e-> joystick.paint());
        joystick.xProperty().addListener(e-> joystick.paint());
        vertical.setMin(0);
        vertical.setMax(1);
        vertical.setBlockIncrement(0.1);
        vertical.setMajorTickUnit(0.1);
        vertical.setMinorTickCount(0);
        vertical.setShowTickMarks(true);
        horizontal.setMin(-1);
        horizontal.setMax(1);
        horizontal.setMajorTickUnit(0.1);
        horizontal.setBlockIncrement(0.1);
        horizontal.setShowTickMarks(true);
    }


}