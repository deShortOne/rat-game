import java.util.ArrayList;

/**
 * Class modelling a sex change item that turns females to males.
 * @author Salim, Andrew Wu
 * */

public class SexChangeToMale extends Item {

    /**
     * Sets item health to 1.
     */
	public SexChangeToMale() {
		hp = 1;
	}

    /**
     * Sets the gender of a rat to male regardless of it's starting gender.
     * Removes 1 from hp when used.
     * @param r a Rat object.
     * @return ArrayList of alive rats on the tile this item was invoked on.
     */
	@Override
	public ArrayList<Rat> itemAction(ArrayList<Rat> r) {
		r.get(0).setIsMale(true);
		hp--;
		return r;
	}
}
