package pt.trasmum.loja.app;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utilitário de navegação entre ecrãs.
 */
public class Navigator {

    private static Stage stage;
    private static BorderPane rootLayout;

    public static void setStage(Stage s) { stage = s; }
    public static Stage getStage()       { return stage; }

    public static void setRootLayout(BorderPane root) {
        rootLayout = root;
    }

    /** Substitui a cena inteira (usado em login/logout). */
    public static void navegarParaLogin() {
        carregarCena("/fxml/LoginView.fxml");
    }

    public static void navegarParaMain() {
        carregarCena("/fxml/MainView.fxml");
    }

    /** Substitui o centro do BorderPane principal (navegação interna). */
    public static void navegarParaCentro(String fxmlPath) {
        if (rootLayout == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxmlPath));
            Pane pane = loader.load();
            rootLayout.setCenter(pane);
        } catch (Exception e) {
            Throwable causa = e.getCause() != null ? e.getCause() : e;
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR);
            alert.initOwner(stage);
            alert.initModality(javafx.stage.Modality.WINDOW_MODAL);
            alert.setTitle("Erro de navegação");
            alert.setHeaderText("Não foi possível carregar a vista: " + fxmlPath);
            alert.setContentText(causa.getClass().getSimpleName() + ": " + causa.getMessage());
            alert.showAndWait();
        }
    }

    private static void carregarCena(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxmlPath));
            Pane root = loader.load();

            Group grupo = new Group(root);
            // Tamanho inicial baseado no ecrã primário; será corrigido dinamicamente
            // pela binding em aplicarEscala quando a stage maximizar em qualquer monitor.
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Scene scene = new Scene(grupo, bounds.getWidth(), bounds.getHeight());
            scene.setFill(javafx.scene.paint.Color.web("#f0f2f5"));

            aplicarEscala(root, scene);

            String css = Navigator.class.getResource("/styles/app.css") != null
                    ? Navigator.class.getResource("/styles/app.css").toExternalForm() : null;
            if (css != null) scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar cena: " + fxmlPath, e);
        }
    }

    /**
     * Vincula prefWidth/prefHeight do painel às dimensões reais da scene (não ao ecrã
     * primário), de modo a que qualquer redimensionamento da janela — incluindo
     * maximização num monitor diferente — seja reflectido correctamente.
     */
    private static void aplicarEscala(Pane root, Scene scene) {
        GestorEscala gestor = GestorEscala.getInstance();

        root.prefWidthProperty().bind(scene.widthProperty().divide(gestor.escalaProperty()));
        root.prefHeightProperty().bind(scene.heightProperty().divide(gestor.escalaProperty()));

        Scale s = new Scale();
        s.xProperty().bind(gestor.escalaProperty());
        s.yProperty().bind(gestor.escalaProperty());
        s.setPivotX(0);
        s.setPivotY(0);
        root.getTransforms().add(s);
    }
}
