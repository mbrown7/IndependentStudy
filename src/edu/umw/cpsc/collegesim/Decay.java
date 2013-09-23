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
		//People is a bag of all people
		Bag people = Sim.instance( ).people.getAllNodes( );
		//We're going to print out the people for sanity check
		System.out.println("The contents of the bag: ");
		for(int m=0; m<people.size( ); m++){
			Person mP = (Person) people.get(m);
			System.out.println(mP.name);
		}
		//for each of the people
		for(int i=0; i<people.size( ); i++){
			//pick one
			Person person = (Person) people.get(i);
System.out.println("Looking at person " + person.name);
			//get a bag containing all of the edges between others and this person
			Bag b = Sim.instance( ).lastMet.getEdgesIn(person);
			//for each of the edges
			for(int j=0; j<b.size( ); j++){
				//pick one
				Edge edge = (Edge)b.get(j);
				//determine who the other person on that edge is
				Person otherPerson = (Person) edge.getOtherNode(person);
System.out.println("Looking at when they last met person " + otherPerson.name);
				//obtain the number of steps since the two last met
				int steps = (int) edge.getInfo( );
				//increment this number
				steps++;
System.out.println("This was " + steps + " steps ago.");
				//if the steps is past the decay point
				if(steps > NUM_STEPS_TO_DECAY){
System.out.println("Steps causes decay");
					//remove the edge saying when they last met
					Sim.instance( ).lastMet.removeEdge(edge);
					//get a bag of all the friendships of this person
					Bag friendships = Sim.instance( ).people.getEdgesIn(person);
					//for each friendship
					for(int m=0; m<friendships.size( ); m++){
						//pick one
						Edge edgeTest = (Edge)friendships.get(m);
						//obtain the person on the other end
						Person test = (Person) edgeTest.getOtherNode(person);
						//when this person is the other friend in question
						if(test.equals(otherPerson)){
System.out.println("We're removing the friendship between " + person.name + " and " + test.name);
							//remove this edge
							Sim.instance( ).people.removeEdge(edgeTest);
						}
					}
				//if we're not past the decay point
				}else{
System.out.println("Steps does not cause decay.");
					//just make the edge hold the new number of steps
					Sim.instance( ).lastMet.updateEdge(edge, person, otherPerson, steps);
				}
			}
System.out.println("The friends for " + person.name + " are:");
Bag testBag = Sim.instance( ).people.getEdgesIn(person);
for(int k=0; k<testBag.size( ); k++){
	Edge friendEdge = (Edge)testBag.get(k);
	Person friend = (Person)friendEdge.getOtherNode(person);
	System.out.println(friend.name);
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

