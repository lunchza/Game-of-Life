package life;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

public class GameOfLife extends JFrame implements ActionListener, MouseListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//program version
	private final static String VERSION = "1.1";
	//2D array of Cells represents colony
	private Cell[][] grid;
	//buffer for state transitions
	private boolean[][] buffer;
	//size(width, height) of grid
	private int size;
	//frame panels
	private JPanel buttonPanel, gridPanel;
	//status bar
	private JStatusBar statusBar;
	//determines if life forms are being killed or created
	private int currentMode;
	//stat tracking variables
	private int currentGeneration, aliveCount;
	//cell color
	private Color cellColor;

	
	/*
	 * MenuBar components
	 */
	private JMenuBar menuBar;
	private JMenu fileMenu, templateMenu, optionsMenu, changeSizeMenu, changeColorMenu, helpMenu;
	private JMenuItem clearMenuItem, exitMenuItem, helpMenuItem, controlsMenuItem, aboutMenuItem;
	private JMenuItem glider, smallExploder, exploder, tenCellRow, lightSpaceship, tumbler, gosperGliderGun;
	private JRadioButtonMenuItem blackColour, blueColour, greenColour, redColour;
	private JRadioButtonMenuItem smallSize, mediumSize, largeSize;
	
	private Cell clickedCell;
	
	//modes
	private static final int IDLING = -1; 
	private static final int CREATING_MODE = 0; 
	private static final int KILLING_MODE = 1;
	
	//sizes
	private static final int SMALL = 5;
	private static final int MEDIUM = 20;
	private static final int LARGE = 50;
	
	//speed controls
	private JButton startButton, stepButton;
	@SuppressWarnings("rawtypes")
	private JComboBox speedBox;
	String[] speeds = {"Slow(1x)", "Fast(10x)", "Hyper(50x)"};
	
	private boolean running = false;
	//determines delay between steps
	private int speedFactor;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GameOfLife()
	{
		super("Game of life v" + VERSION);
		setSize(1024, 768);
				
		/**
		 * COMPONENT INIT
		 */
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		optionsMenu = new JMenu("Options");
		helpMenu = new JMenu("Help");
		
		clearMenuItem = new JMenuItem("Clear");
		clearMenuItem.addActionListener(this);
		templateMenu = new JMenu("Load template");
		templateMenu.addActionListener(this);
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(this);
		
		changeColorMenu = new JMenu("Cell colour");
		changeSizeMenu = new JMenu("Grid size");
		
		glider = new JMenuItem("Glider");
		glider.addActionListener(this);
		smallExploder = new JMenuItem("Small exploder");
		smallExploder.addActionListener(this);
		exploder = new JMenuItem("Exploder");
		exploder.addActionListener(this);
		tenCellRow = new JMenuItem("10 Cell row");
		tenCellRow.addActionListener(this);
		lightSpaceship = new JMenuItem("Light spaceship");
		lightSpaceship.addActionListener(this);
		tumbler = new JMenuItem("Tumbler");
		tumbler.addActionListener(this);
		gosperGliderGun = new JMenuItem("Gosper glider gun");
		gosperGliderGun.addActionListener(this);
		
		blackColour = new JRadioButtonMenuItem("Black");
		blackColour.addActionListener(this);
		blackColour.setSelected(true);
		blueColour = new JRadioButtonMenuItem("Blue");
		blueColour.addActionListener(this);
		greenColour = new JRadioButtonMenuItem("Green");
		greenColour.addActionListener(this);
		redColour = new JRadioButtonMenuItem("Red");
		redColour.addActionListener(this);
		
		smallSize = new JRadioButtonMenuItem("Small(5x5)");
		smallSize.addActionListener(this);
		mediumSize = new JRadioButtonMenuItem("Medium(20x20)");
		mediumSize.addActionListener(this);
		largeSize = new JRadioButtonMenuItem("Large(50x50)");
		largeSize.addActionListener(this);
		
		helpMenuItem = new JMenuItem("Help");
		helpMenuItem.addActionListener(this);
		controlsMenuItem = new JMenuItem("Controls");
		controlsMenuItem.addActionListener(this);
		aboutMenuItem = new JMenuItem("About");
		aboutMenuItem.addActionListener(this);
		
		ButtonGroup btg2 = new ButtonGroup();
		btg2.add(blackColour);
		btg2.add(blueColour);
		btg2.add(greenColour);
		btg2.add(redColour);
		
		ButtonGroup btg = new ButtonGroup();
		btg.add(smallSize);
		btg.add(mediumSize);
		btg.add(largeSize);		
		
		mediumSize.setSelected(true);
		
		templateMenu.add(glider);
		templateMenu.add(smallExploder);
		templateMenu.add(exploder);
		templateMenu.add(tenCellRow);
		templateMenu.add(lightSpaceship);
		templateMenu.add(tumbler);
		templateMenu.add(gosperGliderGun);
		
		fileMenu.add(clearMenuItem);
		fileMenu.add(templateMenu);
		fileMenu.add(exitMenuItem);
		
		changeColorMenu.add(blackColour);
		changeColorMenu.add(blueColour);
		changeColorMenu.add(greenColour);
		changeColorMenu.add(redColour);
		
		changeSizeMenu.add(smallSize);
		changeSizeMenu.add(mediumSize);
		changeSizeMenu.add(largeSize);
		
		optionsMenu.add(changeColorMenu);
		optionsMenu.add(changeSizeMenu);
		
		helpMenu.add(helpMenuItem);
		helpMenu.add(controlsMenuItem);
		helpMenu.add(aboutMenuItem);
		
		menuBar.add(fileMenu);
		menuBar.add(optionsMenu);
		menuBar.add(helpMenu);
		
		setJMenuBar(menuBar);
		
		buttonPanel = new JPanel();
		startButton = new JButton("Start");
		startButton.addActionListener(this);
		
		stepButton = new JButton("Step");
		stepButton.addActionListener(this);
		
		speedBox = new JComboBox(speeds);
		
		buttonPanel.add(stepButton);
		buttonPanel.add(startButton);
		buttonPanel.add(speedBox);
				
		/**
		 * VARIABLE INIT
		 */
		size = MEDIUM;
		currentGeneration = aliveCount = 0;
		buffer = new boolean[size][size];
		currentMode = IDLING;
		cellColor = Color.black;
		statusBar = new JStatusBar("Program ready", JStatusBar.LEFT_ORIENTATION);
		
		setLayout(new BorderLayout());
		populateGrid(size);
		add(statusBar, BorderLayout.SOUTH);
		add(buttonPanel, BorderLayout.NORTH);
		
		/**
		 * Establish key dispatcher
		 */
		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());

		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);	
	}
	
	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == startButton) //begin running simulation
		{
			if(!running)
			{
			running = true;
			startButton.setText("Stop");
			stepButton.setEnabled(false);
			simulate();
			}
			
			else
			{
				running = false;
				stepButton.setEnabled(true);
				startButton.setText("Start");
			}
		}
		
		if (e.getSource() == stepButton)
		{
			step();
		}
			
		//menus
		if (e.getSource() == clearMenuItem)
		{
			clear();
		}
		
		if(e.getSource() == exitMenuItem)
		{
			System.exit(0);
		}
		
		/**
		 * Change the cell colour
		 */
		
		if (e.getSource() == blackColour)
		{
			cellColor = Color.black;
			repaint();
		}
		
		if (e.getSource() == blueColour)
		{
			cellColor = Color.blue;
			repaint();
		}
		
		if (e.getSource() == greenColour)
		{
			cellColor = Color.green;
			repaint();
		}
		
		if (e.getSource() == redColour)
		{
			cellColor = Color.red;
			repaint();
		}
		
		/**
		 * Load a template. These are all hard-coded and were constructed by hand
		 */
		
		if (e.getSource() == glider)
		{
			actionPerformed(new ActionEvent(mediumSize, 10, null));
			Point[] points = {new Point(8, 10), new Point(9, 10), new Point(10, 10), new Point(10, 9), new Point(9, 8)};
			loadtemplate(points);
		}
		
		if (e.getSource() == smallExploder)
		{
			actionPerformed(new ActionEvent(mediumSize, 10, null));
			Point[] points = {new Point(9, 8), new Point(8, 9), new Point(9, 9), new Point(10, 9), new Point(8, 10), new Point(10, 10), new Point(9, 11)};
			loadtemplate(points);
		}
		
		if (e.getSource() == exploder)
		{
			actionPerformed(new ActionEvent(mediumSize, 10, null));
			Point[] points = {new Point(7, 7), new Point(7, 8), new Point(7, 9), new Point(7, 10), new Point(7, 11), new Point(9, 7), new Point(9, 11), new Point(11, 7), new Point(11, 8), new Point(11, 9), new Point(11, 10), new Point(11, 11)};
			loadtemplate(points);
		}
		
		if (e.getSource() == tenCellRow)
		{
			actionPerformed(new ActionEvent(mediumSize, 10, null));
			Point[] points = {new Point(5, 10), new Point(6, 10), new Point(7, 10), new Point(8, 10), new Point(9, 10), new Point(10, 10), new Point(11, 10), new Point(12, 10), new Point(13, 10), new Point(14, 10)};
			loadtemplate(points);
		}
		
		if (e.getSource() == lightSpaceship)
		{
			actionPerformed(new ActionEvent(mediumSize, 10, null));
			Point[] points = {new Point(8, 6), new Point(9, 6), new Point(10, 6), new Point(11, 6), new Point(7, 7), new Point(11, 7), new Point(11, 8), new Point(10, 9), new Point(7, 9)};
			loadtemplate(points);
		}
		
		if (e.getSource() == tumbler)
		{
			actionPerformed(new ActionEvent(mediumSize, 10, null));
			Point[] points = {new Point(7, 6), new Point(8, 6), new Point(7, 7), new Point(8, 7), new Point(8, 8), new Point(8, 9), new Point(8, 10), new Point(7, 11), new Point(6, 11), new Point(6, 10), new Point(6, 9), new Point(10, 6), new Point(11, 6), new Point(10, 7), new Point(11, 7), new Point(10, 8), new Point(10, 9), new Point(10, 10), new Point(11, 11), new Point(12, 11), new Point(12, 10), new Point(12, 9)};
			loadtemplate(points);
		}
		
		if (e.getSource() == gosperGliderGun)
		{
			actionPerformed(new ActionEvent(largeSize, 10, null));
			Point[] points = {new Point(7, 11), new Point(7, 12), new Point(8, 11), new Point(8, 12), new Point(16, 11), new Point(17, 11), new Point(15, 12), new Point(17, 12), new Point(15, 13), new Point(16, 13), new Point(23, 13), new Point(24, 13), new Point(23, 14), new Point(25, 14), new Point(23, 15), new Point(29, 11), new Point(30, 11), new Point(29, 10), new Point(30, 9), new Point(31, 9), new Point(31, 10),
					new Point(41, 10), new Point(41, 9), new Point(42, 9), new Point(42, 10), new Point(42, 16), new Point(42, 17), new Point(43, 16), new Point(44, 17), new Point(42, 18), new Point(33, 21), new Point(32, 21), new Point(31, 21), new Point(31, 22), new Point(32, 23)};
			loadtemplate(points);
		}
		
		if (e.getSource() == smallSize) //change colony size to small
		{
			if(running)
				clear();
			
			populateGrid(size = SMALL);
		}
		
		if (e.getSource() == mediumSize) //change colony size to medium
		{
			if(running)
				clear();
			
			populateGrid(size = MEDIUM);
		}
		
		if (e.getSource() == largeSize) //change colony size to large
		{
			if(running)
				clear();
			
			populateGrid(size = LARGE);
		}
		
		if (e.getSource() == helpMenuItem) //Help was clicked
		{
			JOptionPane.showMessageDialog(null, "                                                The Rules\n\nFor a space that is 'populated'\n     ·Each cell with one or no neighbors dies, as if by loneliness.\n     ·Each cell with four or more neighbors dies, as if by overpopulation.\n     ·Each cell with two or three neighbors survives.\n\nFor a space that is 'empty' or 'unpopulated'\n     ·Each cell with three neighbors becomes populated.");
		}
		
		if (e.getSource() == controlsMenuItem) //Controls was clicked
		{
			JOptionPane.showMessageDialog(null, "{1, 2, 3} - Change simulation speed\n{F1} - clear grid\n{SPACE} - start/stop simulation\n{RIGHT} - advance colony one generation");
		}
		
		if (e.getSource() == aboutMenuItem) //About was clicked
		{
			JOptionPane.showMessageDialog(null, "Conway's Game of Life\nCoded by lunch\n2012");
		}
		
		requestFocus(); //frame grabs focus to prevent key conflicts
	}
	
	/**
	 * Constructs a colony based on the points parameter by clearing the grid then flipping every specified point
	 * @param points
	 */
	private void loadtemplate(Point[] points) {
		clear();
		for (Point p: points)
			grid[p.y][p.x].flip();

	}



	private void clear() { //clear the grid and reset the program
		if (running)
			actionPerformed(new ActionEvent(startButton, 01, null));
		
		for (int i = 0; i < grid.length;i++)
			for (int j = 0; j < grid.length; j++)
				grid[i][j].setAlive(false);
		currentGeneration = 0;
		setStatus("Program ready");
		repaint();
	}

	public void populateGrid(int size) //build the grid using the specified size
	{
		if (gridPanel != null)
			remove(gridPanel);
		grid = new Cell[size][size];
		gridPanel = new JPanel(new GridLayout(size, size));

		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				grid[i][j] = new Cell(i, j);
				grid[i][j].addMouseListener(this);

				gridPanel.add(grid[i][j]);
			}
		}
		add(gridPanel, BorderLayout.CENTER);
		buffer = new boolean[size][size];
		validate();
		//setVisible(true);
	}
	
	public void buildBuffer() //buffer is constructed from the current colony state
	{
		for (int i = 0; i < grid.length;i++)
			for (int j = 0; j < grid.length; j++)
			buffer[i][j] = grid[i][j].isAlive();
	}
	
	/**
	 * Runs the simulation by stepping then delaying by speedFactor. Runs until colony
	 * is dead or user stops the simulation.
	 * Simulation runs in its own thread.
	 */
	public void simulate()
	{
		new Thread()
		{
			public void run()
			{
				while (running)
				{
					step();
					try {
						switch(speedBox.getSelectedIndex())
						{
						case 0: //slow
							speedFactor = 1000;
							break;
						case 1: //fast
							speedFactor = 100;
							break;
						case 2: //hyper
							speedFactor = 20;
							break;
						}
						Thread.sleep(speedFactor);
					} catch (InterruptedException e) {

					}
				}
			}
		}.start();
	}
	
	/**
	 * Advance the colony by one generation. The original grid is not changed until
	 * the entire colony has been processed. Once the rules have been applied, the
	 * buffer contains the state of the next generation. This is then transferred to
	 * the grid and drawn to screen.
	 */
	public void step()
	{
		aliveCount = 0;
		buildBuffer();
		for (int i = 0; i < grid.length;i++)
			for (int j = 0; j < grid.length; j++)
			{
				int neighbours = getNeighbourCount(grid[i][j]);
				if (grid[i][j].isAlive())
				{
					//RULE 1
					if (neighbours < 2)
						buffer[i][j] = false;
					//RULE 2
					else if (neighbours == 2 || neighbours == 3)
						continue; //cell lives on
					//RULE 3
					else if (neighbours > 3)
						buffer[i][j] = false;
				}
				else
				{
					//RULE 4
					if (neighbours == 3)
						buffer[i][j] = true;
				}
			}//GENERATION FINISHED
		nextGeneration(buffer);
		currentGeneration++;
		setStatus("Current generation: " + currentGeneration + " | Lifeform count: " + aliveCount);
		repaint();
		
		if (aliveCount == 0)
		{
			clear();
			JOptionPane.showMessageDialog(null, "Your colony is dead");
		}
	}
	
	/**
	 * Transfer the buffer state to the grid
	 * @param buffer
	 */
	private void nextGeneration(boolean[][] buffer) {
		for (int i = 0; i < grid.length;i++)
			for (int j = 0; j < grid.length; j++)
			{
				grid[i][j].setAlive(buffer[i][j]);
				if(buffer[i][j])
					aliveCount++;
			}
	}
	
	/**
	 * Counts the number of alive cells adjacent to the specified cell. Out-of-bounds cells are simply
	 * ignored and thus do not contribute to the neighbour count
	 * @param c
	 * @return
	 */
	public int getNeighbourCount(Cell c)
	{
		int count = 0;
		int x = c.getXPos();
		int y = c.getYPos();

			try {
				if (grid[x-1][y-1].isAlive())
					count++;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			
			try {
				if (grid[x][y-1].isAlive())
					count++;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			
			try {
				if (grid[x+1][y-1].isAlive())
					count++;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			
			try {
				if (grid[x-1][y].isAlive())
					count++;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			
			try {
				if (grid[x+1][y].isAlive())
					count++;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			
			try {
				if (grid[x-1][y+1].isAlive())
					count++;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			
			try {
				if (grid[x][y+1].isAlive())
					count++;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
			
			try {
				if (grid[x+1][y+1].isAlive())
					count++;
			} catch (ArrayIndexOutOfBoundsException e) {
			}
		
		return count;
	}
	
	/**
	 * Changes the statusbar message
	 * @param m - the new message
	 */
	public void setStatus(String m)
	{
		statusBar.setStatus(m);
	}
	
	public static void main(String[] args)
	{
		java.awt.EventQueue.invokeLater(new Runnable() {
		    @Override
		    public void run() {
				new GameOfLife();
		    }
		});
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		//Clicked on a single cell, flip that cell
		Cell clickedObject = (Cell) e.getSource();
		if (clickedCell != null && clickedObject != clickedCell)
			clickedObject.flip();
			repaint();	
	}

	@Override
	//while holding down the left mouse button, cells can be created or killed by dragging the mouse.
	//Creation/Destruction is determined by the state of the first-clicked cell
	public void mouseEntered(MouseEvent e) {
		
		if (currentMode == CREATING_MODE) 
		{
			Cell hoverCell = (Cell) e.getSource();
			if (!hoverCell.isAlive())
				hoverCell.flip();
			repaint();
		}
		if (currentMode == KILLING_MODE)
		{
			Cell hoverCell = (Cell) e.getSource();
			if (hoverCell.isAlive())
				hoverCell.flip();
			repaint();
		}
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		//If the mouse button is depressed, moving the mouse either creates or destroys
		//cells, depending on what the state of the initial clicked cell was
		Cell clickedObject = (Cell) e.getSource();
		clickedCell = clickedObject;
			if (clickedObject.isAlive())
				currentMode = KILLING_MODE;
			else
				currentMode = CREATING_MODE;
			clickedObject.flip();
			repaint();
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		currentMode = IDLING;
		
	}
	
	/**
	 * Keyboard shortcuts are handled by this KeyEventDispatcher. Key presses are handled this way so that
	 * they are globally captured
	 */
	private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                switch(e.getKeyCode())
                {
                case KeyEvent.VK_1:
                	speedBox.setSelectedIndex(0);
                	break;
                	
                case KeyEvent.VK_2:
                	speedBox.setSelectedIndex(1);
                	break;
                	
                case KeyEvent.VK_3:
                	speedBox.setSelectedIndex(2);
                	break;
                	
                case KeyEvent.VK_F1:
                	clear();
                	break;
                	
                case KeyEvent.VK_ESCAPE:
                	System.exit(0);
                	break;
                	
                case KeyEvent.VK_SPACE:
                	actionPerformed(new ActionEvent(startButton, 12, null));
                	break;
                	
                case KeyEvent.VK_RIGHT:
                	step();
                	break;
                }
            }
            /*
            else if (e.getID() == KeyEvent.KEY_RELEASED) {
                System.out.println("2test2");
            } 
            else if (e.getID() == KeyEvent.KEY_TYPED) {
                System.out.println("3test3");
            }
            */
            return false;
        }
    }

	
	
	/**
	 * Each cell contains a boolean determining whether or not it is alive, as well
	 * as its x and y coordinates in the grid
	 */
	private class Cell extends JPanel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private boolean alive;
		int x, y;
		
		public Cell(int x, int y)
		{
			alive = false;
			this.x = x;
			this.y = y;
			
			setLayout(null);
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}

		public boolean isAlive() {
			return alive;
		}
		
		public void setAlive(boolean alive)
		{
			this.alive = alive;
		}
		
		public void flip() //quick transition from dead to alive and vice versa
		{
			if (alive)
				alive = false;
			else
				alive = true;
		}

		public int getXPos() {
			return x;
		}

		public int getYPos() {
			return y;
		}
		
		public void paintComponent(Graphics g)
		{
			g.setColor(cellColor);
			if (alive)
				g.fill3DRect(0, 0, getWidth(), getHeight(), true);
		}
	}
}
