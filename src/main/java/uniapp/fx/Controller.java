package uniapp.fx;

import javafx.stage.Stage;

/**
 * Base Controller class - All controllers extend this class
 * @param <M> Model type (generic)
 */
public abstract class Controller<M> {
    protected M model; //data types
    protected Stage stage; //current window
}

