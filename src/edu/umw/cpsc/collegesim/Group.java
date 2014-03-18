package edu.umw.cpsc.collegesim;

import java.io.IOException;
import java.util.ArrayList;
import ec.util.*;
import sim.engine.*;
import sim.util.Bag;

/*TODO: clean up size stuff, maybe get rid of the variable all together and just keep the setter and use students.size() in the class. 
* Should I change students to people? That way it would be consistant throughout the program
* maybe change factors to 0-1 rather than 0-10?
* What are we doing with tightness? Does it help determine recruitement? Or should it deal with leaving a group?
* maybe have a max/min num students per group factor?
*/


/**
 * A group of students in the CollegeSim model, perhaps representing a
 * campus club, orientation group, dorm room floor, or group of friends.
 */
public class Group implements Steppable{

    /**
     * The lower bound on the size of a group at any point during its
     * lifespan. */
    public static final int MINIMUM_GROUP_SIZE = 3;

    /**
     * The lower bound on the size of a newly created group. (See {@link
     * #MINIMUM_GROUP_SIZE}.) */
    public static final int MINIMUM_START_GROUP_SIZE = 3;
  
    /**
     * The upper bound on the size of a newly created group. */
    public static final int MAXIMUM_START_GROUP_SIZE = 8; 
  
    /**
     * A threshold that determines the likelihood of whether a student will 
     * join a group. Higher numbers means less acceptance into groups. The
     * number itself is difficult to interpret, since a complex conflagration
     * of factors will be put together to compare to it. */
    public static final double RECRUITMENT_REQUIRED = .6;     //lower this to accept more students in group per step
  
    /**
     * Each time step, the probability that each student will leave each of
     * their groups, provided that leaving said group would not push the
     * group size below the minimum. */
    public static final double LIKELIHOOD_OF_RANDOMLY_LEAVING_GROUP = .1;   //increase this to remove more students in group per step
  
    /** Each time step, the probability that a student will change one of
     * their attribute values <i>provided</i> that said attribute is "different
     * enough" from their influencing peers to warrant change. */
    public static final double LIKELIHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE = .1;
  
    /**
     * Each time step, the number of students who will be "invited" to a
     * group. (<i>i.e.</i>, have {@link #recruitStudent(Person)} called on
     * them.)
     */
    public static final int NUM_PEOPLE_TO_RECRUIT = 10;

    private int id;

    // A number in the range 0 to 1 indicating how aggressive the group is
    // in attracting members.
    private double recruitmentFactor;
    private static MersenneTwisterFast rand = Sim.instance( ).random;
    int numTimes = 0;
  
    private ArrayList<Person> students;
    
    /**
     * Constructs a new Group object with the id passed, which is
     * <i>not</i> checked for uniqueness, and pre-populate it with members
     * from the list passed. */
    public Group(int id, ArrayList<Person> peopleList){
      this.id = id;
      students = new ArrayList<Person>();
      selectStartingStudents(peopleList);
      recruitmentFactor = rand.nextDouble();
    }

    private void selectStartingStudents(ArrayList<Person> people){
        int initialGroupSize = rand.nextInt(
            MAXIMUM_START_GROUP_SIZE-MINIMUM_START_GROUP_SIZE) + 
            MINIMUM_START_GROUP_SIZE + 1;
        Person randStudent;
        if(initialGroupSize>MINIMUM_GROUP_SIZE){
          initialGroupSize=MINIMUM_GROUP_SIZE;    //keeps groups at least 
          // the min
        }
        if(initialGroupSize>Sim.getNumPeople()){
          initialGroupSize=Sim.getNumPeople();    //to insure the initial 
          // group size is never greater than the number of total people
        }
        for(int x = 0; x < initialGroupSize; x++){
          randStudent = people.get(rand.nextInt(people.size()));
          while(doesGroupContainStudent(randStudent)){
            randStudent = people.get(rand.nextInt(people.size()));
          }
          students.add(randStudent);
          randStudent.joinGroup(this);
        }
    }

    private ArrayList<Person> findStudentsToRecruit(ArrayList<Person> people){
        int numPeople = NUM_PEOPLE_TO_RECRUIT;
        ArrayList<Person> recruits = new ArrayList<Person>();
        Person randStudent;
        if(numPeople>Sim.getNumPeople()){
          numPeople=Sim.getNumPeople();    //to insure the initial group size 
          // is never greater than the number of total people
        }
        for(int x = 0; x < numPeople; x++){
          randStudent = people.get(rand.nextInt(people.size()));
          while(doesGroupContainStudent(randStudent)){
            randStudent = people.get(rand.nextInt(people.size()));
          }
          recruits.add(randStudent);
        }
        return recruits;
    }

    private void recruitStudent(Person s){
        if(!doesGroupContainStudent(s)){

          //FIX FOR DECIMALS
            double r = (affinityTo(s) + recruitmentFactor + 
                s.getExtroversion()*2 + rand.nextDouble()*2)/6.0; 
                //want to mess with balance here
            if(r>RECRUITMENT_REQUIRED){
                students.add(s);
                s.joinGroup(this);
            }
        }
    }
  
    private boolean doesGroupContainStudent(Person p){
        for (int x = 0; x<students.size(); x++){
          if (p.getID()==students.get(x).getID()){
            return true;
          }
        }
        return false;
    }
  
//    public boolean equals(Group a){
//        return (id==a.getID());
//    }

    /**
     * Return a number from 0 to 1 indicating the degree of affinity the
     *   Person passed has to the existing members of this group.
     */
    public double affinityTo(Person p) {
        if(getSize()>0){
              double temp=0;
              for(int x = 0; x<students.size(); x++){
                temp = p.similarityTo(students.get(x));
              }
              return temp/students.size();
        }else{
          return .5;
        }

        // write this maddie
        // ideas:
        //    for each of the person's attributes, find the avg number of
        //    group members (with that attribute, and then take the avg of
        //    those averages.
        //  Ex: The group has persons F, T, Q. The Person in question is
        //  person A. Person A has three attributes: 1, 4, and 5. Attribute
        //  1 is owned by F and T. Attribute 4 is owned by F, T, and Q.
        //  Attribute 5 is owned by no one in the group. So, the affinity
        //  for Person A to this group is (2/3 + 3/3 + 0/3)/3 = 5/3/3
        //
        // question: what to return from this method if the group is empty?
        // .5?
    }

   	private void influenceMembers(){
      if(students.size()>0){
    	ArrayList<Double> independentAverage = new ArrayList<Double>();
    	ArrayList<Double> dependentAverage = new ArrayList<Double>();
      	double tempTotal;
      	for (int x = 0; x<students.get(0).getIndependentAttributes().size(); 
                x++){    
        	tempTotal=0;
        	for (int y = 0; y<students.size(); y++){
          		tempTotal+=students.get(y).getIndependentAttributes().get(x);
        	}
        	independentAverage.add(tempTotal/students.size());
      	}
      	for (int x = 0; x<students.get(0).getDependentAttributes().size(); 
                x++){
        	tempTotal=0;
        	for (int y = 0; y<students.size(); y++){
          		tempTotal+=students.get(y).getDependentAttributes().get(x);
        	}
        	dependentAverage.add(tempTotal/students.size());
      	}

        //At this point, both independentAverage and dependentAverage are
        //filled the following should use two rands-- one to see if the
        //attribute should in fact change, and another to be used to
        //multiply by the distance to calculate how much it would increment
        //by note that a group's influence will never directly decrement
        //any attributes-- dependent attributes may only decrement by
        //indirect normalization We have to keep our numbers pretty low
        //here-- this will be called at every step
        
      	double distanceI;  //distance between current person's current 
        // independent attribute and the group's average attribute
      	double distanceD;  //distance between current person's current 
        // dependent attribute and group's average attribute
      	double increment; //how much each attribute will increment by
      	for(int x = 0; x<students.size(); x++){
        	for (int y = 0; y<independentAverage.size(); y++){
          		distanceI = independentAverage.get(y) - 
                    students.get(x).getIndependentAttributes().get(y);
          		distanceD = dependentAverage.get(y) - 
                    students.get(x).getDependentAttributes().get(y);
          		if(rand.nextDouble(true,true)<
                    LIKELIHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE && distanceI>0){
                    //rand subject to change 
            		increment = (rand.nextDouble(true,true)/52)*distanceI; 
                    //random number inclusively from 0-1, then divide by 5,
                    //then multiply by the distance that attribute is from
                    //the group's average
            		students.get(x).setIndAttrValue(y, 
                        (students.get(x).getIndependentAttributes().get(y)) +
                        increment);
          		}  

          		if(rand.nextDouble(true,true)<
                    LIKELIHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE && distanceD>0){  
            		increment = (rand.nextDouble(true, true)/5)*distanceD;
            		students.get(x).setDepAttrValue(y, 
                        (students.get(x).getDependentAttributes().get(y)) +
                        increment);  //Morgan's method
          		}
        	}
      	}
      }
    }

     private void possiblyLeaveGroup(Person p){
      if(rand.nextDouble(true,true)<
            LIKELIHOOD_OF_RANDOMLY_LEAVING_GROUP && 
            students.size()>MINIMUM_GROUP_SIZE){
    //    System.out.println("Person " + p.getID() + " has randomly left group " + getID());
        p.leaveGroup(this);
        removeStudent(p);
      }
    }

    /**
     * Make this group perform one month's actions. These include:
     * <ol>
     * <li>"Backwards influence" its members, by possibly altering some of
     * their attributes to make them more like those of the group at
     * large.</li>
     * <li>Recruit other students from the student body at large.</li>
     * <li>Give each member a chance to leave the group.</li>
     * </ol>
     * After this, the Group reschedules itself for the next month (or
     * August, if it's coming up on summertime.)
     * <p>Note that Groups only step during academic months.</p>
     */
      public void step(SimState state){
        influenceMembers();
        ArrayList<Person> recruits = findStudentsToRecruit(Sim.getPeople());
        for(int x = 0; x < recruits.size(); x++){
          recruitStudent(recruits.get(x));
        }
        for(int x = 0; x<students.size(); x++){
          possiblyLeaveGroup(students.get(x));
        }
        
        //If we've done the maximum number of iterations, then stop; 
        //otherwise, keep stepping
        if(numTimes >= Sim.MAX_ITER){
          System.out.println(this);
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
  
  
    /** Sets the recruitment factor for this group, which is a number in the
     * range 0 to 1 indicating how aggressive the group is in attracting
     * members. Higher numbers increase the likelihood of {@link
     * #recruitStudent(Person)} returning true.
     */
    public void setRecruitmentFactor(double r){
      recruitmentFactor=r;
    }
    
      /** Returns the number of students currently in the group.
       */
    public int getSize(){
      return students.size();
    }
  
    /** Gets the recruitment factor for this group (See {@link
     * #setRecruitmentFactor(double)}.)
     */
    public double getRecruitmentFactor(){
      return recruitmentFactor;
    }
    
    /* public int getCloseness(){
      return (tightness+frequency+recruitmentFactor)/3; 
      //maybe this could be used for leaving the group
    }*/
  

    private void listMembers(){
      System.out.println("The following students are in group " + id + ":");
      for(int x = 0; x < students.size(); x++){
        System.out.println("\t" + students.get(x));
      }
    }
  
    /** Get the ID of this group, a unique number across all group objects.
     */
    public int getID(){
      return id;
    }

    /** Returns the member whose position in the person vector is passed. 
     * (FIX: seems like a weird API?) */
    public Person getPersonAtIndex(int x){
      return students.get(x);
    }

    /** Removes the person passed from this group. If the person is not
     * already a member, does nothing. */
    public void removeStudent(Person p){
      for(int x = 0; x<students.size(); x++){
          if(students.get(x).getID( ) == p.getID( )){
            students.remove(x);
          }
      }
    }

    /** Clears all Person objects from this group. (This method will in
     * turn inform each ex-member that they are no longer members.) */
    public void removeEveryoneFromGroup(){
      for(int x = 0; x<students.size(); x++){
        students.get(x).leaveGroup(this);
      }
    }

}
