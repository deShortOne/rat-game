import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author 2010573
 *
 */
public class PathTile extends Tile {
	
	/** Pick definition
	 * Will go through list of rats on tile and tell the rat class where to go
	 *  and tile class which rats are going to it and from what direction
	 *  
	 * Tells rats on this tile which direction to go and other tile 
	 * 	class which rats are going to it and from what direction
	 */
	@Override
	public void getNextDirection() {
		for (Direction prevDirection : currBlock.keySet()) {			
			ArrayList<Rat> ratList = currBlock.get(prevDirection);
			if (!ratList.isEmpty()) {

				Direction goTo;
				if (directions[0] == prevDirection) {
					goTo = directions[1];
				} else {
					goTo = directions[0];
				}
		
				int ratsGetThrough; //Number of rats that can keep going onwards
				if (neighbourTiles.get(goTo).isTileBlocked()) {
					ratsGetThrough = damageStopSign(currBlock.get(prevDirection).size());
				} else {
					ratsGetThrough = currBlock.get(prevDirection).size();
				}
				
				int i;
				for (i = 0; i < ratsGetThrough; i++) {
					//Tell rat to go to previous direction
					//Tell that Tile the direction the rat came from using prevDirection.opposite()
				}
				for (; i < ratList.size(); i++) {
					//Tell rat to go to previous direction
					//Tell that Tile the direction the rat came from using goTo.opposite()
				}
				
			}
		}
		currBlock = nextBlock;
		nextBlock = new HashMap<>();
	}
	
	/**
	 * Add rat from other tile to this tile
	 * @param r rat to be added to this Tile
	 * @param d direction the rat came from
	 */
	public void addRat(Rat r, Direction d) {
		nextBlock.putIfAbsent(d, new ArrayList<Rat>());
		nextBlock.get(d).add(r);
		
		/* ArrayList<Rat> r (Adding a whole list of rats instead of one by one)
		nextBlock.putIfAbsent(d, new ArrayList<Rat>());
		if (nextBlock.get(d).isEmpty()) {
			nextBlock.put(d, r);
		} else {
			nextBlock.get(d).addAll(r);
		}
		*/
	}
	
	
}
