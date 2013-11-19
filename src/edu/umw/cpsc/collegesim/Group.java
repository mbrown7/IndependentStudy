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


public class Group implements Steppable{
  private final int MINIMUM_START_GROUP_SIZE = 3;
  private final int MAXIMUM_START_GROUP_SIZE = 8; 
  private final double RECRUITMENT_REQUIRED = .6;     //lower this to accept more students in group per step
  private final double LIKELYHOOD_OF_RANDOMLY_LEAVING_GROUP = .1;   //increase this to remove more students in group per step
  private final double LIKELYHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE = .1;
  private final int MINIMUM_GROUP_SIZE = 3;
  private final int NUM_PEOPLE_TO_RECRUIT = 10;
  private final double STANDARD_DEVIATION_WILLINGNESS = .15;
  private int id;
  private double recruitmentFactor;
  private MersenneTwisterFast rand = Sim.instance( ).random;
  int numTimes = 0;

  private ArrayList<Person> students;
  
  public Group(int x){
    id = x;
    rand = Sim.instance( ).random;
    recruitmentFactor = rand.nextDouble();
    students = new ArrayList<Person>();
  }

  public void selectStartingStudents(ArrayList<Person> people){
    int initialGroupSize = rand.nextInt(MAXIMUM_START_GROUP_SIZE-MINIMUM_START_GROUP_SIZE)+MINIMUM_START_GROUP_SIZE+1;
    Person randStudent;
    if(initialGroupSize>MINIMUM_GROUP_SIZE){
      initialGroupSize=MINIMUM_GROUP_SIZE;    //keeps groups at least the min
    }
    if(initialGroupSize>Sim.getNumPeople()){
      initialGroupSize=Sim.getNumPeople();    //to insure the initial group size is never greater than the number of total people
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

  public ArrayList<Person> findStudentsToRecruit(ArrayList<Person> people){
    int numPeople = NUM_PEOPLE_TO_RECRUIT;
    ArrayList<Person> recruits = new ArrayList<Person>();
    Person randStudent;
    if(numPeople>Sim.getNumPeople()){
      numPeople=Sim.getNumPeople();    //to insure the initial group size is never greater than the number of total people
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

  public void recruitStudent(Person s){
    if(!doesGroupContainStudent(s)){
      /*System.out.println("A: " + affinityTo(s));
      System.out.println("RF: " +recruitmentFactor);
      System.out.println("Willing: " + s.getWillingnessToMakeFriends());
      System.out.println("Rand: " + rand.nextInt(10));
      */

      //FIX FOR DECIMALS
        double r = (affinityTo(s) + recruitmentFactor + s.getWillingnessToMakeFriends()*2 + rand.nextDouble()*2)/6.0; //want to mess with balence here
     //   System.out.println("\nFinal Recruitment: " + r);
    //    System.out.println("Person " + s.getID() + " looks at group " + id);
        if(r>RECRUITMENT_REQUIRED){
            students.add(s);
            s.joinGroup(this);
     //     System.out.println("Person " + s.getID() + " joined group " + id);
        }
      }
    }
  
  public boolean doesGroupContainStudent(Person p){
    for (int x = 0; x<students.size(); x++){
      if (p.getID()==students.get(x).getID()){
        return true;
      }
    }
    return false;
  }
  
  public boolean equals(Group a){
    return (id==a.getID());
  }

   /*
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

   	public void influenceMembers(){
      if(students.size()>0){
   		//System.out.println("**Influence members**"); ADD BACK IN
    	ArrayList<Double> independentAverage = new ArrayList<Double>();
    	ArrayList<Double> dependentAverage = new ArrayList<Double>();
      	double tempTotal;
      	for (int x = 0; x<students.get(0).getIndependentAttributes().size(); x++){    
        	tempTotal=0;
        	for (int y = 0; y<students.size(); y++){
          		tempTotal+=students.get(y).getIndependentAttributes().get(x);
        	}
        	independentAverage.add(tempTotal/students.size());
      	}
      	for (int x = 0; x<students.get(0).getDependentAttributes().size(); x++){
        	tempTotal=0;
        	for (int y = 0; y<students.size(); y++){
          		tempTotal+=students.get(y).getDependentAttributes().get(x);
        	}
        	dependentAverage.add(tempTotal/students.size());
      	}

      	//At this point, both independentAverage and dependentAverage are filled
      	//the following should use two rands-- one to see if the attribute should in fact change, and another to be used to multiply by the distance to calculate how much it would increment by
      	//note that a group's influence will never directly decrement any attributes-- dependent attributes may only decrement by indirect normalization
      	//We have to keep our numbers pretty low here-- this will be called at every step
      	double distanceI;  //distance between current person's current independent attribute and the group's average attribute
      	double distanceD;  //distance between current person's current dependent attribute and group's average attribute
      	double increment; //how much each attribute will increment by
      	for(int x = 0; x<students.size(); x++){
        	for (int y = 0; y<independentAverage.size(); y++){
          		distanceI = independentAverage.get(y) - students.get(x).getIndependentAttributes().get(y);
          		distanceD = dependentAverage.get(y) - students.get(x).getDependentAttributes().get(y);
          		if(rand.nextDouble(true,true)<LIKELYHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE && distanceI>0){  //rand subject to change 
            		increment = (rand.nextDouble(true,true)/52)*distanceI; //random number inclusively from 0-1, then divide by 5, then multiply by the distance that attribute is from the group's average
            		students.get(x).setIndAttrValue(y, (students.get(x).getIndependentAttributes().get(y))+increment);
            		//System.out.println("Person " + students.get(x).getID() + " has changed an independent attribute"); ADD BACK IN
          		}  

          		if(rand.nextDouble(true,true)<LIKELYHOOD_OF_RANDOMLY_CHANGING_ATTRIBUTE && distanceD>0){  
            		increment = (rand.nextDouble(true, true)/5)*distanceD;
            		students.get(x).setDepAttrValue(y, (students.get(x).getDependentAttributes().get(y))+increment);  //Morgan's method
          			//System.out.println("Person " + students.get(x).getID() + " has changed a dependent attribute"); ADD BACK IN
          		}
        	}
      	}
      }
    }

     public void possiblyLeaveGroup(Person p){
      if(rand.nextDouble(true,true)<LIKELYHOOD_OF_RANDOMLY_LEAVING_GROUP&&students.size()>MINIMUM_GROUP_SIZE){
        p.leaveGroup(this);
        removeStudent(p);
    //    System.out.println("Removing Student "+p.getID()+" from group " + id);
      }
    }
      
    public void step(SimState state){
      influenceMembers();
      ArrayList<Person> recruits = findStudentsToRecruit(Sim.getPeople());
      for(int x = 0; x < recruits.size(); x++){
        recruitStudent(recruits.get(x));
      }
      for(int x = 0; x<students.size(); x++){
        possiblyLeaveGroup(students.get(x));
      }
      
      //If we've done the maximum number of iterations, then stop; otherwise, keep stepping
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


  public void setRecruitmentFactor(double r){
    recruitmentFactor=r;
  }
  
  public int getSize(){
    return students.size();
  }

  public double getRecruitmentFactor(){
    return recruitmentFactor;
  }
  
 /* public int getCloseness(){
    return (tightness+frequency+recruitmentFactor)/3; //maybe this could be used for leaving the group
  }*/


  public void listMembers(){
    System.out.println("The following students are in group " + id + ":");
    for(int x = 0; x < students.size(); x++){
      System.out.println("\t" + students.get(x));
    }
  }
  
  public int getID(){
    return id;
  }

  public Person getPersonAtIndex(int x){
    return students.get(x);
  }

  public void removeStudent(Person p){
      for(int x = 0; x<students.size(); x++){
          if(students.get(x).equals(p)){
            students.remove(x);
          }
      }
    }

    public void removeEveryoneFromGroup(){
      for(int x = 0; x<students.size(); x++){
        students.get(x).leaveGroup(this);
      }
    }

}