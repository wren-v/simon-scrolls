import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class BaseGame extends AbstractGame {
    
    private static final int INTRO = 0; 
    private static boolean paused = false;

    private String PLAYER_IMG = "images/user.gif";    // specify user image file
    private String FAKE_PLAYER_IMG = "images/user.gif";	//specify fake user image file
    private String SPLASH_IMG = "images/splash.png"; //specify splash screen image file
    private String RED_IMG = "images/red.gif";	//specify red block image file
    private String YELLOW_IMG = "images/yellow.gif"; //specify yellow block image file
    private String BLUE_IMG = "images/blue.gif";	//specify blue block image file
    private String GREEN_IMG = "images/green.gif";	//specify green block image file
    
    private String RED_B = "images/redb.png";
    private String GREEN_B = "images/greenb.png";
    private String BLUE_B = "images/blueb.png";
    private String YELLOW_B = "images/yellowb.png";
    private String WHITE_B = "images/white.png";
    private String GAMEOVER = "images/gameover.png";
    private String WINOVER = "images/winover.png";

    private String lastCollision = null;
    private int failures = 0;
    private int currentSequenceIndex = 0;
    private String sequence = "";
    private int highestSequence = 1;
    private int gameCount = 0;
    private boolean seeSequence = true;

    // default number of vertical/horizontal cells: height/width of grid
    private static final int DEFAULT_GRID_H = 5;
    private static final int DEFAULT_GRID_W = 10;
    //default timer delay
    private static final int DEFAULT_TIMER_DELAY = 100;
    
    private static final int MAXIMUM_FAILURES = 2; //maximum failures, begins at zero
    
    // default location of user at start
    private static final int DEFAULT_PLAYER_ROW = 0;
    protected static final int STARTING_FACTOR = 3;      // you might change that this when working on timing
    protected int factor = STARTING_FACTOR;
    protected Location player;
    protected int screen = INTRO;
    protected GameGrid grid;
    
    //BaseGame constructors
    public BaseGame() {
        this(DEFAULT_GRID_H, DEFAULT_GRID_W);
    }  
    public BaseGame(int grid_h, int grid_w){
         this(grid_h, grid_w, DEFAULT_TIMER_DELAY);
    }
    public BaseGame(int hdim, int wdim, int init_delay_ms) {
        super(init_delay_ms);
        //set up our "board" (i.e., game grid) 
        grid = new GameGrid(hdim, wdim);           
    }
    
    /******************** Methods **********************/
    
    protected void initGame(){    
         // store and initialize user position
         player = new Location(DEFAULT_PLAYER_ROW, 0);
         grid.setCellImage(player, PLAYER_IMG);
         sequence = getSequence(4);
         flashSequence(1);
         updateTitle();                           
    }
           
    // Display the intro screen
    protected void displayIntro(){
       grid.setSplash(SPLASH_IMG);
       while (screen == INTRO) {
          super.sleep(timerDelay);
          // Listen to keep press to break out of intro 
          // in particular here --> space bar necessary
          handleKeyPress();
       }
       grid.setSplash(null);
    }
  
    protected void updateGameLoop() {
        handleKeyPress();        // update state based on user key press 
        
        if (!paused) {
        	if (turnsElapsed % factor == 0) {  // if it's the FACTOR timer tick
        		populateRightEdge();
        		scrollLeft();
        	}     
        	
        	if (isGameOver()) {
        		displayOutcome();
        	}
        	updateTitle();
        }
        
    }
    
    // update game state to reflect adding in new cells in the right-most column
    private void populateRightEdge() {
    	Location[] rightColumn = new Location[DEFAULT_GRID_H - (DEFAULT_GRID_H % 2)];
    	ArrayList<Integer> rightEdge = new ArrayList<Integer>();
    	for (int j=0; j< DEFAULT_GRID_H; j++) {
    		Location tempLocation = new Location(j, DEFAULT_GRID_W);
    		grid.setCellImage(tempLocation, null);
    	}
    	for (int i=0; i< rightColumn.length; i++) {
    		int randRowNum = rand.nextInt(DEFAULT_GRID_H);
    		if (!(rightEdge.contains(randRowNum))) {
    			rightEdge.add(randRowNum);
    		}
    		for (int k=0; k< rightEdge.size(); k++) {
    			Location tempLocation = new Location(rightEdge.get(k), DEFAULT_GRID_W);
    			if (rand.nextInt(50) == 0) {
    				grid.setCellImage(tempLocation, RED_IMG);
    			}
    			else if (rand.nextInt(50) == 1) {
    				grid.setCellImage(tempLocation, YELLOW_IMG);
    			}
    			else if (rand.nextInt(50) == 2) {
    				grid.setCellImage(tempLocation, BLUE_IMG);
    			}
    			else if (rand.nextInt(50) == 3) {
    				grid.setCellImage(tempLocation, GREEN_IMG);
    			}
    		}
    	}    	
    }
    
    // updates the game state to reflect scrolling left by one column
    private void scrollLeft() {
    	for (int i = 0; i<DEFAULT_GRID_H + 1; i++) {
    		for (int k = 0; k<DEFAULT_GRID_W + 1; k++) {
    			Location oldLocation = new Location(i, k);
    			String oldImage = grid.getCellImage(oldLocation);
    			int oldRow = oldLocation.getRow();
    			int oldCol = oldLocation.getCol();
    			//if in play area and image is not player image
    			if (oldCol >= 1 && oldImage != PLAYER_IMG) {
    				Location newLocation = new Location(oldRow, oldCol - 1);
    				//if new location collides with a player
    				if (grid.getCellImage(newLocation) == PLAYER_IMG) {
    					handleCollision(newLocation, oldImage);
    				}
    				//if new location doesn't collide with a player
    				else if (grid.getCellImage(newLocation) != PLAYER_IMG) {
    					grid.setCellImage(oldLocation, null);
    					grid.setCellImage(newLocation, oldImage);
    				}
    			}
    			//deals with keeping where player is
    			else if (oldImage == PLAYER_IMG && oldCol >= 0) {
    				grid.setCellImage(oldLocation, PLAYER_IMG);
    			}
    			//erases the left most column
    			else if (oldCol < 1) {
    				grid.setCellImage(oldLocation, null);
    			}
    			
    		}
    	}
    }

    //handles a collision between the player and the blocks
    private void handleCollision(Location collisionLocation, String collisionImage) {
    	if (collisionImage == (RED_IMG)) {
    		lastCollision = "R";	
    	}
    	else if (collisionImage == (GREEN_IMG)) {
    		lastCollision = "G";
    	}
    	else if (collisionImage == (BLUE_IMG)) {
    		lastCollision = "B";
    	}
    	else if (collisionImage == (YELLOW_IMG)) {
    		lastCollision = "Y";
    	}
    	if (sequence.length() < highestSequence) {
    		failures = 0;
    		currentSequenceIndex = 1;
    		sequence = "";
    		highestSequence = 1;
    		gameCount = 0;
    		sequence = getSequence(4);
    	}
    	if (lastCollision != null) {
    		compareToSequence(lastCollision);
    	}
        grid.setCellImage(collisionLocation, PLAYER_IMG);
        lastCollision = null;
    }
    
    //compares last collision to sequence, increments failures and sequence index
    private void compareToSequence(String lastCollision) {
    	String charToS = "";
    	if (currentSequenceIndex < sequence.length()-1) {
    		charToS += sequence.charAt(currentSequenceIndex);
    	}
    	if (!charToS.equals(lastCollision)) {
    		failures++;
    	}
    	else {
    		currentSequenceIndex++;
    	}
    	if (currentSequenceIndex == highestSequence && highestSequence < sequence.length()) {
    		currentSequenceIndex = 0;
    		highestSequence++;
    		flashSequence(highestSequence);
    	}
    }
   
    //flashes the sequence by changing background color
    private void flashSequence(int currentSequenceIndex) {
    	for (int i=0; i<currentSequenceIndex; i++) {
    		if (sequence.charAt(i) == 'R') {
    			grid.setGameBackground(RED_B);
    			sleep(1200);
    		}
    		if (sequence.charAt(i) == 'G') {
    			grid.setGameBackground(GREEN_B);
    			sleep(1200);
    		}
    		if (sequence.charAt(i) == 'B') {
    			grid.setGameBackground(BLUE_B);
    			sleep(1200);
    		}
    		if (sequence.charAt(i) == 'Y') {
    			grid.setGameBackground(YELLOW_B);
    			sleep(1200);
    		}
    		grid.setGameBackground(WHITE_B);
    		sleep(150);
    	}
    	grid.setGameBackground(WHITE_B);
    }

    //creates a new sequence of a specified number of colors
    private String getSequence(int length) {
    	String sequence = "";
    	length++;
    	for (int i=0; i<length; i++) {
    		int randNum = rand.nextInt(3);
    		if (randNum == 0) {
    				sequence+= "R";
    		}
    		else if (randNum == 1) {
    			sequence+= "G";
    		}
    		else if (randNum == 2) {
    			sequence+= "B";
    		}
    		else if (randNum == 3) {
    			sequence+= "Y";
    		}
    	}
    	return sequence;
    }
    
    //---------------------------------------------------//
    
    // handles actions upon key press in game
    protected void handleKeyPress() {
        int key = grid.checkLastKeyPressed();
    
        // Q for quit
        if (key == KeyEvent.VK_Q) {
            System.exit(0);
        }
        
        //saves a screenshot jpg
        else if (key == KeyEvent.VK_S) {
        	grid.save("screenshot.jpg");
        }
        
        //advances past the splash screen
        else if (key == KeyEvent.VK_SPACE) {
        	screen += 1;
        }
        
        //sets if you can display the sequence or not
        else if (key == KeyEvent.VK_D) {
        	if (seeSequence) {
        		seeSequence = false;
        	}
        	else {
        		seeSequence = true;
        	}
        }
        
        //moves player up
        else if (key == KeyEvent.VK_UP && !paused) {
        	if (!(player.getRow() <= 0)) {
        		grid.setCellImage(player, null);
        		player.setRow(player.getRow() - 1);
        		if (grid.getCellImage(player) != null) {
        			String collisionImage = grid.getCellImage(player);
        			handleCollision(player, collisionImage);
        		}
        		else if (grid.getCellImage(player) == null) {
        			grid.setCellImage(player, PLAYER_IMG);
        		}
        	}
        }
        
        //moves player down
        else if (key == KeyEvent.VK_DOWN && !paused) {
        	if (!(player.getRow() >= DEFAULT_GRID_H - 1)) {
        		grid.setCellImage(player, null);
        		player.setRow(player.getRow() + 1);
        		if (grid.getCellImage(player) != null) {
        			String collisionImage = grid.getCellImage(player);
        			handleCollision(player, collisionImage);
        		}
        		else if (grid.getCellImage(player) == null) {
        			grid.setCellImage(player, PLAYER_IMG);
        		}
        	}
        }
        
        //moves player left
        else if (key == KeyEvent.VK_LEFT && !paused) {
        	if (!(player.getCol() <= 0)) {
        		grid.setCellImage(player, null);
        		player.setCol(player.getCol() - 1);
        		if (grid.getCellImage(player) != null) {
        			String collisionImage = grid.getCellImage(player);
        			handleCollision(player, collisionImage);
        		}
        		else if (grid.getCellImage(player) == null) {
        			grid.setCellImage(player, PLAYER_IMG);
        		}
        	}
        }
        
        //moves player right
        else if (key == KeyEvent.VK_RIGHT && !paused) {
        	if (!(player.getCol() >= DEFAULT_GRID_W - 1)) {
        		grid.setCellImage(player, null);
        		player.setCol(player.getCol() + 1);
        		if (grid.getCellImage(player) != null) {
        			String collisionImage = grid.getCellImage(player);
        			handleCollision(player, collisionImage);
        		}
        		else if (grid.getCellImage(player) == null) {
        			grid.setCellImage(player, PLAYER_IMG);
        		}
        	}
        }
        
        //pause and unpause toggle
        else if (key == KeyEvent.VK_P) {
        	if (paused) {
        		paused = false;
        	}
        	else if (!paused) {
        		paused = true;
        		grid.setTitle("Scrolling Game: PAUSED!!!");
        	}
        }
    }

    // update the title bar of the game window 
    private void updateTitle() {
    	String displaySequence = "";
    	if (seeSequence) {
    		if (highestSequence < sequence.length()) {
    			for (int i=0; i<highestSequence; i++) {
    				displaySequence += sequence.charAt(i);
    				grid.setTitle("SIMONS SEQUENCE: "+displaySequence);
    			}
    		}
    	}
    	else {
    		grid.setTitle("HIDDEN SEQUENCE");
    	}
    	
    }
    
    // return true if the game is finished, false otherwise
    //      used by play() to terminate the main game loop 
    protected boolean isGameOver() {
        if (failures > MAXIMUM_FAILURES) {
        	return true;
        }
        else if (currentSequenceIndex >= sequence.length()-1) {
        	return true;
        }
    	return false;
    }
    
    // display the game over screen, blank for now
    protected void displayOutcome() {
    	for (int i = 0; i<DEFAULT_GRID_H + 1; i++) {
    		for (int k = 0; k<DEFAULT_GRID_W + 1; k++) {
    			Location oldLocation = new Location(i, k);
    			grid.setCellImage(oldLocation, null);
    		}
    	}
    	if (failures > MAXIMUM_FAILURES) {
    		grid.setGameBackground(GAMEOVER);
    	}
    	else {
    		grid.setGameBackground(WINOVER);
    	}

    }
}
