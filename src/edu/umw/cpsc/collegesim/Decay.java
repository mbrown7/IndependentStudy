package edu.umw.cpsc.collegesim;
import java.util.ArrayList;

import sim.engine.*;
import sim.util.*;
import ec.util.*;
import sim.field.network.*;

public class Decay implements Steppable{
	
	private int numTimes = 1;
	private static final int MAX_ITER = 3;
	public static final int NUM_STEPS_TO_DECAY = 3;
	
	Decay( ){
        
	}
	
	public void step(SimState state){
		Bag people = Sim.instance( ).people.getAllNodes( );
		System.out.println("The contents of the bag: ");
		for(int m=0; m<people.size( ); m++){
			Person mP = (Person) people.get(m);
			System.out.println(mP.name);
		}
		
		for(int i=0; i<people.size( ); i++){
			Person person = (Person) people.get(i);
System.out.println("Looking at person " + person.name);
			Bag b = Sim.instance( ).lastMet.getEdgesIn(person);
			for(int j=0; j<b.size( ); j++){
				Edge edge = (Edge)b.get(j);
				Person otherPerson = (Person) edge.getOtherNode(j);
				//Is j the correct thing to have here?
				int steps = (int) edge.getInfo( );
				steps++;
				if(steps > NUM_STEPS_TO_DECAY){
					Sim.instance( ).lastMet.removeEdge(edge);
					//I need to find out how to remove this edge from the friendships too
					//What am I trying to do?
					//remove the friendship between the person object in edge and the person object person
					Edge peopleEdge = (Edge)people.get(i);
					Sim.instance( ).people.removeEdge(peopleEdge);
				}else{
					Sim.instance( ).lastMet.removeEdge(edge);
				}
			}
		}
		if(numTimes >= MAX_ITER){
			System.out.println(this);
		}else{
			Sim.instance( ).schedule.scheduleOnceIn(1, this);
		}
		numTimes++;
	}
	
    
    public boolean met(Person other){
    	Bag b = Sim.instance( ).lastMet.getEdgesIn(this);
    	for(int i=0; i<b.size( ); i++){
    		Person otherSideOfThisEdge = (Person) ((Edge)b.get(i)).getOtherNode(this);
    		if(other == otherSideOfThisEdge){
    			return true;
    		}
    	}
    	return false;
    }
}

