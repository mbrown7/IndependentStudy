package edu.umw.cpsc.collegesim;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Enumeration;

import sim.engine.*;
import sim.util.*;
import ec.util.*;
import sim.field.network.*;

public class Person implements Steppable{

    public enum Race { WHITE, MINORITY };
    public enum Gender { MALE, FEMALE };
  public static final int PROBABILITY_WHITE = 80;
  public static final int PROBABILITY_FEMALE = 50;
  
  public static final double RACE_WEIGHT = 3;
  public static final double GEN_WEIGHT = 1;
  public static final double CONST_WEIGHT = 1;
  public static final double INDEP_WEIGHT = 1.5;
  public static final double DEP_WEIGHT = 2.5;
  
  public static final double FRIENDSHIP_COEFFICIENT = .7;
  public static final double FRIENDSHIP_INTERCEPT = .2;
  
  //The number of people to meet from groups
  public static final int NUM_TO_MEET_GROUP = 2;
  public static final int NUM_TO_MEET_POP = 1;

  private int ID;
  private int year;
  private MersenneTwisterFast generator = Sim.instance( ).random;
  private int numTimes = 1;
  private int decayThreshold = 4;
  
  private Race race;
  private Gender gender;
    
    private int willingnessToMakeFriends;
    private ArrayList<Group> groups;
  
    int NUM_CONSTANT_ATTRIBUTES = 10;
  //constant attributes, like place of birth, etc.
  private ArrayList<Boolean> attributesK1     //Constant attributes
    = new ArrayList<Boolean>(Collections.nCopies(NUM_CONSTANT_ATTRIBUTES, false));
  
    int NUM_INDEPENDENT_ATTRIBUTES = 2;
    int INDEPENDENT_ATTRIBUTE_POOL = 5;
  //independent attributes, which can change but do not affect each other
  private ArrayList<Double> attributesK2      //Independent attributes
    = new ArrayList<Double>(Collections.nCopies(INDEPENDENT_ATTRIBUTE_POOL, 0.0));
  //the following is the interval inside which two attributes are considered "the same"
  //so for attribute 14, if this has 0.5 and other has 0.3, they have this attribute in
  //common, but if other had 0.2, they would not have this attribute in common
  double INDEPENDENT_INTERVAL = 0.2;
  
    int NUM_DEPENDENT_ATTRIBUTES = 2;
    int DEPENDENT_ATTRIBUTE_POOL = 5;
  //dependent attributes, which can change but you only have 1 unit to split among them
  //in other words, if one increases, then another decreases
    private ArrayList<Double> attributesK3      //Dependent attributes
    = new ArrayList<Double>(Collections.nCopies(DEPENDENT_ATTRIBUTE_POOL, 0.0));
    //the following is the interval inside which two attributes are considered "the same"
    //so for attribute 14, if this has 0.5 and other has 0.2, they have this attribute in
    //common, but if other had 0.1, they would not have this attribute in common
    double DEPENDENT_INTERVAL = 0.3;

    //A list that will house the absolute sim time that this person first met,
    //or last tickled, each other person
    private Hashtable<Integer,Double> lastTickleTime
      = new Hashtable<Integer,Double>();

    
    public void leaveUniversity( ){
System.out.println("Student " + ID + " is leaving...");
    	//This removes all the friendships this person holds and makes them leave all groups
    	//to be called when the person graduates or drops out
    	for(int i=0; i<groups.size( ); i++){
    		Group group = groups.get(i);
    		group.removeStudent(this);
    	}
    	//This SHOULD work to remove this person from the network graph AS WELL AS all edges
    	//into it and out of it, aka, all friendships
    	Sim.instance( ).people.removeNode(this);
    }
    
    
    /**
     * I have now tickled the person whose ID (index) is passed. So set the
     * last tickle time for this person to the current time.
     */
    public void refreshLastTickleTime(int index){
        lastTickleTime.put(index,Sim.instance().schedule.getTime());
    }
    
    /**
     * Blah, I'm no longer friends with this person. So completely remove
     * them from my hashtable (and life.)
     */
    public void resetLastTickleTime(int index){
        lastTickleTime.remove(index);
    }
    
    private void decay( ){
        Enumeration<Integer> friendIDs = lastTickleTime.keys();
        while (friendIDs.hasMoreElements()) {
            int friendID = friendIDs.nextElement();

            Edge toRemoveIn = null;
            Edge toRemoveOut = null;
            double val = lastTickleTime.get(friendID);
            //if the people last met longer than the threshold ago
            if(Sim.instance().schedule.getTime() - val > decayThreshold){
              //Get a bag of all the edges into this person
              Bag bIn = Sim.instance( ).people.getEdgesIn(this);
              //for each of these edges
              for(int j=0; j<bIn.size( ); j++){
                //look for the person whose ID matches the ID of the person we want to decay
                Edge edgeIn = (Edge)bIn.get(j);
                Person otherPerson = (Person) edgeIn.getOtherNode(this);
                int otherID = otherPerson.getID( );
                if(otherID == friendID){
                  //when we find the person, make their edge the one we want to remove
                  toRemoveIn = edgeIn;
                  j = bIn.size( );
                }
              }
              //Do the same with the other edges
              Bag bOut = Sim.instance( ).people.getEdgesOut(this);
              Person otherPerson = null;
              //for each of these edges
              for(int j=0; j<bOut.size( ); j++){
                //look for the person whose ID matches the ID of the person we want to decay
                Edge edgeOut = (Edge)bOut.get(j);
                otherPerson = (Person) edgeOut.getOtherNode(this);
                int otherID = otherPerson.getID( );
                if(otherID == friendID){
                  //when we find the person, make their edge the one we want to remove
                  toRemoveOut = edgeOut;
                  otherPerson.resetLastTickleTime(ID);
                  j = bOut.size( );
                }
              }
              Sim.instance( ).people.removeEdge(toRemoveIn);
              Sim.instance( ).people.removeEdge(toRemoveOut);
              resetLastTickleTime(friendID);
            }
          }
    }
    
    private void assignAttribute(int numAttr, int poolSize, ArrayList<Double> attr){
      boolean okay;
      for(int i=0; i<numAttr; i++){
        //pick an attribute to change
        int index = generator.nextInt(poolSize);
        okay = false;
        //while we have not chosen an appropriate index
        while(!okay){
          //if the attribute is zero, it has not already been changed, so we use it
          if(attr.get(index) == 0.0){
            okay = true;
          //otherwise, we have to pick a new attribute
          }else{
            index = generator.nextInt(poolSize);
          }
        }
        //pick a degree to which the person will have this attribute
        //we generate a number between 0 and 1, including 1 but not including 0
        double degree = generator.nextDouble(false, true);
        //then we set the attribute at the chosen index to be the generated degree
        attr.set(index, degree);
      }
    }
    
    private boolean assignRaceGender(int probability){
      int gen = generator.nextInt(100);
      if(gen <= probability){
        return true;
      }else{
        return false;
      }
    }
    
  Person(int ID){
        this.ID = ID;
    groups = new ArrayList<Group>( );
    
    //Assigning constant attributes
    for(int i=0; i<NUM_CONSTANT_ATTRIBUTES; i++){
      boolean rand = generator.nextBoolean( );
      attributesK1.set(i, rand);
    }
    //Assigning independent attributes
    assignAttribute(NUM_INDEPENDENT_ATTRIBUTES, INDEPENDENT_ATTRIBUTE_POOL, attributesK2);
    //Assigning dependent attributes
    assignAttribute(NUM_DEPENDENT_ATTRIBUTES, DEPENDENT_ATTRIBUTE_POOL, attributesK3);
    
    //Assign a race   
    boolean white = assignRaceGender(PROBABILITY_WHITE);
    if(white){
      race = Race.WHITE;
    }else{
      race = Race.MINORITY;
    }
    //Assign a gender
    boolean female = assignRaceGender(PROBABILITY_FEMALE);
    if(female){
      gender = Gender.FEMALE;
    }else{
      gender = Gender.MALE;
    }
    willingnessToMakeFriends = generator.nextInt(10)+1;
  }
  
  //What to do when meeting a new person
  public void meet(Person personToMeet){
    double similar;
    boolean friends = false;
    int personToMeetID = personToMeet.getID( );
//System.out.println("Person " + ID + " is meeting person " + personToMeetID);            ADD BACK IN
    //Calculate their similarity rating, and then see if they should become friends
    similar = similarityTo(personToMeet);
//System.out.println("similar " + similar);
    friends = areFriends(similar);
//System.out.println("friends " + friends);                           ADD BACK IN
    //if they become friends, add their edge to the network
    //and reset when they met
    if(friends){
        Sim.instance( ).people.addEdge(this, personToMeet, 1);
        refreshLastTickleTime(personToMeetID);
        personToMeet.refreshLastTickleTime(ID);
    }
  }
  
  //A function which "tickles" the relationship between "this" and the person whose ID is tickleID
  public void tickle(Person person){
    //reset when the two last encountered each other
    int tickleID = person.getID( );
    //System.out.println("Person " + ID + " is tickling person " + tickleID);         ADD BACK IN
    refreshLastTickleTime(tickleID);
    person.refreshLastTickleTime(ID);
  }
  
  public void encounter(int number, Bag pool){
    if(pool.size( ) < number){
      number = pool.size( );
    }
    for(int i=0; i<number; i++){
      Person personToMeet;
      do{
        personToMeet = (Person) pool.get(generator.nextInt(pool.size( )));
      }while(personToMeet == this);
      if(friendsWith(personToMeet)){
        tickle(personToMeet);
      }else{
        meet(personToMeet);
      }
    }
  }
  
  public void step(SimState state){
    //Get a bag of all the people in the groups
    Bag groupBag = getPeopleInGroups( );
    encounter(NUM_TO_MEET_GROUP, groupBag);
    //Get a bag of all the people and then encounter some number of those people
    Bag peopleBag = Sim.instance( ).people.getAllNodes( );
    encounter(NUM_TO_MEET_POP, peopleBag);


//NOTE: Decay only matters if the people are friends- you can't decay a friendship that
//doesn't exist.
//So, the time they last met only matters if they are friends already or if they
//become friends this turn
//If they aren't already friends and if they don't become friends this turn, then -1 for last met is
//fine
//(unless we implement something where if two people meet enough times, they become friends by brute
//force)
    
    
    //Now we want to see if any of the friendships have decayed
    decay( );
    
//    System.out.println(ID + " last tickled:");
//    for(int i=0; i<lastTickleTime.size( ); i++){
//      System.out.println(i + " " + lastTickleTime.get(i));
//    }
    
    
    //If we've done the maximum number of iterations, then stop; otherwise, keep stepping
    if(numTimes >= Sim.MAX_ITER){
      /*
<<<<<<< HEAD
      String message = Integer.toString(ID) + " ";
          Bag b = Sim.instance( ).people.getEdgesIn(this);
          int numFriends = 0;
          for (int i=0; i<b.size(); i++) {
              numFriends++;
          }
          message = message + Integer.toString(numFriends) + " "
              + Integer.toString(groups.size( )) + " " + race + " " + gender + " "
              + willingnessToMakeFriends + "\n";
          //Edit this try?
          try {
        Sim.outWriter.write(message);
        System.out.println("here");
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("bad");
      }
=======
>>>>>>> 4554d6937f367188ee37f28038cf7633f88696b3
      System.out.println(this);
      */
    }else{
            if (Sim.instance().nextMonthInAcademicYear()) {
                // It's not the end of the academic year yet. Run again
                // next month.
                Sim.instance( ).schedule.scheduleOnceIn(1, this);
            } else {
                // It's summer break! Sleep for the summer.
                Sim.instance( ).schedule.scheduleOnceIn(
                    Sim.NUM_MONTHS_IN_SUMMER, this);
            }
    }
    numTimes++;
  }

public void printToFile(){
        String message = Integer.toString(ID) + " ";
          Bag b = Sim.instance( ).people.getEdgesIn(this);
          int numFriends = 0;
          for (int i=0; i<b.size(); i++) {
              numFriends++;
          }
          message = message + Integer.toString(numFriends) + " "
              + Integer.toString(groups.size( )) + " " + race + " " + gender + " "
              + willingnessToMakeFriends +  " " + year + "\n";
          //Edit this try?
          try {
        Sim.outWriter.write(message);
        System.out.println("here");
      } catch (IOException e) {
        e.printStackTrace();
        System.out.println("bad");
      }
      System.out.println(this);
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
      int otherID = other.getID( );
      if(lastTickleTime.get(otherID) == -1){
        return false;
      }else{
        return true;
      }
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
  
  public Race getRace( ){
	  return race;
  }
  
  public Gender getGender( ){
	  return gender;
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
    
    private int attrCounter(int num, ArrayList<Boolean> attr1, ArrayList<Boolean> attr2){
      int count = 0;
      for(int i=0; i<num; i++){
        //if they have the same boolean value for an attribute
        if(attr1.get(i) == attr2.get(i)){
          //increment constant count
          count++;
        }
      }
      return count;
    }
    
    private int attrCounter(int num, ArrayList<Double> attr1, ArrayList<Double> attr2, double interval){
      int count = 0;
      for(int i=0; i<num; i++){
        double difference = attr1.get(i) - attr2.get(i);
        difference = Math.abs(difference);
        //if the difference is within the accept interval
        if(difference <= interval){
          //increment constant count
          count++;
        }
      }
      return count;
    }
    
    public double similarityTo(Person other) {
      double similar = 0.0;
      
      //Kind 1: Constant
      int constantCount = attrCounter(NUM_CONSTANT_ATTRIBUTES, attributesK1, other.attributesK1);
      
      //Kind 2: Independent
      int indepCount = attrCounter(INDEPENDENT_ATTRIBUTE_POOL, attributesK2, other.attributesK2, INDEPENDENT_INTERVAL);
      
      //Kind 3: Dependent
      ArrayList<Double> normalK3This = normalize(attributesK3);
      ArrayList<Double> normalK3Other = normalize(other.attributesK3);
      int depCount = attrCounter(DEPENDENT_ATTRIBUTE_POOL, normalK3This, normalK3Other, DEPENDENT_INTERVAL);
      
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
      double maxRating = (NUM_CONSTANT_ATTRIBUTES * CONST_WEIGHT) + (INDEPENDENT_ATTRIBUTE_POOL * INDEP_WEIGHT)
        + (DEPENDENT_ATTRIBUTE_POOL * DEP_WEIGHT) + RACE_WEIGHT + GEN_WEIGHT;
      double similarities = similar / maxRating;
    return similarities;
    }
    
  public boolean areFriends(double similarities){
    double acceptProb = FRIENDSHIP_COEFFICIENT * similarities + FRIENDSHIP_INTERCEPT;
//    System.out.println("accept prob y=mx + b " + acceptProb);
    double friendProb = generator.nextDouble( );
//    System.out.println("friend prob " + friendProb);
    if(friendProb <= acceptProb){
      return true;
    }else{
      return false;
    }
  }
  
  private ArrayList<Double> normalize(ArrayList<Double> attr){
    ArrayList<Double> normal
      = new ArrayList<Double>(Collections.nCopies(DEPENDENT_ATTRIBUTE_POOL, 0.0));
    double sum = 0.0;
    for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
        sum = sum + attr.get(i);
      }
    for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
        double valThis = attr.get(i)/sum;
        normal.set(i,valThis);
      }
    return normal;
  }
  
  public ArrayList<Double> getDependentAttributes(){
    return normalize(attributesK3);
  }
  
  public ArrayList<Double> getIndependentAttributes(){
    return attributesK2;
  }

  public void setIndAttrValue(int index, double val){
    attributesK2.set(index, val);
  }

  public void setDepAttrValue(int index, double val){
    //this functions says I want the normalized value of attribute index to be val
    double sum = 0.0;
    //Take the sum of all of the other non-normalized values
    for(int i=0; i<DEPENDENT_ATTRIBUTE_POOL; i++){
      if(index != i){
        sum = sum + attributesK3.get(i);
      }
    }
    double newNonNormalVal = (val * sum)/(1-val);
    attributesK3.set(index, newNonNormalVal);
  }

//  public ArrayList<Person> getPeopleInGroups( ){
//    ArrayList<Person> groupmates = new ArrayList<Person>();
//    boolean addPerson;
//    for(int x = 0; x < groups.size(); x++){
//      for(int y = 0; y < groups.get(x).getSize(); y++){
//        addPerson = true;
//        for(int z = 0; z < groupmates.size(); z++){
//          if (groups.get(x).getPersonAtIndex(y).equals(groupmates.get(z))){
//            addPerson = false;
//          }
//        }
//        if(addPerson&&!(groups.get(x).getPersonAtIndex(y).equals(this))){
//          groupmates.add(groups.get(x).getPersonAtIndex(y));
//        }
//      }
//    }
//    return groupmates;
//  }
  
  public Bag getPeopleInGroups( ){
    Bag groupmates = new Bag( );
    boolean addPerson;
    boolean first = true;
    for(int x = 0; x < groups.size( ); x++){
      for(int y = 0; y < groups.get(x).getSize( ); y++){
        addPerson = true;
        Person personToAdd = groups.get(x).getPersonAtIndex(y);
        if(first){
          if(!personToAdd.equals(this)){
            groupmates.add(personToAdd);
          }
        }else{
          for(int z = 0; z < groupmates.size( ); z++){
            if(personToAdd.equals(groupmates.get(z))){
              addPerson = false;
            }
          }
          if(addPerson && !(groups.get(x).getPersonAtIndex(y).equals(this))){
            groupmates.add(groups.get(x).getPersonAtIndex(y));
          }
        }
      }
    }
    return groupmates;
  }

  public void leaveGroup(Group g){
    for(int x = 0; x<groups.size(); x++){
      if(groups.get(x).equals(g)){
        groups.remove(x);
      }
    }
  }

  public boolean equals(Person p){
    return(ID==p.getID());
  }

  public void setYear(int x){
    year = x;
  }

  public int getYear(){
    return year;
  }

  public void incrementYear(){
    year++;
  }


}



