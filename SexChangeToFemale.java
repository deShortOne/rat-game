import java.util.ArrayList;

import javafx.scene.image.Image;

/**
 * Class modelling a sex change item that turns males to females.
 * @author Salim, Andrew Wu
 */
public class SexChangeToFemale extends Item {

	/**
	 * Name of item.
	 */
	public static final String NAME = "toFemale";
	
	public static final Image IMAGE = new Image(Main.IMAGE_FILE_LOC + "SexChangeToFemale.png");
	
    /**
     * Health point of item.
     */
	public SexChangeToFemale() {
		hp = 1;
	}
	
	public SexChangeToFemale(int hp) {
		this.hp = hp;
	}
	
    /**
     * Sets the gender of a rat to male regardless of it's starting gender.
     * Removes 1 from hp when used.
     * @param r a Rat object.
     * @return ArrayList of alive rats on the tile this item was invoked on.
     */
	@Override
	public ArrayList<Rat> itemAction(ArrayList<Rat> r) {
		r.get(0).setIsMale(false);
		hp--;
		return r;
	}
	
	@Override
	public String toString() {
		String out = NAME + Main.FILE_SUB_SEPERATOR + hp;
		return out;
	}
}
