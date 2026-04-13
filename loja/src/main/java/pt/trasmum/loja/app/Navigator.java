package pt.trasmum.loja.app;

import javafx.beans.binding.Bindings;
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

    public static void setStage(Stage s) {
        stage = s;
    }

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
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar FXML: " + fxmlPath, e);
        }
    }

    private static void carregarCena(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxmlPath));
            Pane root = loader.load();

            // Aplica escala. O Group impede o JavaFX de redimensionar o root para
            // preencher a cena, preservando o layout no tamanho "virtual" correcto.
            aplicarEscala(root);
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            Group grupo = new Group(root);
            Scene scene = new Scene(grupo, bounds.getWidth(), bounds.getHeight());

            String css = Navigator.class.getResource("/styles/app.css") != null
                    ? Navigator.class.getResource("/styles/app.css").toExternalForm() : null;
            if (css != null) scene.getStylesheets().add(css);
            stage.setScene(scene);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar cena: " + fxmlPath, e);
        }
    }

    /**
     * Liga o prefWidth/prefHeight do painel ao inverso da escala e adiciona a
     * transformação Scale. Quando a escala muda, o layout é recalculado no tamanho
     * virtual menor e depois ampliado visualmente para preencher o ecrã.
     */
    private static void aplicarEscala(Pane root) {
        GestorEscala gestor = GestorEscala.getInstance();
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        root.prefWidthProperty().bind(Bindings.divide(bounds.getWidth(),  gestor.escalaProperty()));
        root.prefHeightProperty().bind(Bindings.divide(bounds.getHeight(), gestor.escalaProperty()));

        Scale s = new Scale();
        s.xProperty().bind(gestor.escalaProperty());
        s.yProperty().bind(gestor.escalaProperty());
        s.setPivotX(0);
        s.setPivotY(0);
        root.getTransforms().add(s);
    }
}
