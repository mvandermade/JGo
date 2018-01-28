package gui;

/**
 * Example on how to use the GOGUI
 *
 * @author Daan van Beek
 */
public class MainTester {

    public static void main(String[] args) {
        GoGUIIntegrator gogui = new GoGUIIntegrator(false, true, 9);
        gogui.startGUI();
        gogui.setBoardSize(9);

        gogui.addStone(0, 0, false);
        gogui.addStone(0, 2, true);
        gogui.addStone(0, 3, false);
        gogui.addStone(1, 1, true);
        gogui.addStone(1, 2, true);
        gogui.addStone(1, 3, false);
//        gogui.addStone(2, 0, true);
//        gogui.addStone(2, 1, true);
//        gogui.addStone(2, 2, false);
//        gogui.addStone(2, 7, false);
//        gogui.addStone(2, 8, false);
//        gogui.addStone(3, 1, false);
//        gogui.addStone(3, 2, false);
//        gogui.addStone(3, 3, false);
//        gogui.addStone(3, 4, false);
//        gogui.addStone(3, 5, false);
//        gogui.addStone(3, 6, false);
//        gogui.addStone(3, 7, false);
//        gogui.addStone(3, 8, true);
//        gogui.addStone(4, 0, false);
//        gogui.addStone(4, 1, false);
//        gogui.addStone(4, 7, true);
//        gogui.addStone(4, 8, true);
//        gogui.addStone(5, 0, false);
//        gogui.addStone(5, 1, true);
//        gogui.addStone(5, 2, true);
//        gogui.addStone(5, 3, true);
//        gogui.addStone(5, 4, true);
//        gogui.addStone(5, 5, true);
//        gogui.addStone(5, 6, true);
//        gogui.addStone(5, 7, true);
//        gogui.addStone(6, 0, true);
//        gogui.addStone(6, 1, true);
//        gogui.addStone(6, 6, true);
//        gogui.addStone(6, 7, false);
//        gogui.addStone(6, 8, false);
//        gogui.addStone(7, 5, true);
//        gogui.addStone(7, 6, false);
//        gogui.addStone(7, 7, false);
//        gogui.addStone(8, 5, true);
//        gogui.addStone(8, 6, false);
//        gogui.addStone(8, 8, false);

//        gogui.addAreaIndicator(0, 4, false);
//        gogui.addAreaIndicator(0, 5, false);
//        gogui.addAreaIndicator(0, 6, false);
//        gogui.addAreaIndicator(0, 7, false);
//        gogui.addAreaIndicator(0, 8, false);
//        gogui.addAreaIndicator(1, 4, false);
//        gogui.addAreaIndicator(1, 5, false);
//        gogui.addAreaIndicator(1, 6, false);
//        gogui.addAreaIndicator(1, 7, false);
//        gogui.addAreaIndicator(1, 8, false);
//        gogui.addAreaIndicator(2, 3, false);
//        gogui.addAreaIndicator(2, 4, false);
//        gogui.addAreaIndicator(2, 5, false);
//        gogui.addAreaIndicator(2, 6, false);
//
//        gogui.addAreaIndicator(6, 2, true);
//        gogui.addAreaIndicator(6, 3, true);
//        gogui.addAreaIndicator(6, 4, true);
//        gogui.addAreaIndicator(6, 5, true);
//        gogui.addAreaIndicator(7, 0, true);
//        gogui.addAreaIndicator(7, 1, true);
//        gogui.addAreaIndicator(7, 2, true);
//        gogui.addAreaIndicator(7, 3, true);
//        gogui.addAreaIndicator(7, 4, true);
//        gogui.addAreaIndicator(8, 0, true);
//        gogui.addAreaIndicator(8, 1, true);
//        gogui.addAreaIndicator(8, 2, true);
//        gogui.addAreaIndicator(8, 3, true);
//        gogui.addAreaIndicator(8, 4, true);
//
//        gogui.addHintIndicator(4, 4);
    }
}
