package views.viewing;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class View extends StackPane
{

    public final ViewController controller;
    public View()
    {
        super();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));
            StackPane displayer=null;
            try
            {
                displayer = loader.load();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

        if(displayer!=null)
        {
            controller = loader.getController();
            displayer.setAlignment(Pos.CENTER);
            this.getChildren().add(displayer);
        }
        else
        {
            controller = null;
        }
    }
}
