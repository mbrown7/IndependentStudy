package edu.umw.cpsc.collegesim;
import java.util.ArrayList;
import java.util.Random;

import sim.engine.*;
import sim.util.*;
import ec.util.*;
import sim.field.network.*;

public class Person implements Steppable{

    public enum Race { WHITE, MINORITY };
	public static final int TOTAL_NUM_ATTRIBUTES = 50;
	public static final int NUM_ATTRIBUTES_PER_PERSON = 10;
	public static final int MIN_TO_FRIENDS = 3;

	private ArrayList<Integer> attributes;
	private String name;
	private MersenneTwisterFast generator = Sim.instance().random;
	private int numTimes = 1;
	private static final int MAX_ITER = 20;
	
    private Race race;
	
	Person(String name){
        this.name = name;
		boolean okay;
		attributes = new ArrayList<Integer>( );
		//we're going to give each person 10 attributes
		for(int i=0; i<NUM_ATTRIBUTES_PER_PERSON; i++){
			okay = true;
			int rand = generator.nextInt(TOTAL_NUM_ATTRIBUTES);
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
		
		//let's pretend we have it set to 80% chance to be white and
		//20% chance to be a minority
		int test = generator.nextInt(100);
		//I might have an OBOE here
		if(test >= 80){
			race = Race.WHITE;
		}else{
			race = Race.MINORITY;
		}
	}
	
	public void step(SimState state){
		int similar = 0;
		//Creates a bag of all the people
		Bag peopleBag = Sim.instance().people.getAllNodes( );
		//Randomly determines a person that the individual stepping will meet
		//ensures that the person they are meeting is not themselves
		Person personToMeet;
		do{
			personToMeet = 
                (Person) peopleBag.get(generator.nextInt(Sim.NUM_PEOPLE));
		}while(personToMeet == this);
		for(int i=0; i<NUM_ATTRIBUTES_PER_PERSON; i++){
			for(int j=0; j<NUM_ATTRIBUTES_PER_PERSON; j++){
				if(personToMeet.attributes.get(j) == attributes.get(i)){
					similar++;
				}
			}
		}
		//if they have at least n shared traits, then they become friends
		if(similar >= MIN_TO_FRIENDS){
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
			
            if (!friendsWith(personToMeet)) {
                Sim.instance().people.addEdge(this, personToMeet, 1);
            }
		}
		if(numTimes >= MAX_ITER){
            System.out.println(this);
		}else{
			Sim.instance().schedule.scheduleOnceIn(1,this);
		}
		numTimes++;
	}

    public boolean friendsWith(Person other) {
        Bag b = Sim.instance().people.getEdgesIn(this);

        for (int i=0; i<b.size(); i++) {
            Person otherSideOfThisEdge = 
                (Person) ((Edge)b.get(i)).getOtherNode(this);
            if (other == otherSideOfThisEdge) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        String retval = "Person " + name + " (friends with ";
        Bag b = Sim.instance().people.getEdgesIn(this);
        for (int i=0; i<b.size(); i++) {
            retval += ((Person)(((Edge)b.get(i)).getOtherNode(this))).name;
            if (i == b.size()-1) {
                retval += ")";
            } else {
                retval += ",";
            }
        }
        return retval;
    }
}

