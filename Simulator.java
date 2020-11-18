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


/** Framework for discrete event simulation.
 */
abstract class Simulator {
    private Simulator(){} // prevent anyone from instantiating this class

    // BUG -- this may not be the right place to specify time units
    public static final double day = 1.0;
    public static final double hour = day / 24.0;
    public static final double minute = day / (24.0 * 60.0);
    public static final double second = day / (24.0 * 60.0 * 60.0);
    public static final double week = day * 7;

    /** Interface to allow lambda parameters to schedule()
     *  as such, no external code ever uses Action
     */
    public interface Action {
	// actions contain the specific code of each event
	void trigger( double time );
    }

    private static class Event {
	public double time; // the time of this event
	public Action act; // what to do at that time
    }

    private static PriorityQueue<Event> eventSet
	= new PriorityQueue<Event> (
	    (Event e1, Event e2)-> Double.compare( e1.time, e2.time )
	);

    /** Call schedule to make act happen at time.
     *  Users typically pass the action as a lambda expression:
     *  <PRE>
     *  Simulator.schedule( t, ( double time )-> method( ... time ... ) )
     *  </PRE>
     */
    static void schedule( double time, Action act ) {
	Event e = new Event();
	e.time = time;
	e.act = act;
	eventSet.add( e );
    }

    /** run the simulation.
     *  Call run() after scheduling some initial events to run the simulation.
     */
    static void run() {
	while (!eventSet.isEmpty()) {
	    Event e = eventSet.remove();
	    e.act.trigger( e.time );
	}
    }
}
