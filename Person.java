import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/* Epidemic Simulator, People occupy places
 * @author Blake Preston, Douglas Jones
 * @version 11/9/20
 * @see Place
 * @see Employee
 */

// Model class

public class Person {

    protected enum States {
	uninfected, latent, infectious, bedridden, recovered, dead
	// the order of the above is significant: >= uninfected is infected
    }

    // static attributes describing progression of infection
    // BUG --  These should come from model description file, not be hard coded
    double latentMedT = 2 * Simulator.day;
    double latentScatT = 1 * Simulator.day;
    double bedriddenProb = 0.7;
    double infectRecMedT = 1 * Simulator.week;
    double infectRecScatT = 6 * Simulator.day;
    double infectBedMedT = 3 * Simulator.day;
    double infectBedScatT = 5 * Simulator.day;
    double deathProb = 0.2;
    double bedRecMedT = 2 * Simulator.week;
    double bedRecScatT = 1 * Simulator.week;
    double bedDeadMedT = 1.5 * Simulator.week;
    double bedDeadScatT = 1 * Simulator.week;

    // static counts of infection progress
    private static int numUninfected = 0;
    private static int numLatent = 0;
    private static int numInfectious = 0;
    private static int numBedridden = 0;
    private static int numRecovered = 0;
    private static int numDead = 0;

    // fixed attributes of each instance
    private final HomePlace home;  // all people have homes
    public final String name;      // all people have names

    // instance variables
    protected Place place;         // when not in transit, where the person is
    public States infectionState;  // all people have infection states

    // the collection of all instances
    private static final LinkedList <Person> allPeople =
	new LinkedList <Person> ();

    // need a source of random numbers
    private static final MyRandom rand = MyRandom.stream();

    /** The only constructor
     *  @param h the home of the newly constructed person
     */
    public Person( HomePlace h ) {
	name = super.toString();
	home = h;
	place = h; // all people start out at home
	infectionState = States.uninfected;
	numUninfected = numUninfected + 1;
	h.addResident( this );

	allPeople.add( this ); // this is the only place items are added!
    }

    /** Predicate to test person for infectiousness
     *  @returns true if the person can transmit infection
     */
    public boolean isInfectious() {
	return (infectionState == States.infectious)
	    || (infectionState == States.bedridden);
    }

    /** Primarily for debugging
     *  @return textual name and home of this person
     */
    public String toString() {
	return name ;// DEBUG  + " " + home.name + " " + infectionState;
    }

    /** Shuffle the population
     *  This allows correlations between attributes of people to be broken
     */
    public static void shuffle() {
	Collections.shuffle( allPeople, rand );
    }

    /** Allow outsiders to iterate over all people
     *  @return an iterator over people
     */
    public static Iterator <Person> iterator() {
	return allPeople.iterator();
    }

    // simulation methods relating to infection process

    /** Infect a person
     *  called when circumstances call for a person to become infected
     */
    public void infect( double time ) {
	if (infectionState == States.uninfected) {
	    // infecting an already infected person has no effect
	    double delay = rand.nextLogNormal( latentMedT, latentScatT );

	    numUninfected = numUninfected - 1;
	    infectionState = States.latent;
	    numLatent = numLatent + 1;

	    Simulator.schedule( delay + time, (double t)-> beInfectious( t ) );
	}
    }

    /** An infected but latent person becomes infectous
     *  scheduled by infect() to make a latent person infectious
     */
    public void beInfectious( double time ) {
	numLatent = numLatent - 1;
	infectionState = States.infectious;
	numInfectious = numInfectious + 1;

	if (place != null) place.oneMoreInfectious( time );

	if (rand.nextFloat() > bedriddenProb) { // person stays asymptomatic
	    double delay = rand.nextLogNormal( infectRecMedT, infectRecScatT );
	    Simulator.schedule( delay + time, (double t)-> beRecovered( t ) );
	} else { // person becomes bedridden
	    double delay = rand.nextLogNormal( infectBedMedT, infectBedScatT );
	    Simulator.schedule( delay + time, (double t)-> beBedridden( t ) );
	}
    }

    /** An infectious person becomes bedridden
     *  scheduled by beInfectious() to make an infectious person bedridden
     */
    public void beBedridden( double time ) {
	numInfectious = numInfectious - 1;
	infectionState = States.bedridden;
	numBedridden = numBedridden + 1;

	if (rand.nextFloat() > deathProb) { // person recovers
	    double delay = rand.nextLogNormal( bedRecMedT, bedRecScatT );
	    Simulator.schedule( delay + time, (double t)-> beRecovered( t ) );
	} else { // person dies
	    double delay = rand.nextLogNormal( bedDeadMedT, bedDeadScatT );
	    Simulator.schedule( delay + time, (double t)-> beDead( t ) );
	}

	// if in a place (not in transit) that is not home, go home now!
	if ((place != null) && (place != home)) goHome( time );
    }

    /** A infectious or bedridden person recovers
     *  scheduled by beInfectious() or beBedridden to make a person recover
     */
    public void beRecovered( double time ) {
	if (infectionState == States.infectious) {
	    numInfectious = numInfectious - 1;
	} else {
	    numBedridden = numBedridden - 1;
	}
	infectionState = States.recovered;
	numRecovered = numRecovered + 1;

	if (place != null) place.oneLessInfectious( time );
    }

    /** A bedridden person dies
     *  scheduled by beInfectious() to make a bedridden person die
     */
    public void beDead( double time ) {
	numBedridden = numBedridden - 1;
	infectionState = States.dead; // needed to prevent resurrection
	numDead = numDead + 1;

	// if the person died in a place, make them leave it!
	if (place != null) place.depart( this, time );

	// BUG: leaves them in the directory of residents and perhaps employees
    }

    // simulation methods relating to daily reporting

    /** Make the daily midnight report
     *  @param time the current time
     */
    public static void report( double time ) {
	System.out.println(
	    "at " + time
	  + ", un = " + numUninfected
	  + ", lat = " + numLatent
	  + ", inf = " + numInfectious
	  + ", bed = " + numBedridden
	  + ", rec = " + numRecovered
	  + ", dead = " + numDead
	);

	// make this happen cyclically
	Simulator.schedule( time + Simulator.day, (double t)-> report( t ) );
    }

    // simulation methods relating to personal movement

    /** Make a person arrive at a new place
     *  @param p new place
     *  @param time the current time
     *  scheduled
     */
    public void arriveAt( double time, Place p ) {
	if ((infectionState == States.bedridden) && (p != home)) {
	    // go straight home if you arrive at work while sick
	    goHome( time );

	} else if (infectionState == States.dead) { // died on the way to work
	    // allow this person to be forgotten

	} else { // only really arrive if not sick
	    p.arrive( this, time );
	    this.place = p;
	}
    }

    /** Move a person to a new place
     *  @param p where the person travels
     *  @param time at which the move will be completed takes place
     *  BUG -- if time was the time the trip started:
     *  travelTo could do the call to this.place.depart()
     *  and it could compute the travel time
     */
    public void travelTo( Place p, double time ) {
	this.place = null;
	Simulator.schedule( time, (double t) -> arriveAt( t, p ) );
    }

    /** Simulate the trip home from wherever
     * @param time of departure
     */
    public void goHome( double time ) {
	double travelTime = rand.nextLogNormal(
	    20 * Simulator.minute, // mean travel time
	    3 * Simulator.minute   // scatter in travel time
	);

	// the possibility of arriving at work after falling ill requires this
	if (this.place != null) this.place.depart( this, time );

	this.travelTo( this.home, time + travelTime );
    }
}
