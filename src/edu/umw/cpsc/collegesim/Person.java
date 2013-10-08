package edu.umw.cpsc.collegesim;
import java.util.ArrayList;
import java.util.Collections;

import sim.engine.*;
import sim.util.*;
import ec.util.*;
import sim.field.network.*;

public class Person implements Steppable{

    public enum Race { WHITE, MINORITY };
    public enum Gender { MALE, FEMALE };
//	public static final int TOTAL_NUM_ATTRIBUTES = 50;
	//below must be an int to make sense but must be a double for division
//	public static final double NUM_ATTRIBUTES_PER_PERSON = 10.0;
//	public static final int MIN_TO_FRIENDS = 3;
	public static final int PROBABILITY_WHITE = 80;
	
	
	public static final double RACE_WEIGHT = 3;
	public static final double GEN_WEIGHT = 1;
	public static final double CONST_WEIGHT = 1;
	public static final double INDEP_WEIGHT = 1.5;
	public static final double DEP_WEIGHT = 2.5;
	
	
	public static final double BASE_CHANCE_FOR_FRIENDSHIP = 20.0;
	public static final int PROBABILITY_FEMALE = 50;

	private int ID;
	//change the above back to private at some point
	private MersenneTwisterFast generator = Sim.instance( ).random;
	private int numTimes = 1;
	private static final int MAX_ITER = 3;
	
	private Race race;
	private Gender gender;
    
    //from maddie's code
    private int willingnessToMakeFriends;
    private ArrayList<Group> groups;
//	int numGroupsJoined=0;
	
    int NUM_CONSTANT_ATTRIBUTES = 10;
	//constant attributes, like place of birth, etc.
	private ArrayList<Boolean> attributesK1
		= new ArrayList<Boolean>(Collections.nCopies(NUM_CONSTANT_ATTRIBUTES, false));	//Added the <Boolean> type thing-- it wasn't compiling without it-- also added to other similar lines --ML
	
    int NUM_INDEPENDENT_ATTRIBUTES = 20;
    int INDEPENDENT_ATTRIBUTE_POOL = 100;
	//independent attributes, which can change but do not affect each other
	private ArrayList<Double> attributesK2
		= new ArrayList<Double>(Collections.nCopies(INDEPENDENT_ATTRIBUTE_POOL, 0.0));
	//the following is the interval inside which two attributes are considered "the same"
	//so for attribute 14, if this has 0.5 and other has 0.3, they have this attribute in
	//common, but if other had 0.2, they would not have this attribute in common
	double INDEPENDENT_INTERVAL = 0.2;
	
    int NUM_DEPENDENT_ATTRIBUTES = 5;
    int DEPENDENT_ATTRIBUTE_POOL = 20;
	//dependent attributes, which can change but you only have 1 unit to split among them
	//in other words, if one increases, then another decreases
    private ArrayList<Double> attributesK3
	= new ArrayList<Double>(Collections.nCopies(DEPENDENT_ATTRIBUTE_POOL, 0.0));
    //the following is the interval inside which two attributes are considered "the same"
  	//so for attribute 14, if this has 0.5 and other has 0.2, they have this attribute in
  	//common, but if other had 0.1, they would not have this attribute in common
  	double DEPENDENT_INTERVAL = 0.3;
    
    
    
	Person(int ID){
        this.ID = ID;
		boolean okay;
		groups = new ArrayList<Group>( );
		
		//Assigning constant attributes
		for(int i=0; i<NUM_CONSTANT_ATTRIBUTES; i++){
			boolean rand = generator.nextBoolean( );
			attributesK1.set(i, rand);
		}
		
		//Assigning independent attributes
		//we will be setting a specified number of attributes - the rest will remain 0
		for(int i=0; i<NUM_INDEPENDENT_ATTRIBUTES; i++){
			//pick an attribute to change
			int index = generator.nextInt(INDEPENDENT_ATTRIBUTE_POOL);
			okay = false;
			//while we have not chosen an appropriate index
			while(!okay){
				//if the attribute is zero, it has not already been changed, so we use it
				if(attributesK2.get(index) == 0.0){
					okay = true;
				//otherwise, we have to pick a new attribute
				}else{
					index = generator.nextInt(INDEPENDENT_ATTRIBUTE_POOL);
				}
			}
			//pick a degree to which the person will have this attribute
			//we generate a number between 0 and 1, including 1 but not including 0
			double degree = generator.nextDouble(false, true);
			//then we set the attribute at the chosen index to be the generated degree
			attributesK2.set(index, degree);
		}
		
		//Assigning dependent attributes
		//we will be setting a specified number of attributes - the rest will remain 0
		//This is going to be identical to setting the independent attributes - we will
		//normalize these later, when calculating the similarities between people
		for(int i=0; i<NUM_DEPENDENT_ATTRIBUTES; i++){
			//pick an attribute to change
			int index = generator.nextInt(DEPENDENT_ATTRIBUTE_POOL);
			okay = false;
			//while we have not chosen an appropriate index
			while(!okay){
				//if the attribute is zero, it has not already been changed, so we use it
				if(attributesK3.get(index) == 0.0){
					okay = true;
				//otherwise, we have to pick a new attribute
				}else{
					index = generator.nextInt(DEPENDENT_ATTRIBUTE_POOL);
				}
			}
			//pick a degree to which the person will have this attribute
			//we generate a number between 0 and 1, including 1 but not including 0
			double degree = generator.nextDouble(false, true);
			//then we set the attribute at the chosen index to be the generated degree
			attributesK3.set(index, degree);
		}
		
		//Assign a race
		int genRace = generator.nextInt(100);
		//I might have an OBOE here
		if(genRace <= PROBABILITY_WHITE){
			System.out.println(ID + " is white");
			race = Race.WHITE;
		}else{
			System.out.println(ID + " is a minority");
			race = Race.MINORITY;
		}
		//Assign a gender
		int genGender = generator.nextInt(100);
		if(genGender <= PROBABILITY_FEMALE){
			gender = Gender.FEMALE;
		}else{
			gender = Gender.MALE;
		}
		willingnessToMakeFriends = generator.nextInt(10)+1;
	}
	
	public void step(SimState state){
		double similar;
		Bag peopleBag = Sim.instance( ).people.getAllNodes( ); //all of the people that exist
		Person personToMeet;
		boolean friends = false;
		boolean decay = false;
		//Pick a person for this person to meet out of the bag
		//And do this until the person we choose is not this person
		do{
			personToMeet = (Person) peopleBag.get(generator.nextInt(Sim.NUM_PEOPLE));
		}while(personToMeet == this);
System.out.println("Person " + ID + " is meeting person " + personToMeet.ID);

		//If they have not met each other before, add them to the lastMet network
		if(!met(personToMeet)){
			Sim.instance( ).lastMet.addEdge(this, personToMeet, 0);
		}

		//If they are not already friends
		if(!friendsWith(personToMeet)){
			//Calculate their similarity rating, and then see if they should become friends
			similar = similarityTo(personToMeet);
			friends = areFriends(similar);
			//if they should become friends, then add them
			if(friends){// && !decay){
					Sim.instance( ).people.addEdge(this, personToMeet, 1);
			}
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
		
		//If we've done the maximum number of iterations, then stop; otherwise, keep stepping
		if(numTimes >= MAX_ITER){
			System.out.println(this);
		}else{
			Sim.instance( ).schedule.scheduleOnceIn(1, this);
		}
		numTimes++;
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
        String retval = "Person " + ID + " (friends with ";
        Bag b = Sim.instance().people.getEdgesIn(this);
        for (int i=0; i<b.size(); i++) {
            retval += ((Person)(((Edge)b.get(i)).getOtherNode(this))).ID;
            if (i == b.size()-1) {
                retval += ")";
            } else {
                retval += ",";
            }
        }
        return retval;
    }
    
    public int getID( ){
    	return ID;
    }
 
	int getWillingnessToMakeFriends( ){
		return willingnessToMakeFriends;
	}
	
	void joinGroup(Group group){
		groups.add(group);
	}
	
	boolean isStudentInGroup(Group group){
		for(int x = 0; x<groups.size( ); x++){
			if(groups.get(x).equals(group)){
				return true;
			}
		}
		return false;
	}

	void printStatement( ){
		System.out.println("Willingness: "+willingnessToMakeFriends+" Number of Groups: " + groups.size( ));
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

    /**
     * Return a number from 0 to 1 based on how similar the passed Person
     * is to this Person.
     */
    public double similarityTo(Person other) {
    	double similar = 0.0;
    	int constantCount = 0;
    	//Kind 1: Constant
    	for(int i=0; i<NUM_CONSTANT_ATTRIBUTES; i++){
    		//if they have the same boolean value for an attribute
    		if(attributesK1.get(i) == other.attributesK1.get(i)){
    			//increment constant count
    			constantCount++;
    		}
    	}
    	
    	int indepCount = 0;
    	for(int i=0; i<INDEPENDENT_ATTRIBUTE_POOL; i++){
    		double difference = attributesK2.get(i) - other.attributesK2.get(i);
    		difference = Math.abs(difference);
    		if(difference <= INDEPENDENT_INTERVAL){
    			indepCount++;
    		}
    	}
    	
    	int depCount = 0;
    	ArrayList<Double> normalK3This
    		= new ArrayList<Double>(Collections.nCopies(DEPENDENT_ATTRIBUTE_POOL, 0.0));
    	ArrayList<Double> normalK3Other
    		= new ArrayList<Double>(Collections.nCopies(DEPENDENT_ATTRIBUTE_POOL, 0.0));
    	//we need to normalize the data for this person and the other person    	
    	//First, we will sum
    	double sum = 0.0;
    	double otherSum = 0.0;
    	for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
    		sum = sum + attributesK3.get(i);
    		otherSum = otherSum + other.attributesK3.get(i);
    	}
    	//Then, we normalize each entry
    	for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
    		double valThis = attributesK3.get(i)/sum;
    		normalK3This.set(i,valThis);
    		double valOther = other.attributesK3.get(i)/otherSum;
    		normalK3Other.set(i, valOther);
    	}
    	//Now, we see if the differences between each attribute are within the interval
       	for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
    		double difference = normalK3This.get(i) - normalK3Other.get(i);
    		difference = Math.abs(difference);
    		if(difference <= DEPENDENT_INTERVAL){
    			depCount++;
    		}
    	}
       	//Do they have the same race?
       	int raceCount = 0;
       	if(race == other.race){
       		raceCount = 1;
       	}
       	//Do they have the same gender?
       	int genCount = 0;
       	if(gender == other.gender){
       		genCount = 1;
       	}
       	//Calculate their similarity rating, taking importance of each category (the weight) into account
    	similar = (constantCount * CONST_WEIGHT) + (indepCount * INDEP_WEIGHT)
    			+ (depCount * DEP_WEIGHT) + (raceCount * RACE_WEIGHT) + (genCount * GEN_WEIGHT);
		return similar;
    }
    
	public boolean areFriends(double similarities){
		double maxRating = (NUM_CONSTANT_ATTRIBUTES * CONST_WEIGHT) + (NUM_INDEPENDENT_ATTRIBUTES * INDEP_WEIGHT)
				+ (NUM_DEPENDENT_ATTRIBUTES * DEP_WEIGHT) + RACE_WEIGHT + GEN_WEIGHT;
		double acceptProb = (similarities / maxRating) * 100;
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
	
	public ArrayList<Double> getIndependentAttributes(){
		return attributesK2;
	}

	public ArrayList<Double> getDependentAttributes(){
		return attributesK3;
	}
	
}



