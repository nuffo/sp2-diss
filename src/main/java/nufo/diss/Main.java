package nufo.diss;

public class Main {
    public static void main(String[] args) {
        GUI gui = new GUI();
        Controller controller = new Controller(gui);
        gui.setController(controller);
    }
}