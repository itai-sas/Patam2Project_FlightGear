package views.user;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class User extends AnchorPane
{
    public final UserController controller;
    public User()
    {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("user.fxml"));
        AnchorPane player=null;
        try {
            player = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(player!=null){
            controller = loader.getController();
            player.setLayoutX(180);
            this.getChildren().add(player);
        }
        else{
            controller = null;
        }
    }
}
