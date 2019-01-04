import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

// test commit
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("/dbManager.fxml"));
        primaryStage.setTitle("elsheepo's SQLite DB Manager");
        primaryStage.setScene(new Scene(root, 675, 455));

        primaryStage.setMinWidth(677);
        primaryStage.setMaxWidth(677);

        primaryStage.setMinHeight(447);
        primaryStage.setMaxHeight(447);

        primaryStage.show();
    }

    public static void main(String[] args) {launch(args);}
}
