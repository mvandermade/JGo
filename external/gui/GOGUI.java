package gui;

/**
 * Created by daan.vanbeek on 13-12-16.
 */
public interface GOGUI {

    /**
     * Sets the board width and height to the given value. Adjusts the window size accordingly.
     * Re-initialises the board, note that the existing stone configuration will be lost.
     * @param size the desired width and height of the board.
     * @throws InvalidCoordinateException when x or y coordinate fall outside of the board.
     */
    void setBoardSize(int size) throws InvalidCoordinateException;

    /**
     * Adds a new stone to the board of the given type and at the given position.
     * Removes any existing stone/area indicator at the given position.
     * @param x the x coordinate of the new stone, ranges from 0 to boardSize - 1.
     * @param y the y coordinate of the new stone, ranges from 0 to boardSize - 1.
     * @param white if true then a white stone will be added, otherwise a black stone will be added
     * @throws InvalidCoordinateException when x or y coordinate fall outside of the board.
     */
    void addStone(int x, int y, boolean white) throws InvalidCoordinateException;

    /**
     * Removes any existing stone at the given position.
     * Does nothing if the position currently has no stone/area indicator.
     * @param x the x coordinate of the stone/area indicator to remove, ranges from 0 to boardSize - 1.
     * @param y the y coordinate of the stone/area indicator to remove, ranges from 0 to boardSize - 1.
     * @throws InvalidCoordinateException when x or y coordinate fall outside of the board.
     */
    void removeStone(int x, int y) throws InvalidCoordinateException;

    /**
     * Adds a new area indicator to the board of the given type and at the given position.
     * Removes any existing stone/area indicator at the given position.
     * @param x the x coordinate of the new stone, ranges from 0 to boardSize - 1.
     * @param y the y coordinate of the new stone, ranges from 0 to boardSize - 1.
     * @param white if true then a white stone will be added, otherwise a black stone will be added
     * @throws InvalidCoordinateException when x or y coordinate fall outside of the board.
     */
    void addAreaIndicator(int x, int y, boolean white) throws InvalidCoordinateException;

    /**
     * Adds a hint indicator to the board of the given type and at the given position.
     * @param x the x coordinate of the hint, ranges from 0 to boardSize - 1.
     * @param y the y coordinate of the hint, ranges from 0 to boardSize - 1.
     * @throws InvalidCoordinateException when x or y coordinate fall outside of the board.
     */
    void addHintIndicator(int x, int y) throws InvalidCoordinateException;

    /**
     * Hides the hint indicator.
     */
    void removeHintIdicator();

    /**
     * Clears the board of all stones.
     */
    void clearBoard();

    /**
     * Starts the GO graphical user interface
     */
    void startGUI();

    /**
     * Stops the GO graphical user interface
     */
    void stopGUI();

}
