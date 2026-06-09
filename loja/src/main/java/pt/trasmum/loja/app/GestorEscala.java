package pt.trasmum.loja.app;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.stage.Screen;
import java.util.prefs.Preferences;

/**
 * Gere o factor de escala da UI. Persiste a preferência entre sessões via
 * java.util.prefs.Preferences (registo do utilizador do SO, independente de ficheiros).
 * Na primeira execução (sem preferência guardada) calcula automaticamente a escala
 * com base no DPI do ecrã primário (referência: 96 DPI → 1.0×).
 */
public class GestorEscala {

    private static final String PREF_KEY  = "ui.scale";
    private static final double MIN       = 0.5;
    private static final double MAX       = 3.0;
    private static final double PASSO     = 0.25;
    private static final double DPI_BASE  = 96.0;

    private static final GestorEscala INSTANCIA = new GestorEscala();

    private final Preferences   prefs  = Preferences.userNodeForPackage(GestorEscala.class);
    private final DoubleProperty escala = new SimpleDoubleProperty(prefs.getDouble(PREF_KEY, escalaAutomatica()));

    private GestorEscala() {
        escala.addListener((obs, ant, novo) -> prefs.putDouble(PREF_KEY, novo.doubleValue()));
    }

    public static GestorEscala getInstance() { return INSTANCIA; }

    public DoubleProperty escalaProperty() { return escala; }
    public double         getEscala()      { return escala.get(); }

    public void aumentar() { if (escala.get() < MAX) escala.set(escala.get() + PASSO); }
    public void diminuir() { if (escala.get() > MIN) escala.set(escala.get() - PASSO); }

    /** Repõe a escala automática (baseada em DPI) e apaga a preferência guardada. */
    public void repor() {
        prefs.remove(PREF_KEY);
        escala.set(escalaAutomatica());
    }

    private static double escalaAutomatica() {
        double dpi = Screen.getPrimary().getDpi();
        double raw = dpi / DPI_BASE;
        double arredondado = Math.round(raw / PASSO) * PASSO;
        return Math.max(MIN, Math.min(MAX, arredondado));
    }
}
