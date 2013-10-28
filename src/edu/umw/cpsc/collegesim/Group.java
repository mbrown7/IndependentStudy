
package edu.umw.cpsc.collegesim;

import java.util.ArrayList;
import ec.util.*;
import sim.engine.*;





public class Group implements Steppable{
	//all hard coded rands are subject to change
	private final int RECRUITMENT_REQUIRED = 7;
	private final int MINIMUM_START_GROUP_SIZE = 3;
	private final int MAXIMUM_START_GROUP_SIZE = 6; //MUST be lower than the number of total people-- may want to check later
	private final int INDEPENDENT_ATTRIBUTE_POOL = 10;	//need to find a better way to do this than changing both places
	private final int DEPENDENT_ATTRIBUTE_POOL = 10;
	private int id;
	private int size = 0;//based off how many people join-- affects it for now by decreasing the recruitment factor when increased-- gotta think of a way to scale it though to effect the closeness appropriately 
	private int tightness=0;//based on individual students' willingness to make friends in the group
	private int frequency;//random 1-10
	private int recruitmentFactor;//random 1-10
	static MersenneTwisterFast rand;	//I think Stephen mentioned to use a MersenneTwist from elsewhere so I don't get a new one each time? *check with Stephen

	private ArrayList<Person> students;
	
	public Group(int x){
		id = x;
		rand = Sim.instance( ).random;
		frequency=rand.nextInt(10)+1; 
		recruitmentFactor=rand.nextInt(10)+1; 
		students = new ArrayList<Person>();
	}

	void selectStartingStudents(ArrayList<Person> people){
		int initialGroupSize = rand.nextInt(MAXIMUM_START_GROUP_SIZE-MINIMUM_START_GROUP_SIZE)+MINIMUM_START_GROUP_SIZE+1;
		Person randStudent;
		for(int x = 0; x < initialGroupSize; x++){
			randStudent = people.get(rand.nextInt(people.size()));
			while(doesGroupContainStudent(randStudent)){
				randStudent = people.get(rand.nextInt(people.size()));
			}
			students.add(randStudent);
			randStudent.joinGroup(this);
		}
		size = students.size();
	}

	void recruitStudent(Person s){
		System.out.println("A: " + affinityTo(s));
		System.out.println("RF: " +recruitmentFactor);
		System.out.println("Willing: " + s.getWillingnessToMakeFriends());
		System.out.println("Rand: " + rand.nextInt(10));
     	double r = (affinityTo(s) + recruitmentFactor + s.getWillingnessToMakeFriends()+rand.nextInt(10)+1)/4.0; //want to mess with balence here
     	System.out.println("R: " + r);
     	System.out.println("Person " + s.getID() + " looks at group " + id +"\n");
     	if(r>RECRUITMENT_REQUIRED){
     	  students.add(s);
     	  s.joinGroup(this);
     	  System.out.println("Person " + s.getID() + " joined group " + id +"\n");
     	}
     	size = students.size();
     	int t=0;
     	for(int x = 0; x<size; x++){
     		t += students.get(x).getWillingnessToMakeFriends();
     	}
     	if(size>0){
    		tightness = t/size;
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
	
	boolean equals(Group a){
		if(id==a.getID()){
			return true;
		}
		return false;
	}

	 /*
     * Return a number from 0 to 1 indicating the degree of affinity the
     *   Person passed has to the existing members of this group.
     */
	double affinityTo(Person p) {
    	if(size>0){
    		double temp=0;
    		for(int x = 0; x<students.size(); x++){
    			temp = p.similarityTo(students.get(x));
    			//System.out.println("Temp: "+temp);
    		}
    		return temp/students.size();
    	}else{
    		return 5;
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

   	void influenceMembers(){
   		System.out.println("**Influence members**");
    	ArrayList<Double> independentAverage = new ArrayList<Double>();
    	ArrayList<Double> dependentAverage = new ArrayList<Double>();
      	double tempTotal;
      	for (int x = 0; x<INDEPENDENT_ATTRIBUTE_POOL; x++){    
        	tempTotal=0;
        	for (int y = 0; y<students.size(); y++){
          		tempTotal+=students.get(y).getIndependentAttributes().get(x);
        	}
        	independentAverage.add(tempTotal/students.size());
      	}
      	for (int x = 0; x<DEPENDENT_ATTRIBUTE_POOL; x++){
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
          		if(rand.nextDouble(true,true)>.97 && distanceI>0){  //rand subject to change 
            		increment = (rand.nextDouble(true,true)/5)*distanceI; //random number inclusively from 0-1, then divide by 5, then multiply by the distance that attribute is from the group's average
            		students.get(x).setIndAttrValue(y, (students.get(x).getIndependentAttributes().get(y))+increment);
            		System.out.println("Person " + students.get(x).getID() + "has changed an independent attribute");
          		}  

          		if(rand.nextDouble(true,true)>.97 && distanceD>0){  
            		increment = (rand.nextDouble(true, true)/5)*distanceD;
            		students.get(x).setDepAttrValue(y, (students.get(x).getDependentAttributes().get(y))+increment);  //Morgan's method
          			System.out.println("Person " + students.get(x).getID() + " has changed a dependent attribute");
          		}
        	}
      	}
    }
      
    public void step(SimState state){
    	influenceMembers();
 	}

	
	public void setSize(int s){
		size=s;
	}
	
	public void setTightness(int t){
		tightness=t;
	}
	
	public void setFrequency(int f){
		frequency=f;
	}
	
	public void setRecruitmentFactor(int r){
		recruitmentFactor=r;
	}
	
	public int getSize(){
		return students.size();
	}
	
	public int getTightness(){
		return tightness;
	}
	
	public int getFrequency(){
		return frequency;
	}
	
	public double getRecruitmentFactor(){
		return recruitmentFactor;
	}
	
	public int getCloseness(){
		return (tightness+frequency+recruitmentFactor)/3;
	}

	public String toString(){
		return "Closeness: "+ getCloseness() + " (Size: " + size + " Tightness: " + tightness + " Frequency: " + frequency + " Recruitment Factor: "+ recruitmentFactor + ")";
	}

	public void listMembers(){
		System.out.println("The following students are in group " + id + ":");
		for(int x = 0; x < students.size(); x++){
			System.out.println("\t" + students.get(x).getID() +" IA: " + students.get(x).getIndependentAttributes());// + " DA: " + students.get(x).getDependentAttributes());
		}
	}
	
	public int getID(){
		return id;
	}
	
	public void setID(int i){
		id=i;
	}

	public Person getPersonAtIndex(int x){
		return students.get(x);
	}

}

