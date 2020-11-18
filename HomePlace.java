import java.lang.Math;
import java.lang.NumberFormatException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

// Model classes

/**
 * HomePlaces are occupied by any type of person
 * @see Place
 * @see Person
 */
class HomePlace extends Place {
    private final LinkedList <Person> residents = new LinkedList <Person> ();

    // transmissivity median and scatter for homes
    // BUG --  These should come from model description file, not be hard coded
    private static final double transMed = 0.03 * Simulator.hour;
    private static final double transScat = 0.02 * Simulator.hour;

    // need a source of random numbers
    private static final MyRandom rand = MyRandom.stream();

    /** The only constructor for Place
     *  Places are constructed with no occupants
     */
    public HomePlace() {
	super(); // initialize the underlying place
	super.transmissivity = rand.nextLogNormal( transMed, transScat );
    }

    /** Add a resident to a place
     *  Should only be called from the person constructor
     *  @param r a Person, the new resident
     */
    public void addResident( Person r ) {
	residents.add( r );
	occupants.add( r );
	// no need to check to see if the person already lives there?
    }

    /** Primarily for debugging
     * @return textual name and residents of the home
     */
    public String toString() {
	String res = name;
	// DEBUG for (Person p: residents) { res = res + " " + p.name; }
	return res;
    }
}
