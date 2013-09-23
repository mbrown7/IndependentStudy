package edu.umw.cpsc.collegesim;
import java.util.ArrayList;

import sim.engine.*;
import sim.util.*;
import ec.util.*;
import sim.field.network.*;

public class Person implements Steppable{

	//818
    public enum Race { WHITE, MINORITY };
	public static final int TOTAL_NUM_ATTRIBUTES = 50;
	//below must be an int to make sense but must be a double for division
	public static final double NUM_ATTRIBUTES_PER_PERSON = 10.0;
//	public static final int MIN_TO_FRIENDS = 3;
	public static final int PROBABILITY_WHITE = 80;
	//RACE_WEIGHT = x means where every similar attribute counts as 1 thing in common, if they have the
	//same race, it counts as being x things in common
	public static final int RACE_WEIGHT = 2;
	public static final double BASE_CHANCE_FOR_FRIENDSHIP = 20.0;

	private ArrayList<Integer> attributes;
	public String name;
	//change the above back to private at some point
	private MersenneTwisterFast generator = Sim.instance().random;
	private int numTimes = 1;
    
    //from maddie's code
    static MersenneTwisterFast rand1;
    private int willingnessToMakeFriends;
	private ArrayList<Group> groups;
	private int id;
	
	private static final int MAX_ITER = 3;
    private Race race;
    
	
	Person(String name){
        this.name = name;
		boolean okay;
		groups = new ArrayList<Group>();
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
		System.out.println("Person " + name + " has the following attributes:");
		for(int i=0; i<NUM_ATTRIBUTES_PER_PERSON; i++){
			System.out.print(attributes.get(i)+", ");
		}
		
		//let's pretend we have it set to 80% chance to be white and
		//20% chance to be a minority
		int test = generator.nextInt(100);
		//I might have an OBOE here
		if(test <= PROBABILITY_WHITE){
			System.out.println(name + " is white");
			race = Race.WHITE;
		}else{
			System.out.println(name + " is a minority");
			race = Race.MINORITY;
		}
		willingnessToMakeFriends = generator.nextInt(10)+1;
	}
	
	public void step(SimState state){
		int similar = 0;
		Bag peopleBag = Sim.instance( ).people.getAllNodes( ); //all of the people that exist
		Person personToMeet;
		boolean friends = false;
		boolean decay = false;
		do{
			personToMeet = (Person) peopleBag.get(generator.nextInt(Sim.NUM_PEOPLE));
		}while(personToMeet == this);
System.out.println("Person " + name + " is meeting person " + personToMeet.name);

		if(!met(personToMeet)){
			Sim.instance( ).lastMet.addEdge(this, personToMeet, 0);
//			Sim.instance( ).lastMet.addEdge(personToMeet, this, 0);	
		}

		if(!friendsWith(personToMeet)){
			for(int i=0; i<NUM_ATTRIBUTES_PER_PERSON; i++){
				for(int j=0; j<NUM_ATTRIBUTES_PER_PERSON; j++){
					if(personToMeet.attributes.get(j) == attributes.get(i)){
						similar++;
					}
				}
			}
			if(race == personToMeet.race){
				similar = similar + RACE_WEIGHT;
			}
			friends = areFriends(similar);
		}
		
//		Bag bagMet = Sim.instance( ).lastMet.getEdgesIn(this);
//		
//		System.out.println("People in this bag and steps since met: ");
//		for(int j=0; j<bagMet.size( ); j++){
//			Edge edger = (Edge)bagMet.get(j);
//			Person womp = (Person) ((Edge)bagMet.get(j)).getOtherNode(this);
//			int womp2 = (int) edger.getInfo( );
//			System.out.println(womp.name + " " + womp2);
//		}
//		
//		Edge otherEdge = null;
//		
//		for(int i=0; i<bagMet.size( ); i++){
//			Edge edgeUpdate = (Edge)bagMet.get(i);
//			Person otherPerson = (Person) ((Edge)bagMet.get(i)).getOtherNode(this);
//System.out.println("name of other person " + otherPerson.name);
//			Bag secondBag = Sim.instance( ).lastMet.getEdgesIn(otherPerson);
//			for(int j=0; j<secondBag.size( ); j++){
//				Person testPerson = (Person) ((Edge)secondBag.get(j)).getOtherNode(otherPerson);
//				if(testPerson.equals(this)){
//					otherEdge = (Edge)secondBag.get(j);
//				}
//			}
//			int steps = (int) edgeUpdate.getInfo( );
//			steps++;
//System.out.println("Person " + name + " last met person " + otherPerson.name + " a number of "
//		+ steps + " step(s) ago.");
//			if(steps >= NUM_STEPS_TO_DECAY){
//				System.out.println("decay is true");
//				decay = true;
//				Sim.instance( ).lastMet.removeEdge(edgeUpdate);
////				Sim.instance( ).lastMet.removeEdge(otherEdge);
//			}else{
//				Sim.instance( ).lastMet.updateEdge(edgeUpdate, this, otherPerson, steps);
////				Sim.instance( ).lastMet.updateEdge(otherEdge, otherPerson, this, steps);
//			}
//		}
		
		if(friends){// && !decay){
				Sim.instance( ).people.addEdge(this, personToMeet, 1);
		}
		
		if(numTimes >= MAX_ITER){
			System.out.println(this);
		}else{
			Sim.instance( ).schedule.scheduleOnceIn(1, this);
		}
		numTimes++;
	}
	
	public boolean areFriends(int similarities){
		double acceptProb = (similarities / (NUM_ATTRIBUTES_PER_PERSON + RACE_WEIGHT)) * 100;
		acceptProb = acceptProb + BASE_CHANCE_FOR_FRIENDSHIP;
		if(acceptProb > 100.0){
			acceptProb = 100.0;
		}
		int friendProb = generator.nextInt(100);
		if(friendProb <= acceptProb){
System.out.println("They became friends.");
			return true;
		}else{
			return false;
		}
	}

    public boolean friendsWith(Person other) {
    	Bag b = Sim.instance( ).people.getEdgesIn(this);
        for (int i=0; i<b.size(); i++) {
            Person otherSideOfThisEdge = 
                (Person) ((Edge)b.get(i)).getOtherNode(this);
            if (other == otherSideOfThisEdge) {
                return true;
            }
        }
        return false;
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
    
    //moved my methods down here
	int getWillingnessToMakeFriends(){
		return willingnessToMakeFriends;
	}

	void joinGroup(Group group){
		groups.add(group);
	}
	
	boolean isStudentInGroup(Group group){
		for(int x = 0; x<groups.size();x++){
			if (groups.get(x).equals(group)){
				return true;
			}
		}
		return false;
	}
	
	public int getID(){
		return id;
	}
	
	public void setID(int i){
		id=i;
	}
	
	void printStatement(){
		System.out.println("Willingness: "+willingnessToMakeFriends+" Number of Groups: " + groups.size());
	}
	


    /**
     * Based on the possible presence of popular attributes possessed by
     * the Group's members, possibly absorb one or more of these attributes
     * into this Person, if he/she does not already have them.
     */
    public void possiblyAbsorbAttributesFrom(Group g) {
        // for now, if you absorb an attribute, just add it, don't try to
        // even it out by removing one.
    }

}

