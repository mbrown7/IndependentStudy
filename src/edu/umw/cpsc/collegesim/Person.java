package edu.umw.cpsc.collegesim;
import java.util.ArrayList;
import java.util.Random;

import sim.engine.*;
import sim.util.*;
import ec.util.*;
import sim.field.network.*;

public class Person implements Steppable{

	private ArrayList<Integer> attributes;
	public int numAttr = 10;
	String name;
	public int minToFriends = 3;
	MersenneTwisterFast generator = Sim.instance().random;
	Sim sim;
	int numTimes = 1;
	int maxIter = 20;
	int race;
	//if race is 1, race is white; if race is 0, race is minority
	
	Person( ){
		boolean okay;
		attributes = new ArrayList<Integer>( );
		//we're going to give each person 10 attributes
		for(int i=0; i<numAttr; i++){
			okay = true;
			int rand = generator.nextInt(50);
			//Check to be sure the attribute is not repeated
			for(int j=0; j<i; j++){
				if(attributes.get(j) == rand){
					okay = false;
				}
			}
			//if it's okay, then add it - if it was repeated, decrement i
			//because we never added an attribute this time through
			if(okay){
				attributes.add(rand);
			}else{
				i--;
			}
		}
		//I couldn't work out using his random generator so I'm just using
		//the worse regular one for now
		
		//let's pretend we have it set to 80% chance to be white and
		//20% chance to be a minority
		int test = generator.nextInt(100);
		//I might have an OBOE here
		if(test >= 80){
			race = 1;
		}else{
			race = 0;
		}
	}
	
	public void step(SimState state){
		sim = (Sim) state;
		int similar = 0;
		//Creates a bag of all the people
		Bag peopleBag = sim.people.getAllNodes( );
		//Randomly determines a person that the individual stepping will meet
		//ensures that the person they are meeting is not themselves
		Person personToMeet;
		do{
			personToMeet = (Person) peopleBag.get(generator.nextInt(sim.numPeople));
		}while(personToMeet == this);
		for(int i=0; i<numAttr; i++){
			for(int j=0; j<numAttr; j++){
				if(personToMeet.attributes.get(j) == attributes.get(i)){
					similar++;
				}
			}
		}
		//if they have at least 5 shared traits, then they become friends
		if(similar >= minToFriends){
			//I would like to eventually account for if people are already
			//friends, so you don't have to add edges that already exist
			
			//Represent the friendship in the network
			
			//Here's the problem - I can't manage to make it so that you
			//don't become friends again with someone you already know
			//So people are making 30 friends out of 9 possibilities
			//I have tried checking that the edge doesn't already exist in
			//the graph - this has been the closest to a solution
			//but the edge object I am able to write is directed
			//whereas the edge objects that are returned in the bag go both
			//ways, so java doesn't count them as actually equal
			//even if the two nodes are the same
			
			sim.people.addEdge(this, personToMeet, 1);
			sim.people.addEdge(personToMeet, this, 1);
			System.out.println("Person " + name + " making friends with "
					+ personToMeet.name);
		}
		if(numTimes >= maxIter){
			Bag bag = sim.people.getEdgesIn(this);
			int x = bag.size( );
			System.out.println("Person " + name + " made " + x + " friends in "
					+ maxIter + " trials.");
		}else{
			sim.schedule.scheduleOnce(this);
		}
		numTimes++;
	}
}

