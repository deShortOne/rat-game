import java.util.ArrayList;
import java.util.Arrays;

import javafx.application.Application;

/**
 * Test class for Board.java. Use for test only.
 * 
 * @author Jing Shiang Gu deprecated
 */
public class MainBoardTester {

	// static String map1 = "GGGGGPPGGGGG"; // 4 3

	// 31 16
	// static String map2 =
	// "GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGPPPPPPJPPPPPPPGGGGPPPPPPPPPPPGGPGGGGGPGGGGGGPGGGGPGGGGGGGGGPGGPGGGGGPGGGGGGPGGGGPGGGGGGGGGPGGPGGGGGPGGGGGGPPPPPJGGGGGGGGGPGGJPPPGGPGGGGGGGGGGGPGGGGGGGGGPGGPGGPGGPGGGGGGGGGGGPGGGGGGGGGPGGPGGPGGPGGGGPPPPPPPJPPPPPPPPPJGGPPPPPPPGGGGPGGGGGGPGGGGGGGGGPGGPGGGGGGGGGGPGGGGGGPGGGGGGGGGPGGPGGGGGGGGGGPGGGGGGPGGGGGGGGGPGGPPPPPPPPPPPPGGGGGGPPPPPPPPPPPG";

	// 17 11
	static String properMap1 = "GGGGGGGGGGGGGGGGGGPPPPPPPJPPPPPPPGGPGGGGGGPGGGGGGPGGPGGGGGGPGGGGGGPGGPGGGGGGPGGGGGGPGGJPPPPPPJPPPPPPJGGPGGGGGGPGGGGGGPGGPGGGGGGPGGGGGGPGGPGGGGGGPGGGGGGPGGPPPPPPPJPPPPPPPGGGGGGGGGGGGGGGGGG";
	public static Main BBOAD;


	public static void main(String[] args) {
		Board m = new Board(properMap1, 17, 11);
		System.out.println(Board.getBoard()[2][2].X_Y_POS);
		m.addStopSign(1, 1);
		//print2dArray(m.getBoard());
		//System.out.println();
		//m.eliminateEmpties();
		//print2dArray(m.getBoard());
		System.out.println("Done!");
	}

	public static Tile[][] create2dArray(String map, int x, int y) {
		Board m = new Board(map, x, y);
		//m.eliminateBadInvisTiles();
		return m.getBoard();
	}

	public static void print2dArray(Tile[][] tile) {
		for (Tile[] tileY : tile) {
			for (Tile tileX : tileY) {
				if (tileX instanceof PathTile) {
					System.out.print("P");
				} else if (tileX instanceof JunctionTile) {
					System.out.print("J");
				} else if (tileX instanceof LightTile) {
					System.out.print("L");
				} else {
					System.out.print("G");
				}
			}	
			//else if (tileX instanceof LightTile) {
			//System.out.print("L");					
		//} 
			System.out.println();
		}
	}
}
