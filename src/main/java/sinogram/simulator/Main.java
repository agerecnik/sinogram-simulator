package sinogram.simulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/SimulatorWindow.fxml"));
        Parent root = loader.load();
        SimulatorWindowController controller = loader.getController();

        stage.setTitle("Sinogram Simulator");
        stage.setScene(new Scene(root));
        stage.setHeight(600);
        stage.setWidth(1300);
        stage.setResizable(true);
        stage.setOnCloseRequest(event -> {
            controller.shutdownExecutor();
        });
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

