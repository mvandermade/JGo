package gui;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import client.ClientTextInputPacket;


public class GOGUIImpl extends Application {

    private final static int INITIAL_BOARD_SIZE = 19;
    private final static int INITIAL_SQUARE_SIZE = 50;

    private int currentBoardWidth = INITIAL_BOARD_SIZE;
    private int currentBoardHeight = INITIAL_BOARD_SIZE;
    private int currentSquareSize = INITIAL_SQUARE_SIZE;

    private Node[][] board = null;
    private List<Line> boardLines = new ArrayList<>();
    private Group root = null;
    private Stage primaryStage = null;
    private Node hint = null;

    private boolean mode3D = true;
    private boolean showStartupAnimation = false;

    private final PhongMaterial blackMaterial = new PhongMaterial();
    private final PhongMaterial whiteMaterial = new PhongMaterial();
    private final PhongMaterial yellowMaterial = new PhongMaterial();

    private static final CountDownLatch waitForConfigurationLatch = new CountDownLatch(1);
    private static final CountDownLatch initializationLatch = new CountDownLatch(1);
    
    private final Queue<ClientTextInputPacket> guiClickResult = new ConcurrentLinkedQueue<ClientTextInputPacket>();
    
    // Convert to sorted array
    public List<ClientTextInputPacket> getGuiClickResultToArray() {
    	
		List<ClientTextInputPacket> localPolledQueue = new ArrayList<ClientTextInputPacket>();
		Boolean done = false;
		
		while(!done) {
			ClientTextInputPacket polledObject = guiClickResult.poll();
			// null is the native response for 'no queue items left'
			if (polledObject != null) {
				localPolledQueue.add(polledObject);
			} else {
				done = true;
			}
		}
		
		// Synchronize using timestamping. So output can be done in order of array.
		List<ClientTextInputPacket> textQueue = localPolledQueue.stream()
				.sorted((f1, f2) -> Long.compare(f1.getStartTime(), f2.getStartTime())).
                collect(Collectors.toList());
    	
    	return textQueue;
    	
    }
    
    private void addGuiClickResult(ClientTextInputPacket guiPacket) {
    	
    	guiClickResult.add(guiPacket);
    }
    
    public void changeGuiTitle(String guiTitle) {
    	
    	// use instance otherwise mainThread error
    	instance.primaryStage.setTitle(guiTitle);
    }

    private static GOGUIImpl instance;

    protected static boolean isInstanceAvailable() {
        return instance != null;
    }

    public static GOGUIImpl getInstance() {
        return instance;
    }

    public GOGUIImpl() {
        // Has to be public otherwise JavaFX cannot find it
    }

    protected void countDownConfigurationLatch() {
        waitForConfigurationLatch.countDown();
    }

    protected void setShowStartupAnimation(boolean showStartupAnimation) {
        this.showStartupAnimation = showStartupAnimation;
    }

    protected void setMode3D(boolean mode3D) {
        this.mode3D = mode3D;
    }

    @Override
    public void start(Stage primaryStage) {
        instance = this;
        initDrawMaterials();

        try {
            waitForConfigurationLatch.await();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        this.primaryStage = primaryStage;

        primaryStage.setTitle("GO");
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
        	
        	public void handle(WindowEvent we) {
        		
        		System.out.println("please close the client app instead of the gui");
        		we.consume();
        		
        	}
        });

        initNewBoard();

        if (showStartupAnimation) {
            runStartupAnimation();
        }
        else {
            initializationLatch.countDown();
        }
    }

    private void initDrawMaterials() {
        blackMaterial.setDiffuseColor(Color.BLACK);
        blackMaterial.setSpecularColor(Color.LIGHTBLUE);
        whiteMaterial.setDiffuseColor(Color.WHITE);
        whiteMaterial.setSpecularColor(Color.LIGHTBLUE);
        yellowMaterial.setDiffuseColor(Color.YELLOW);
        yellowMaterial.setSpecularColor(Color.LIGHTBLUE);
    }

    private void runStartupAnimation() {
        final long startNanoTime = System.nanoTime();

        final AnimationTimer animationTimer = new AnimationTimer() {
            int roundCount = 0;
            int lastX = 0;

            public void handle(long currentNanoTime) {
                double t = (currentNanoTime - startNanoTime) / 50000000.0;

                int x = ((int) (t % currentBoardWidth));

                if (x < lastX) {
                    roundCount++;
                }

                if (x != lastX) {
                    if (roundCount >= 2) {
                        stop();
                        clearBoard();
                        initializationLatch.countDown();
                    }
                    else {
                        clearBoard();
                        if (x % 2 != 0) {
                            drawDiagonalStoneLine(x - 1, false, roundCount != 0);
                            drawDiagonalStoneLine(x, true, roundCount != 0);
                            drawDiagonalStoneLine(x + 1, false, roundCount != 0);
                        }
                        else {
                            drawDiagonalStoneLine(x - 1, true, roundCount != 0);
                            drawDiagonalStoneLine(x, false, roundCount != 0);
                            drawDiagonalStoneLine(x + 1, true, roundCount != 0);
                        }
                    }

                    lastX = x;
                }
            }
        };
        animationTimer.start();
    }

    private void initNewBoard() {
        root = new Group();
        board = new Node[currentBoardWidth][currentBoardHeight];
        
        Scene scene = new Scene(root, (currentBoardWidth + 1) * currentSquareSize, (currentBoardHeight + 1) * currentSquareSize);
        primaryStage.setScene(scene);
        primaryStage.show();

        ImagePattern pattern = new ImagePattern(new Image("background_1920.jpg"));
        scene.setFill(pattern);
        
        // Added to see what happens

        initBoardLines();
    }

    private void initBoardLines() {
        root.getChildren().removeAll(boardLines);
        boardLines.clear();

        int height = currentBoardHeight;
        int width = currentBoardWidth;
        int squareSize = currentSquareSize;

        // Draw horizontal lines
        for (int i = 1; i <= height; i++) {
            boardLines.add(new Line(squareSize, i * squareSize, width * squareSize, i * squareSize));
        }

        // Draw vertical lines
        for (int i = 1; i <= width; i++) {
            boardLines.add(new Line(i * squareSize, squareSize, i * squareSize, height * squareSize));
        }
        
        root.getChildren().addAll(boardLines);
        
        List<Circle> clickables = new ArrayList<>();
        List<Text> clickablesText = new ArrayList<>();
        int addCircleOuterRadius = 24;
        int addCircleRadius = 10;
        int labelCircleRadius = addCircleOuterRadius + addCircleOuterRadius/3;
        for (int xCoord = 1; xCoord <= height; xCoord++) {
        	for (int yCoord = 1; yCoord <= width; yCoord++) {
        		
        		Circle addCircleOuter = new Circle(addCircleOuterRadius);
        		addCircleOuter.relocate(xCoord * squareSize - addCircleOuterRadius, yCoord * squareSize - addCircleOuterRadius);
    			addCircleOuter.setStroke(Color.grayRgb(100, 0.3));
    			addCircleOuter.setFill(Color.grayRgb(100, 0.1));
        		
        		Circle addCircle = new Circle(addCircleRadius);
        		addCircle.setStroke(Color.BLUE);
        		addCircle.setFill(Color.ORANGE);
        		addCircle.relocate(xCoord * squareSize - addCircleRadius, yCoord * squareSize - addCircleRadius);
        		
        		Text labelCircle = new Text();
        		labelCircle.setFont(new Font(20));
        		labelCircle.setWrappingWidth(200);
        		labelCircle.setTextAlignment(TextAlignment.JUSTIFY);
        		// BEWARE!! x y coordinates are cols - rows.!!!
        		labelCircle.setVisible(false);
        		
        		labelCircle.setText(""+yCoord+"_"+xCoord);
        		labelCircle.relocate(xCoord * squareSize - labelCircleRadius, yCoord * squareSize - labelCircleRadius);
        		
        		
        		
        		// Inner blob
        		
        		addCircle.setOnMouseClicked(e -> {
        		    addGuiClickResult(new ClientTextInputPacket(labelCircle.getText()));
        		    
        		});
        		
        		addCircle.setOnMouseEntered(e-> {
        			addCircle.setFill(Color.BLACK);
        			addCircleOuter.setStroke(Color.grayRgb(100, 0.8));
        			addCircleOuter.setFill(Color.grayRgb(100, 0.5));
        			labelCircle.setVisible(true);
        		});
        		
        		addCircle.setOnMouseExited(e-> {
        			addCircle.setFill(Color.ORANGE);
        			addCircleOuter.setStroke(Color.grayRgb(100, 0.3));
        			addCircleOuter.setFill(Color.grayRgb(100, 0.1));
        			labelCircle.setVisible(false);
        		});
        		
        		// Outer blob
        		addCircleOuter.setOnMouseEntered(e -> {
        			
        			addCircle.setFill(Color.WHITE);
        			addCircleOuter.setStroke(Color.grayRgb(100, 0.8));
        			addCircleOuter.setFill(Color.grayRgb(100, 0.5));
        		});
        		
        		addCircleOuter.setOnMouseExited(e -> {
        			
        			addCircle.setFill(Color.ORANGE);
        			addCircleOuter.setStroke(Color.grayRgb(100, 0.3));
        			addCircleOuter.setFill(Color.grayRgb(100, 0.1));
        			
        		});
        		
        		// Text
        		labelCircle.setOnMouseEntered(e-> {
        			addCircle.setFill(Color.ORANGE);
        			addCircleOuter.setStroke(Color.grayRgb(100, 0.3));
        			addCircleOuter.setFill(Color.grayRgb(100, 0.1));
        			labelCircle.setVisible(false);
        		});
        		
        		labelCircle.setOnMouseExited(e-> {
        			addCircle.setFill(Color.ORANGE);
        			addCircleOuter.setStroke(Color.grayRgb(100, 0.3));
        			addCircleOuter.setFill(Color.grayRgb(100, 0.1));
        			labelCircle.setVisible(false);
        		});
        		
        		// Add to stack
        		
        		clickables.add(addCircleOuter);
        		clickables.add(addCircle);
        		clickablesText.add(labelCircle);
        		
        	}
        	
        }
        
        
        root.getChildren().addAll(clickables);
        root.getChildren().addAll(clickablesText);
        
        

        if (mode3D){
            hint = new Sphere(currentSquareSize / 2);
            ((Sphere) hint).setMaterial(yellowMaterial);
        } else {
            hint = new Circle(currentSquareSize / 2);
            ((Circle) hint).setFill(Color.YELLOW);
        }
        hint.setVisible(false);
        root.getChildren().add(hint);
    }

    private void drawDiagonalStoneLine(int diagonal, Boolean stoneType, boolean flip) {
        try {
            for (int x = 0; x < currentBoardWidth; x++) {
                for (int y = 0; y < currentBoardHeight; y++) {
                    if (x + y == diagonal * 2) {
                        if (!flip) {
                            addStone(x, y, stoneType);
                        }
                        else {
                            addStone(currentBoardWidth - 1 - x, y, stoneType);
                        }
                    }
                }
            }
        }
        catch (InvalidCoordinateException e) {
            throw new IllegalStateException(e);
        }
    }

    protected void addStone(int x, int y, boolean white) throws InvalidCoordinateException {
        checkCoordinates(x, y);
        removeStone(x, y);

        if (mode3D){
            Sphere newStone = new Sphere(currentSquareSize / 2);

            if (white) {
                newStone.setMaterial(whiteMaterial);
            }
            else {
                newStone.setMaterial(blackMaterial);
            }

            newStone.setTranslateX(((x + 1) * currentSquareSize));
            newStone.setTranslateY(((y + 1) * currentSquareSize));
            board[x][y] = newStone;

            root.getChildren().add(newStone);
        }
        else {
            Circle newStone = new Circle(((x + 1) * currentSquareSize), ((y + 1) * currentSquareSize), currentSquareSize / 2);

            if (white) {
                newStone.setFill(Color.WHITE);
            }
            else {
                newStone.setFill(Color.BLACK);
            }

            board[x][y] = newStone;
            root.getChildren().add(newStone);
        }
    }

    protected void removeStone(int x, int y) throws InvalidCoordinateException {
        checkCoordinates(x, y);

        if (board[x][y] != null) {
            root.getChildren().remove(board[x][y]);
        }
        board[x][y] = null;
    }

    protected void addAreaIndicator(int x, int y, boolean white) throws InvalidCoordinateException {
        checkCoordinates(x, y);
        removeStone(x, y);

        if (mode3D){
            Box areaStone = new Box(currentSquareSize / 3, currentSquareSize / 3, currentSquareSize / 3);
            areaStone.setMaterial(white ? whiteMaterial : blackMaterial);
            areaStone.setTranslateX(((x + 1) * currentSquareSize));
            areaStone.setTranslateY(((y + 1) * currentSquareSize));
            board[x][y] = areaStone;
            root.getChildren().add(areaStone);
        } else {
            Rectangle areaStone = new Rectangle(
                ((x + 1) * currentSquareSize) - currentSquareSize / 6,
                ((y + 1) * currentSquareSize) - currentSquareSize / 6,
                currentSquareSize / 3,
                currentSquareSize / 3
            );
            areaStone.setFill(white ? Color.WHITE : Color.BLACK);
            board[x][y] = areaStone;
            root.getChildren().add(areaStone);
        }
    }

    protected void addHintIndicator(int x, int y) throws InvalidCoordinateException {
        hint.setTranslateX(((x + 1) * currentSquareSize));
        hint.setTranslateY(((y + 1) * currentSquareSize));
        hint.setVisible(true);
    }

    protected void removeHintIdicator() {
        hint.setVisible(false);
    }

    private void checkCoordinates(int x, int y) throws InvalidCoordinateException {
        if (x < 0 || x >= currentBoardWidth) {
            throw new InvalidCoordinateException("x coordinate is outside of board range. x coordinate: " + x + " board range: 0-" + (currentBoardWidth - 1));
        }

        if (y < 0 || y >= currentBoardHeight) {
            throw new InvalidCoordinateException("y coordinate is outside of board range. y coordinate: " + y + " board range: 0-" + (currentBoardHeight - 1));
        }
    }

    protected void clearBoard() {
        try {
            for (int x = 0; x < currentBoardWidth; x++) {
                for (int y = 0; y < currentBoardHeight; y++) {
                    removeStone(x, y);
                }
            }
        }
        catch (InvalidCoordinateException e) {
            throw new IllegalStateException(e);
        }
    }

    protected void setBoardSize(int size) {
        currentBoardHeight = size;
        currentBoardWidth = size;

        initNewBoard();
    }

    protected void setInitialBoardSize(int size) {
        currentBoardHeight = size;
        currentBoardWidth = size;
    }

    protected static void startGUI() {
        new Thread() {
            public void run() {
                Application.launch(GOGUIImpl.class);
            }
        }.start();
    }

    protected void waitForInitializationLatch() {
        try {
            System.out.println("Attempting init of the GOGUI!");
            if (!initializationLatch.await(30, TimeUnit.SECONDS)) {
                System.out.println("Initialization of the GOGUI failed!");
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
