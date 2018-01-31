package gui;

import java.util.List;

import clientView.ClientTextInputPacket;
import javafx.application.Platform;

/**
 * Created by daan.vanbeek on 13-12-16.
 */
public class GoGUIIntegrator implements GOGUI {

    private GOGUIImpl wrappee;

    /**
     * Creates a GoGUIIntegrator that is capable of configuring and controlling the GO GUI.
     * @param showStartupAnimation if true then a startup animation will be shown
     *  when the GO GUI is started.
     * @param mode3D if true then the stones will be shown in 3D.
     *  Otherwise a 2D representation will be used.
     * @param boardSize the desired initial board size.
     */
    public GoGUIIntegrator(boolean showStartupAnimation, boolean mode3D, int boardSize) {
        createWrappedObject();
        wrappee.setShowStartupAnimation(showStartupAnimation);
        wrappee.setMode3D(mode3D);
        wrappee.setInitialBoardSize(boardSize);
    }
    
    public synchronized List<ClientTextInputPacket> getGuiClickResultToArray() {
    	
    	return wrappee.getGuiClickResultToArray();
    }

    @Override
    public synchronized void setBoardSize(int size) {
        Platform.runLater(() -> wrappee.setBoardSize(size));
    }

    @Override
    public synchronized void addStone(int x, int y, boolean white) {
    	// Fixed now for RowColwise
        Platform.runLater(() -> {
            try {
                wrappee.addStone(x, y, white);
            } catch (InvalidCoordinateException e) {
                e.printStackTrace();
            }
        });
    }
    
    public synchronized void addStoneRC(int row, int col, boolean white) {
    	// Fixed now for RowColwise
    	int x = col;
    	int y = row;
        Platform.runLater(() -> {
            try {
                wrappee.addStone(x, y, white);
            } catch (InvalidCoordinateException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public synchronized void removeStone(int x, int y) {
        Platform.runLater(() -> {
            try {
                wrappee.removeStone(x, y);
            } catch (InvalidCoordinateException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public synchronized void removeStoneRC(int row, int col) {
    	int x = col;
    	int y = row;
        Platform.runLater(() -> {
            try {
                wrappee.removeStone(x, y);
            } catch (InvalidCoordinateException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public synchronized void addAreaIndicator(int x, int y, boolean white) {
        Platform.runLater(() -> {
            try {
                wrappee.addAreaIndicator(x, y, white);
            } catch (InvalidCoordinateException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public synchronized void addHintIndicator(int x, int y) {
        Platform.runLater(() -> {
            try {
                wrappee.addHintIndicator(x, y);
            } catch (InvalidCoordinateException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public synchronized void removeHintIdicator() {
        Platform.runLater(() -> wrappee.removeHintIdicator());
    }

    @Override
    public synchronized void clearBoard() {
        Platform.runLater(() -> wrappee.clearBoard());
    }

    @Override
    public synchronized void startGUI() {
        startJavaFX();
        wrappee.waitForInitializationLatch();
        System.out.println("GO GUI was successfully started!");
    }
    
    @Override
    public synchronized void changeGuiTitle(String guiTitle) {
    	// To prevent main error
    	Platform.runLater(() -> wrappee.changeGuiTitle(guiTitle));
    }
    
    

    @Override
    public synchronized void stopGUI() {
        // Not implemented yet
    }

    private void createWrappedObject() {
        if (wrappee == null) {
            GOGUIImpl.startGUI();

            while (!GOGUIImpl.isInstanceAvailable()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            wrappee = GOGUIImpl.getInstance();
        }
    }

    private void startJavaFX() {
        createWrappedObject();
        wrappee.countDownConfigurationLatch();
    }
}
