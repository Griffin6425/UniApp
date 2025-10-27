package uniapp.fx;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * ViewLoader - Responsible for loading FXML files and displaying windows
 */

public class ViewLoader {
    /**
     * Load FXML and display window
     * @param model The data model to pass to the controller
     * @param fxml FXML file path
     * @param title Window title
     * @param stage The stage to display
     */
    public static <T> void showStage(T model, String fxml, String title, Stage stage) throws IOException {
        //Create FXML loader with custom controller factory
        FXMLLoader loader = new FXMLLoader(
            Controller.class.getResource(fxml),
            null,
            null,
            type -> {
                try {
                    //Create controller instance
                    @SuppressWarnings("unchecked")
                    Controller<T> controller = (Controller<T>) type.newInstance();
                    //Inject model and stage
                    controller.model = model;
                    controller.stage = stage;
                    return controller;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        );

        //Load FXML file
        Parent root = loader.load();

        //Setup window
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.sizeToScene();
        stage.show();
    }

    public static <T> void showStage(T model, String fxml, String title, Stage stage, java.util.function.Supplier<Controller<T>> controllerFactory) throws IOException {
        //Create FXML loader with custom controller factory
        FXMLLoader loader = new FXMLLoader(
            Controller.class.getResource(fxml),
            null,
            null,
            type -> {
                // Use provided factory to create controller
                Controller<T> controller = controllerFactory.get();

                //Inject model and stage
                controller.model = model;
                controller.stage = stage;

                return controller;
            }
        );

        // Load FXML file
        Parent root = loader.load();

        // Setup window
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.sizeToScene();
        stage.show();
    }
}
