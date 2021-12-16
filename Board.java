import java.util.ArrayList;
import java.util.Set;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author Jing Shiang Gu
 *
 */
public class Board {

	/**
	 * The map in string format.
	 */
	private String mapDesign;

	/**
	 * The width of the map.
	 */
	private int xHeight;

	/**
	 * The height of the map
	 */
	private int yHeight;

	/**
	 * The 2d array of the map. Static so other classes can access this
	 */
	private static Tile[][] board;

	/**
	 * List of all tiles on board.
	 */
	private static ArrayList<Tile> allTiles;
	
	/**
	 * List of death rats.
	 */
	private static ArrayList<DeathRat> deathRatBuffer;

	// ? Is final in the correct place? Should this be public?
	/**
	 * Constants of Tile letters from string to Grass Tile, current implementation
	 * sets this to be {@code null}.
	 */
	private static final char GRASS_TILE = 'G';

	/**
	 * Constants of Tile letters from string to Path Tile.
	 */
	private final static char PATH_TILE = 'P';

	/**
	 * Constants of Tile letters from string to Junction Tile.
	 */
	private final static char JUNCTION_TILE = 'J';

	/**
	 * Constants of Tile letters from string to Tunnel Tile. TODO
	 */
	private final static char TUNNEL_TILE = 'T';

	/**
	 * Number of tiles in between each visible tile +1 (so 2 means 1 extra tile in between)
	 * FIXME -> Will need to make sure all tiles have CORRECT xy pos excluding LightTile.
	 */
	public final static int EXTRA_PADDING = 2;

	/**
	 * Constructs a {@code Board} from input string.
	 * 
	 * @param mapDesign input string of board
	 * @param xHeight   max width of board
	 * @param yHeight   max height of board
	 */
	public Board(String mapDesign, int xHeight, int yHeight) {
		this.mapDesign = mapDesign;
		this.xHeight = xHeight;
		this.yHeight = yHeight;
		try {
			Board.board = new Tile[yHeight * EXTRA_PADDING][xHeight * EXTRA_PADDING];
		} catch (Exception e) {
			e.printStackTrace();
		}
		allTiles = new ArrayList<>();
		createBoard();
		eliminateEmpties();
		createGraph();
	}

    /**
     * Checks if item can be placed on tile. Can be placed if tile is an instance of path but not
     * a tunnel. Also if tile is a junction.
     * @param x x-coordinate being checked.
     * @param y y-coordinate being checked.
     * @return boolean of if item can be placed on tile.
     */
    private static boolean isPlaceableTile(Tile t) {
        if (t == null) {
			return false;
		}  else if (t instanceof TunnelTile) {
			return false;
		}
        return true;
    }

	/**
	 * Adds effect of stop sign to tile
	 * @param x x position of tile on map
	 * @param y y position of tile on map
	 * @return {@code true} if stop sign can be placed
	 */
	public boolean addStopSign(int x, int y) {
		Tile t = board[y * EXTRA_PADDING][x * EXTRA_PADDING];
		if (isPlaceableTile(t)) {
			return t.setTileItem(new StopSign(new int[] {y, x}));
		}
		return false;
	}

	/**
	 * Adds bomb to tile.
	 * @param x x position of tile on map
	 * @param y y position of tile on map
	 * @return if bomb can be placed at that location.
	 */
	public boolean addBomb(int x, int y) {
        Tile t = board[y * EXTRA_PADDING][x * EXTRA_PADDING];
        if (isPlaceableTile(t)) {
			return t.setTileItem(new Bomb(new int[] {y, x}));
		}
        return false;
	}

    /**
     * Blows up tiles from origin in a row until "null" Tile reached.
     * @param x x-coordinate bomb was placed on
     * @param y y-coordinate bomb was placed on
     */
    public static void detonate(int x, int y) {
        y *= EXTRA_PADDING;
        x *= EXTRA_PADDING;
        int startY = y;
        int startX = x;

        while (board[y][x] != null) {
        	board[y][x].blowUp();
            y--;
        }

        y = startY + 1;
        x = startX;
        while (board[y][x] != null) {
        	board[y][x].blowUp();
            y++;
        }

        y = startY;
        x = startX - 1;
        while (board[y][x] != null) {
        	board[y][x].blowUp();
            x--;
        }

        y = startY;
        x = startX + 1;
        while (board[y][x] != null) {
        	board[y][x].blowUp();
            x++;
        }
    }

    /**
     * Adds poison item to tile.
     * @param x x-coordinate of tile.
     * @param y y-coordinate of tile.
     */
    public boolean addPoison(int x, int y) {        
		Tile t = board[y * EXTRA_PADDING][x * EXTRA_PADDING];
		if (isPlaceableTile(t)) {
			return t.setTileItem(new Poison());
		}
		return false;
    }

    /**
     * Adds sex change (Male to Female) item to tile.
     * @param x x-coordinate of tile.
     * @param y y-coordinate of tile.
     */
    public boolean addSexToFemale(int x, int y) {        
        Tile t = board[y * EXTRA_PADDING][x * EXTRA_PADDING];
		if (isPlaceableTile(t)) {
			return t.setTileItem(new SexChangeToFemale());
		}
		return false;
    }

    /**
     * Adds sex change (Female to Male) item to tile.
     * @param x x-coordinate of tile.
     * @param y y-coordinate of tile.
     */
    public boolean addSexToMale(int x, int y) {
    	Tile t = board[y * EXTRA_PADDING][x * EXTRA_PADDING];
		if (isPlaceableTile(t)) {
			return t.setTileItem(new SexChangeToMale());
		}
		return false;
    }

    /**
     * Adds sterilise item to tile.
     * @param x x-coordinate of tile.
     * @param y y-coordinate of tile.
     */
    public boolean addSterilise(int x, int y) {
    	Tile t = board[y * EXTRA_PADDING][x * EXTRA_PADDING];
		if (isPlaceableTile(t)) {
			return t.setTileItem(new Sterilisation());
		}
		return false;
    }

    /**
     * Adds a new Death Rat on tie from specified coordinates. Won't return 
     * boolean like other items because DR can always be added.
     * 
     * @param x x position of tile
     * @param y y position of tile
     */
    public void addDeathRat(int x, int y) {
        placeRat(new DeathRat(), Direction.NORTH, y, x);
    }
    
    /**
     * Adds a new Gas item on tile from specified coordinates.
     * 
     * @param x x position of tile
     * @param y y position of tile
     * @return {@code true} if gas can be placed here.
     */
    public boolean addGas(int x, int y ) {
    	Tile t = board[y * EXTRA_PADDING][x * EXTRA_PADDING];
    	if (isPlaceableTile(t)) {
    		return t.setTileItem(new Gas(x, y));
    	}
    	return false;
    }
    
    /**
     * Removes item from tile. Should only be used to remove Gas.
     * 
     * @param del list of positions of the item.
     */
    public static void clearGas(int x, int y) {
    	board[y * EXTRA_PADDING][x * EXTRA_PADDING].clearGas();
    }
    /**
     * Attempt to spread gas from an origin x y point.
     * 
     * @param x 	x position of the gas
     * @param y 	y position of the gas
     * @param hp	hp of the new gas item
     */
    public static void spreadGas(int x, int y, int hp) {    
    	
    	int boardX = x * EXTRA_PADDING;
        int boardY = y * EXTRA_PADDING;

        Tile t;
        
        t = board[boardY - EXTRA_PADDING][boardX];
        if (isPlaceableTile(t)) {
        	if (t.setTileItem(new Gas(x, y - 1, hp))) {
        		Main.addGas(x, y - 1);
        	}
        }

        t = board[boardY + EXTRA_PADDING][boardX];
        if (isPlaceableTile(t)) {
        	if (t.setTileItem(new Gas(x, y + 1, hp))) {
        		Main.addGas(x, y + 1);
        	}
        }
        
        t = board[boardY][boardX - EXTRA_PADDING];
        if (isPlaceableTile(t)) {
        	if (t.setTileItem(new Gas(x - 1, y , hp))) {
        		Main.addGas(x - 1, y);
        	}
        }
        
        t = board[boardY][boardX + EXTRA_PADDING];
        if (isPlaceableTile(t)) {
        	if (t.setTileItem(new Gas(x + 1, y, hp))) {
        		Main.addGas(x + 1, y);
        	}
        }
    }

	/**
	 * Draws board onto game window.
	 * 
	 * @param gc Canvas to draw the board on
	 */
	public void drawBoard(GraphicsContext gc) {
		
		Image grassImage = new Image("Grass.png");
		Image[] tunnelImagesEntrance = new Image[4];
		for(int i = 0; i < 4; i++) {
			tunnelImagesEntrance[i] = new Image("/img/Tunnel" + i + ".png");
		}
		Image[] tunnelImages = new Image[2];
		for(int i = 0; i < 2; i++) {
			tunnelImages[i] = new Image("/img/TunnelF" + i + ".png");
		}
		
		int x = 0;
		int y = 0;
		
		for (int i = 0; i < yHeight * EXTRA_PADDING; i += EXTRA_PADDING) {
			for (int j = 0; j < xHeight * EXTRA_PADDING; j += EXTRA_PADDING) {
				if (board[i][j] == null) {
					gc.drawImage(grassImage, 
							x++ * Main.TILE_SIZE, 
							y * Main.TILE_SIZE, 
							Main.TILE_SIZE,
							Main.TILE_SIZE);
				} else if (board[i][j] instanceof TunnelTile) {
					Image t = new Image("/img/tile.png");
									
					
					// Check for NESW entrance
					if (board[i - EXTRA_PADDING][j] != null) {
						if (!(board[i - EXTRA_PADDING][j] instanceof TunnelTile)) {
							t = tunnelImagesEntrance[0];
						} else {
							t = tunnelImages[0];
						}
					} 
					if (board[i][j + EXTRA_PADDING] != null) {
						if (!(board[i][j + EXTRA_PADDING] instanceof TunnelTile)) {
							t = tunnelImagesEntrance[1];
						} else {
							t = tunnelImages[1];
						}
					}
					if (board[i + EXTRA_PADDING][j] != null) {
						if (!(board[i + EXTRA_PADDING][j] instanceof TunnelTile)) {
							t = tunnelImagesEntrance[2];
						} 
					} 
					if (board[i][j - EXTRA_PADDING] != null) {
						if (!(board[i][j - EXTRA_PADDING] instanceof TunnelTile)) {
							t = tunnelImagesEntrance[3];
						}
					} 
					gc.drawImage(t, 
							x++ * Main.TILE_SIZE, 
							y * Main.TILE_SIZE, 
							Main.TILE_SIZE,
							Main.TILE_SIZE);
				} else {
					x++;
				}
			}
			x = 0;
			y++;
		}
	}

	/**
	 * Put Rat onto game canvas.
	 * 
	 * @param rats the rat that's going to the next tile
	 * @param dir  direction the rat is came from
	 * @param x    x start position of the rat
	 * @param y    y start position of the rat
	 * 
	 */
	public void placeRat(Rat rats, Direction dir, int x, int y) {
		board[x * EXTRA_PADDING][y * EXTRA_PADDING].addRat(rats, dir); 
	}

	/**
	 * Put Death Rat onto game canvas.
	 * 
	 * @param rat death rat to be added to map
	 * @param dir direction the death rat came from
	 * @param x x start position of the death rat
	 * @param y y start position of the death rat
	 */
	public void placeRat(DeathRat rat, Direction dir, int x, int y) {
		board[x * EXTRA_PADDING][y * EXTRA_PADDING].addRat(rat, dir);
	}

	/**
	 * Goes through each Tile and moves the rat to the next tile before getting the
	 * tiles new list.
	 */
	public void runAllTiles() {
		// Send item to Rat make sure to have boolean to know if it is dead or not
		deathRatBuffer = new ArrayList<>();
		
		// Movement/ make sure it's interacting with correct Rats
		// Give the rat item and keep alive rats
		// Have the rats interact with each other
		for (Tile t : allTiles) {
			t.setCurrRat();
		}
		
		// Secondly move Death rats to kill any rats in its path
		for (Tile t : allTiles) {
			deathRatBuffer.addAll(t.getNextDeathRat());
		}
		
		// Before moving all other rats
		for (Tile t : allTiles) {
			t.getNextDirection();
		}
		
		// Now adding in death rat movements
		for (DeathRat dr : deathRatBuffer) {
			Main.addCurrMovement(dr.getXyPos(), dr.getD(), RatType.DEATH, dr.getMove());
		}
	}
	
	/**
	 * Adds in rats from string format.
	 * @param rats list of rats to be added in
	 */
	public void setUpRats(ArrayList<String> rats) {
		for (String str : rats) {
			Rat createR;
			String[] spl = str.split(";");
			String[] splD = spl[1].split(",");
			Direction d = Direction.toD(Integer.parseInt(splD[0]));
			if (spl[0].split(",")[0].equals(DeathRat.NAME)) {
				if (spl[0].length() == 1) {
					placeRat(new DeathRat(), d, Integer.parseInt(splD[1]), Integer.parseInt(splD[2])); // Only for new start of levels
				} else {
					placeRat(new DeathRat(Integer.parseInt(spl[0].split(",")[1])), d, Integer.parseInt(splD[1]), Integer.parseInt(splD[2]));
				}
			} else {
				if (spl[0].length() == 1) {
					createR = new Rat(spl[0].equals("M")); // Only for new start of levels
				} else {
					createR = RatController.addRat(spl[0]);
				}
				placeRat(createR, d, Integer.parseInt(splD[1]), Integer.parseInt(splD[2]));
			}
		}
	}
	
	/**
	 * Adds in items from string format.
	 * @param items list of items to be added in
	 */
	public void setUpItems(ArrayList<String> items) {
		if (items != null) {
			for (String i : items) {
				String[] it = i.split(";");
				String[] desc = it[0].split(",");
				String[] loc = it[1].split(",");
				int x = Integer.parseInt(loc[1]) * EXTRA_PADDING;
				int y = Integer.parseInt(loc[0]) * EXTRA_PADDING;
				Item item = Item.toItem(desc[0], Integer.parseInt(desc[1]), 
						new int[] {x / EXTRA_PADDING, y / EXTRA_PADDING});
				
				Tile t = board[y][x];
				t.setTileItem(item);
				ItemType.fromItem(item).add(x / EXTRA_PADDING, y / EXTRA_PADDING, item.getState());
				if (item instanceof TimeItem) {
					((TimeItem) item).itemAction();
				}
			}
		}
	}
	
	/**
	 * Takes in a filename to save the board state to.
	 * @param filename the file to save to
	 */
	public void saveState(String filename) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(filename);
			
			for (Tile t : allTiles) {
				ArrayList<String> rats = t.getRats();
				for (String r : rats) {
					out.print(r + "\n");
				}
			}
			for (Tile t : allTiles) {
				out.print(t.getItem());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}  finally {
			out.close();
		}
	}

	/**
	 * Creates 2d array of the map.
	 */
	private void createBoard() {
		int counter = 0;
		for (int i = 0; i < yHeight * EXTRA_PADDING; i++) {
			for (int j = 0; j < xHeight * EXTRA_PADDING; j++) {
				switch (mapDesign.charAt(counter++)) {
				case GRASS_TILE -> board[i][j] = null;
				case PATH_TILE -> board[i][j] = new PathTile(i, j);
				case JUNCTION_TILE -> board[i][j] = new JunctionTile(i, j);
				case TUNNEL_TILE -> board[i][j] = new TunnelTile(i, j);
				default -> { System.out.println("Map error!"); System.exit(0);}
				}
				board[i][++j] = new LightTile(i, j);
			}
			i++;
			for (int j = 0; j < xHeight * EXTRA_PADDING; j++) {
				board[i][j] = new LightTile(i, j);
			}
		}

	}

	/**
	 * Eliminate LightTiles that aren't connected to enough good tiles.
	 */
	private void eliminateEmpties() {
		for (int i = 0; i < yHeight * EXTRA_PADDING; i++) {
			for (int j = 0; j < xHeight * EXTRA_PADDING; j++) {
				if (board[i][j] instanceof LightTile) {
					int counter = 0;
					if (i != 0) {
						counter += check(board[i - 1][j]) ? 1 : 0;
					}
					if (i != yHeight * EXTRA_PADDING - 1) {
						counter += check(board[i + 1][j]) ? 1 : 0;
					}

					if (j != 0) {
						counter += check(board[i][j - 1]) ? 1 : 0;
					}
					
					if (j != xHeight * EXTRA_PADDING - 1) {
						counter += check(board[i][j + 1]) ? 1 : 0;
					}

					if (counter < 2) {
						board[i][j] = null;
					}
				}
			}
		}
	}

	/**
	 * Checks if item can be added to this tile.
	 * 
	 * @param t tile to check
	 * @return {@code true} if item can be placed on this tile
	 */
	private boolean check(Tile t) {
		if (t == null) {
			return false;
		} else if (t instanceof LightTile) {
			return false;
		}
		return true;
	}

	/**
	 * Converts the map into a graph.
	 */
	private void createGraph() {
		for (int i = 0; i < yHeight * EXTRA_PADDING; i++) {
			for (int j = 0; j < xHeight * EXTRA_PADDING; j++) {

				if (board[i][j] != null) {
					allTiles.add(board[i][j]);
					ArrayList<Tile> tiles = new ArrayList<>(4);
					ArrayList<Direction> direction = new ArrayList<>(4);

					// Check North
					if (i != 0) {
						if (board[i - 1][j] != null) {
							tiles.add(board[i - 1][j]);
							direction.add(Direction.NORTH);
						}
					}

					// Check East
					if (j != xHeight * EXTRA_PADDING - 1) {
						if (board[i][j + 1] != null) {
							tiles.add(board[i][j + 1]);
							direction.add(Direction.EAST);
						}
					}

					// Check South
					if (i != yHeight * EXTRA_PADDING - 1) {
						if (board[i + 1][j] != null) {
							tiles.add(board[i + 1][j]);
							direction.add(Direction.SOUTH);
						}
					}

					// Check West
					if (j != 0) {
						if (board[i][j - 1] != null) {
							tiles.add(board[i][j - 1]);
							direction.add(Direction.WEST);
						}
					}
					board[i][j].setNeighbourTiles(tiles.toArray(
							new Tile[2]), direction.toArray(new Direction[2]));
				}
			}
		}

	}

}
