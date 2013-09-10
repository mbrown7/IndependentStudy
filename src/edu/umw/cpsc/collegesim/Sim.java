package edu.umw.cpsc.collegesim;
import sim.engine.*;
import sim.util.*;
import sim.field.network.*;

//worked 3:40
//

public class Sim extends SimState{

//	public Continuous2D room = new Continuous2D(1.0,100,100);
	public Network people = new Network(true);
	public int numPeople = 10;
    private static Sim theInstance;

    public static synchronized Sim instance() {
        if (theInstance == null) {
            theInstance = new Sim(0);
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
		for(int i=0; i<numPeople; i++){
			Person person = new Person( );
			person.name = Integer.toString(i);
			people.addNode(person);
			schedule.scheduleOnce(person);
		}
	}
	
	public static void main(String[] args) {
		doLoop(Sim.class, args);
		System.exit(0);
	}

}
