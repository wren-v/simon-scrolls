import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;

public class GameGrid extends JComponent implements KeyListener, MouseListener {
  
  private static final int NO_KEY = -1; 
  
  public static final Color DEFAULT_COLOR = Color.BLACK; 
  
  //notice not final, thus it can be changed in constructor
  //(if you don't use a background image, you can change the overall color)
  public static Color OVERALL_COLOR = DEFAULT_COLOR; 
  
  
  private Cell[][] cells;
  private JFrame frame;
  private Color lineColor;
  private Image background = null;
  private Image splash = null;
  
  private int lastKeyPressed;
  private Location lastLocationClicked;
  
  public GameGrid(int numRows, int numCols) {
    init(numRows, numCols, null);
  }
  
  // Used to provide a background color with which 
  // to underpaint all the cells 
  public GameGrid(int numRows, int numCols, Color bcolor) {
    init(numRows, numCols, bcolor);
  }
  
  // Useful constructor while working on extension
  public GameGrid(int numRows, int numCols, String splashFile, String backgroundFile) {
    
    splash = setImage(splashFile);  
    background = setImage(backgroundFile);
    
    init(numRows, numCols, null);
  }
  
  //code executed when repaint() on the component is called
  public void paintComponent(Graphics g) {
    
    if (splash!=null)
      g.drawImage(splash, 0, 0,frame.getWidth(), frame.getHeight(), null);
    else 
      drawGameGrid(g);
  }
  
  
  
  //-------------------------- Useful methods --------------------------------
  
  /**
   * sets the title at the top of the grid
   */
  public void setTitle(String title) {
    frame.setTitle(title);
  }
  
  /**
   * sets the cell image at the passed location
   * @param loc Location to change image
   * @param imgName String name of the desired image
   */
  public void setCellImage(Location loc, String imgName) {
    if (isValid(loc)) {
      cells[loc.getRow()][loc.getCol()].setImageFileName(imgName);
      repaint();
    } else 
    System.out.println("Can't set image for this invalid location\n" +
      "Bad grid.setImage(loc, name) call with " + imgName);
    
  } 
  
  /**
   * gets the image at the passed location
   * @param loc current Location
   * @return String image file name 
   */
  public String getCellImage(Location loc) {
    if (isValid(loc))
      return cells[loc.getRow()][loc.getCol()].getImageFileName();
    
    System.out.println("Can't get image at this invalid location\n" + 
      "Bad grid.getImage(loc) call");
    return null;
    
  }
  
  /**
   * gets the number of rows in the grid
   * @return int number of rows
   */
  public int getNumRows() { return cells.length; }
  
  /**
   * gets the number of columns in the grid
   * @return int number of columns
   */
  public int getNumCols() { return cells[0].length; }
  
  /**
   * gets the current line color
   * @return current grid line Color
   */
  public Color getLineColor() { return lineColor; }
  
  //for debugging the motion, consider turning on the cell lines
  /**
   * sets the line color of the grid
   * @param color desired line color
   */
  public void setLineColor(Color color) {
    lineColor = color;
    repaint();
  }
  
  //Useful for extension
  /**
   * sets the current color of the location
   * @param loc location to change
   * @param color color to change to
   */
  public void setColor(Location loc, Color color) {
    if (isValid(loc)) {
      cells[loc.getRow()][loc.getCol()].setColor(color);
      System.out.println("change cell color " + loc + " " + color);
      repaint();
    }
  }
  
  /**
   * gets the current color of the location
   * @param loc Location for which to check color
   */
  public Color getColor(Location loc) {
    if (isValid(loc))
      return cells[loc.getRow()][loc.getCol()].getColor();
    
    return null;
  }
  
  /**
   * sets a splash screen; if null is passed then it is game time
   * @param imageFileName splash file name
   */
  public void setSplash(String imageFileName) {
    splash = setImage(imageFileName);
    
    if (splash != null) background = null;  //only one on: if splash on,
    // background is turned off
    repaint();
  }
  
  /**
   * changes the game background at any time
   * @param imageFileName background image file name
   */
  public void setGameBackground(String imageFileName) {
    background = setImage(imageFileName);
    repaint();
  }
  
  
  /**
   * checks the last key pressed since last call 
   * @return code of last key press, -1 otherwise (none pressed)
   */
  public int checkLastKeyPressed() {
    int key = lastKeyPressed;
    lastKeyPressed = NO_KEY;
    return key;
  }
  
  
  /** 
   * checks the location of the last mouse click
   * @return location clicked since last call, null otherwise 
   */
  public Location checkLastLocationClicked() {
    Location loc = lastLocationClicked;
    lastLocationClicked = null;
    return loc;
  }
  
  /**
   * saves an image of the game in a file
   * @param imgFileName location to save current image
   */
  public void save(String imgFileName) {
    try {
      BufferedImage bi = new BufferedImage(getWidth(), getHeight(), 
        BufferedImage.TYPE_INT_RGB);
      
      paintComponent(bi.getGraphics());
      int index = imgFileName.lastIndexOf('.');
      if (index == -1)
        throw new RuntimeException("invalid file name:  " + imgFileName);
      ImageIO.write(bi, imgFileName.substring(index + 1), new File(imgFileName));
      
    } catch(IOException e) {
      throw new RuntimeException("unable to save to file:  " + imgFileName);
    }
  }
  
  /*---------------------------------------------------------------------------
  private instance methods of the Grid object: helper methods
  ---------------------------------------------------------------------------*/
  
  private int initCell(int numRows, int numCols, Color bcolor) {
    //set the cell color to the provided bcolor or to the default BLACK 
    OVERALL_COLOR = (bcolor != null)? bcolor: DEFAULT_COLOR;
    
    cells = new Cell[numRows][numCols];
    
    for (int row = 0; row < numRows; row++)
      for (int col = 0; col < numCols; col++)
      cells[row][col] = new Cell();
    
    //initial cellSize for determining Window size  
    return Math.max(Math.min(500 / numRows, 500 / numCols), 1);    
    
    
  }
  
  private void init(int numRows, int numCols, Color bcolor) {
    
    
    int cellSize = initCell(numRows, numCols, bcolor);
    
    setPreferredSize(new Dimension(cellSize * numCols, cellSize * numRows));
    addMouseListener(this);
    
    frame = new JFrame("Grid");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.addKeyListener(this);
    frame.getContentPane().add(this);
    frame.pack();
    frame.setVisible(true);
    
    lastKeyPressed = NO_KEY;
    lastLocationClicked = null;
  }
  
  private void drawGameGrid(Graphics g) {
    
    
    if (background!=null) {
      g.drawImage(background, 0, 0, 
        frame.getWidth(), frame.getHeight(), null);
    }
    
    for (int row = 0; row < getNumRows(); row++) {
      for (int col = 0; col < getNumCols(); col++) {
        
        int cellSize = getCellSize();
        
        int x = col * cellSize;
        int y = row * cellSize; 
        
        drawCell(g, x, y, new Location(row, col), cellSize);
        
        //draw boundary if lineColor has been set
        if (lineColor != null) {
          Graphics2D g2 = (Graphics2D) g;
          g.setColor(lineColor);
          g2.setStroke(new BasicStroke(3));
          g.drawRect(x, y, cellSize, cellSize);
        }
      }  
    }
    
  }
  
  private void drawCell(Graphics g, int x, int y, Location loc, int cellSize) {
    
    Cell cell = cells[loc.getRow()][loc.getCol()];
    
    
    Image image = setImage(cell.getImageFileName());
    
    cell.draw(g, x,  y, image, cellSize, background);
    
  }
  
  private int getCellSize() {
    int cellWidth = getWidth() / getNumCols();
    int cellHeight = getHeight() / getNumRows();
    return Math.min(cellWidth, cellHeight);
  }
  
  private boolean isValid(Location loc) {
    
    int row = loc.getRow();
    int col = loc.getCol();
    if (0 <= row && row < getNumRows() && 0 <= col && col < getNumCols())
      return true;
    else 
      throw new RuntimeException("Invalid " + loc);
    
  }
  
  
  private Image loadImage(String imageFileName) {
    URL url = getClass().getResource(imageFileName);
    if (url == null)
      throw new RuntimeException("cannot find file:  " + imageFileName);
    return new ImageIcon(url).getImage();
    
  }
  
  private Image setImage(String imageFileName) {
    if (imageFileName != null)
      return loadImage(imageFileName);
    
    return null;
  }
  
  
  /*---------------------------------------------------------------------------
  Interface methods of the Grid object 
  which implements KeyListener and MouseListener
  ---------------------------------------------------------------------------*/
  
  
  // methods of the KeyListener interface: keyPressed is used
  // others need to be there but do nothing
  public void keyPressed(KeyEvent e) {
    lastKeyPressed = e.getKeyCode();
  }
  
  public void keyReleased(KeyEvent e) { }
  
  public void keyTyped(KeyEvent e) {  }
  
  // methods of the MouseListener interface: mousePressed is used
  // others need to be there but do nothing
  public void mousePressed(MouseEvent e) {
    int cellSize = getCellSize();
    int row = e.getY() / cellSize;
    if (row < 0 || row >= getNumRows())
      return;
    int col = e.getX() / cellSize;
    if (col < 0 || col >= getNumCols())
      return;
    lastLocationClicked = new Location(row, col);
  }
  
  public void mouseReleased(MouseEvent e) { }
  
  public void mouseClicked(MouseEvent e) { }
  
  public void mouseEntered(MouseEvent e) { }
  
  public void mouseExited(MouseEvent e) { }
  

  
  
  
  
  class Cell {
    private Color color = null;
    private String imageFileName;
    
    public Cell() {
      imageFileName = null;
    }
    
    
    public void setColor(Color c) {
      color = c;
    }
    
    public Color getColor() {
      return color;
    }
    
    public String getImageFileName() {
      return imageFileName;
    }
    
    public void setImageFileName(String fileName) {
      imageFileName = fileName;
    }
    
    public void draw(Graphics g, int x, int y, Image image, int cellSize, Image background) {
      
      if (background == null || color != null) {
        
        if (background == null) 
          g.setColor(GameGrid.OVERALL_COLOR);       
        
        if (color != null)
          g.setColor(color);      
        
        g.fillRect(x, y, cellSize, cellSize);   
      }
      
      if (image!= null) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);
        
        if (width > height) {
          int drawHeight = cellSize * height / width;
          
          g.drawImage(image, x, y + (cellSize - drawHeight) / 2,
            cellSize, drawHeight, null);
        } else {
          int drawWidth = cellSize * width / height;
          
          g.drawImage(image, x + (cellSize - drawWidth) / 2, y, 
            drawWidth, cellSize, null);
        }
      }
      
    }
    
  }
  
  
}