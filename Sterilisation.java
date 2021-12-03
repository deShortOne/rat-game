/**
 * Item to make rats sterile.
@author Andrew
 */
public class Sterilisation extends Item {
    /**
     * Checks if Rat object is sterile, if so, sets it to sterile.
     * @param rat a Rat Object
     */
    public void itemAction(Rat rat) {
        if (rat.getSterile() == true) {
            rat.sterilise();
            System.out.println("Rat sterilised");
        }
    }
}