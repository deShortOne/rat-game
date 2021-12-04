import java.util.ArrayList;
import java.util.HashMap;

/**
 * A standard tile that has at most 2 tiles connected to it.
 * 
 * @author Jing Shiang Gu
 *
 */
public class PathTile extends Tile {

	/**
	 * Constructor to set its location
	 * 
	 * @param x number of tiles away from the left
	 * @param y number of tiles away from the top
	 */
	public PathTile(int x, int y) {
		super(new int[] { x, y });
	}

	// Might need to split up this tile, giveItemsToRat should already have
	// aliveRats list.
	// Return list of death rats on this tile.
	@Override
	public ArrayList<DeathRat> getNextDeathRat() {
		// Check number of rats and number of lists of rats to just assign it if needed.

		// Pass in ArrayList of rats on this tile. Should be moved to giveItemToRat
		aliveRats = new ArrayList<>();
		for (Direction prevDirection : currBlock.keySet()) {
			aliveRats.addAll(currBlock.get(prevDirection));
		}
		
		int beforeDeathInter = aliveRats.size();
		
		// Pass in ArrayList of Rats still alive to each DeathRat on the tile
		for (Direction prevDirection : currDeath.keySet()) {
			for (DeathRat dr : currDeath.get(prevDirection)) {
				aliveRats = dr.killRats(aliveRats, -1);
			}
		}

		// Now moves death rats
		ArrayList<DeathRat> drs = new ArrayList<>();
		for (Direction prevDirection : currDeath.keySet()) {
			Direction goTo = directions[0] == prevDirection ? directions[1] : directions[0];
			for (DeathRat dr : currDeath.get(prevDirection)) {
				if (dr.isAlive()) {

					Tile t;
					int i;
					do {
						t = neighbourTiles.get(goTo);
						i = t.numsRatsCanEnter(this, 1);

						Direction tmp = goTo;
						goTo = prevDirection;
						prevDirection = tmp;
					} while (i == 0);

					// Need better logic for this
					t.moveDeathRat(dr, prevDirection.opposite());
					drs.add(dr);
					dr.initalMove(X_Y_POS, prevDirection);
				}

			}
		}

		// Remove fallen rats from list.
		// Will not automatically move the rats in case other death rats come here.
		if (aliveRats.isEmpty()) {
			currBlock = new HashMap<>();
		} else if (aliveRats.size() == beforeDeathInter) {
			// Interesting as to why there is no change...
			System.err.println("aliveRats list has not changed!" + X_Y_POS);
		} else { 
			// Could theoterically still decrease by comparing the difference
			// from before and now. ArrayList keeps order so chances are, all rats
			// from one direction will be eliminated before other direction is
			for (Direction prevDirection : currBlock.keySet()) {
				ArrayList<Rat> tmp = new ArrayList<>();
				ArrayList<Rat> rs = currBlock.get(prevDirection);
				if (rs != null) {
					for (Rat r : rs) {
						if (exists(r)) {
							tmp.add(r);
						}
					}
					currBlock.put(prevDirection, tmp);
				}
			}
		}
		return drs;
	}

	/**
	 * Returns {@code true} if Rat is in aliveRats list.
	 * 
	 * @param r the rat to find
	 * @return {@code true} if rat exists in list
	 */
	private boolean exists(Rat r) {
		return aliveRats.remove(r);
	}

	// Death rat and stop sign stopping rats from moving away hence moving towards
	// DR
	@Override
	public void moveDeathRat(DeathRat dr, Direction prevDirectionDR) {
		// Deal with all rats going towards Death Rat
		Direction dirToDeath = prevDirectionDR == directions[0] ? directions[1] : directions[0];
		ArrayList<Rat> currList = currBlock.get(dirToDeath);
		ArrayList<Rat> escaped = new ArrayList<>();
		if (currList != null) {
			// Baby rats are faster so it will reach Death Rat first
			for (Rat r : currList) {
				if (r.getStatus() == RatType.BABY) {
					if (dr.killRat(r, 2)) {
						Main.addCurrMovement(X_Y_POS, dirToDeath.opposite(), RatType.BABY, 2);
					} else {
						// Should be moved afterwards if more death rats come
						escaped.add(r);
					}
				} else {
					escaped.add(r);
				}
			}

			// Then Death Rats will deal with slower adult rats
			currList = escaped;
			escaped = new ArrayList<>();
			for (Rat r : currList) {
				if (dr.killRat(r, 3)) {
					Main.addCurrMovement(X_Y_POS, dirToDeath.opposite(), r.getStatus(), 1);
				} else {
					escaped.add(r);
				}
			}
			currBlock.put(dirToDeath, escaped);
		}

		// Now that all rats going towards DR from this tile are dealt with, deal with
		// any stragglers who are bounced back by stop sign IF DR is alive and stop sign
		// is present in next tile - basically same as before
		Direction goTo = prevDirectionDR == directions[0] ? directions[0] : directions[1];
		currList = currBlock.get(goTo);
		int ratsGoToDeath = -1;
		int beforeDeath = -1;
		if (dr.isAlive() && currList != null) {
			beforeDeath = currList.size();
			
			Tile tile = neighbourTiles.get(goTo);
			ratsGoToDeath = beforeDeath - tile.numsRatsCanEnter(this, beforeDeath);
			int i = 0;
			for (; i < ratsGoToDeath && i < beforeDeath; i++) {
				Rat r = currList.get(i);
				if (r.getStatus() == RatType.BABY) {
					if (dr.killRat(currList.get(i), 2)) {
						Main.addCurrMovement(X_Y_POS, dirToDeath.opposite(), RatType.BABY, 2);
					} else {
						escaped.add(r);
					}
				} else {
					escaped.add(r);
				}
			}
			// Add in rats that DR couldn't deal with since it died
			escaped.addAll(currList.subList(i, beforeDeath));
			currList = escaped;
			escaped = new ArrayList<>();
		}
		
		if (dr.isAlive() && currList != null) {
			int i = 0;
			for (; i < ratsGoToDeath && i < currList.size(); i++) {
				Rat r = currList.get(i); 
				if (dr.killRat(currList.get(i), 3)) {
					Main.addCurrMovement(X_Y_POS, dirToDeath.opposite(), r.getStatus(), 1);
				} else {
					escaped.add(r);
				}
			}
			escaped.addAll(currList.subList(i, currList.size()));
		}

		if (dr.isAlive()) {
			this.addRat(dr, prevDirectionDR);
		}
		currBlock.put(goTo, escaped);
	}

	/**
	 * Pick definition Will go through list of rats on tile and tell the rat class
	 * where to go and tile class which rats are going to it and from what
	 * direction.
	 * 
	 * Tells rats on this tile which direction to go and other tile class which rats
	 * are going to it and from what direction.
	 */
	@Override
	public void getNextDirection() {
		for (Direction prevDirection : currBlock.keySet()) {
			ArrayList<Rat> ratList = currBlock.get(prevDirection);

			if (!ratList.isEmpty()) {
				int i = giveRatItem(ratList.get(0)) ? 1 : 0;
				Direction goTo = directions[0] == prevDirection ? directions[1] : directions[0];
				int ratsGoForward; // Number of rats that can keep go in current direction

				while (i != ratList.size()) {
					Tile tile = neighbourTiles.get(goTo);

					ratsGoForward = tile.numsRatsCanEnter(this, ratList.size());
					for (; i < ratsGoForward; i++) {
						// Future want this to be a switch case statement ratList.get(i).getStatus()
						// should return a RatType
						if (ratList.get(i).isChild()) {
							Main.addCurrMovement(X_Y_POS, goTo, RatType.BABY, 4);
							tile.getAcceleratedDirection(ratList.get(i), goTo.opposite());
							// timeTravel(ratList.get(i)); //Speeds up aging of rat
						} else {
							if (ratList.get(i).getDeathRat()) {
								Main.addCurrMovement(X_Y_POS, goTo, RatType.DEATH, 4);
								tile.getAcceleratedDirection(ratList.get(i), goTo.opposite());
							} else {
								RatType gen = ratList.get(i).getIsMale() ? RatType.MALE : RatType.FEMALE;
								Main.addCurrMovement(X_Y_POS, goTo, gen, 4);
								tile.addRat(ratList.get(i), goTo.opposite());
							}

						}
					}

					Direction tmp = goTo;
					goTo = prevDirection;
					prevDirection = tmp;
				}
			}
		}
	}

	@Override
	public void getAcceleratedDirection(Rat r, Direction prevDirection) {
		// Direction goTo = directions[0] == prevDirection ? directions[1] :
		// directions[0];
		this.addRat(r, prevDirection.opposite());
	}

	// Debug Speeds up aging
	private void timeTravel(Rat r) {
		for (int i = 0; i < 45; i++) {
			r.incrementAge();
		}
	}
}
