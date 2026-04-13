package pt.trasmum.loja.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        AppContext.initialize();
        Navigator.setStage(stage);
        stage.setTitle("TrasmUM — Software de Loja");
        stage.setMaximized(true);
        Navigator.navegarParaLogin();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
