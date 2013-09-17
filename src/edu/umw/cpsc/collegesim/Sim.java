package edu.umw.cpsc.collegesim;
import sim.engine.*;
import sim.util.*;
import sim.field.network.*;


/**
 * Homophily sub-simulation. (Look at Main.java for propinquity
 * sub-simulation.)
 * @author MB
 */
public class Sim extends SimState{

//	public Continuous2D room = new Continuous2D(1.0,100,100);

    // Undirected graph.
	public static Network people = new Network(false);

	public static final int NUM_PEOPLE = 10;

    private static long SEED = 0;
    //Stephen had the above as final, did not compile for me
    private static Sim theInstance;

    public static synchronized Sim instance() {
        if (theInstance == null) {
            theInstance = new Sim(SEED);
        }
        return theInstance;
    }
    
	public Sim(long seed){
		super(seed);
	}
	
	public void start( ){
		super.start( );
		//create 10 people, put them in the network, and add them to the
		//schedule
		for(int i=0; i<NUM_PEOPLE; i++){
			Person person = new Person(Integer.toString(i));
			people.addNode(person);
			schedule.scheduleOnce(person);
		}
	}
	
	public static void main(String[] args) {
        doLoop(new MakesSimState() {
            public SimState newInstance(long seed, String[] args) {
                Sim.SEED = seed;
                return instance();
            }
            public Class simulationClass() {
                return Sim.class;
            }
        }, args);
	}

}
