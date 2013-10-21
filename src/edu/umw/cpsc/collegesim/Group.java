
package edu.umw.cpsc.collegesim;

import java.util.ArrayList;
import ec.util.*;




public class Group{
	//all hard coded rands are subject to change
	int NUM_CONSTANT_ATTRIBUTES = 10; //this is to be changed if morgan changes her constant-- maybe talk to stephen about keeping them synched?
	private int id;
	private int size = 0;//based off how many people join-- affects it for now by decreasing the recruitment factor when increased-- gotta think of a way to scale it though to effect the closeness appropriately 
	private int tightness=0;//based on individual students' willingness to make friends in the group
	private int frequency;//random 1-10
	private int recruitmentFactor;//random 1-10
	static MersenneTwisterFast rand;	//I think Stephen mentioned to use a MersenneTwist from elsewhere so I don't get a new one each time? *check with Stephen

	private ArrayList<Person> students;
	
	public Group(){
		rand = new MersenneTwisterFast();
		frequency=rand.nextInt(10)+1; 
		recruitmentFactor=rand.nextInt(10)+1; 
		students = new ArrayList<Person>();
	}

	 void recruitStudent(Person s){
     double r = (affinityTo(s) + recruitmentFactor + s.getWillingnessToMakeFriends()+rand.nextInt(10)+1)/4.0; //want to mess with balence here
     if(r>7){
       students.add(s);
     }
     size = students.size();
     int t=0;
     for(int x = 0; x<size; x++){
       t += students.get(x).getWillingnessToMakeFriends();
     }
     tightness = t/size;
   }
	
	private boolean doesGroupContainStudent(Person p){
		for (int x = 0; x<students.size(); x++){
			if (p.getID()==students.get(x).getID()){
				return true;
			}
		}
		return false;
	}
	
	boolean equals(Group a, Group b){
		if(a.getID()==b.getID()){
			return true;
		}
		return false;
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
		return size;
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
	
	public int getID(){
		return id;
	}
	
	public void setID(int i){
		id=i;
	}

    /**
     * Return a number from 0 to 1 indicating the degree of affinity the
     *   Person passed has to the existing members of this group.
     */
    double affinityTo(Person p) {
    	double temp=0;
    	for(int x = 0; x<students.size(); x++){
    		temp = p.similarityTo(students.get(x));
    	}
    	return temp/students.size();

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
      void influnceMembers(){
      ArrayList<Double> independentAverage = new ArrayList<Double>();
      ArrayList<Double> dependentAverage = new ArrayList<Double>();
      double tempTotal;

      for (int x = 0; x<independentAverage.size(); x++){    //when all is working, may want to switch x and y-- I use them for opposite tasks lower in the method 
        tempTotal=0;
        for (int y = 0; y<students.size(); y++){
          tempTotal+=students.get(y).getIndependentAttributes().get(x);
        }
        independentAverage.set(x, (tempTotal/students.size()));
      }

      for (int x = 0; x<dependentAverage.size(); x++){    //when all is working, may want to switch x and y-- I use them for opposite tasks lower in the method 
        tempTotal=0;
        for (int y = 0; y<students.size(); y++){
          tempTotal+=students.get(y).getDependentAttributes().get(x);
        }
        dependentAverage.set(x, (tempTotal/students.size()));
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
            //need method for changing independent attributes that caps attributes at 1
          }  

          if(rand.nextDouble(true,true)>.97 && distanceD>0){  
            increment = (rand.nextDouble(true, true)/5)*distanceD;
            students.get(x).setAttrValue(y, (students.get(x).getDependentAttributes().get(y))+increment);  //Morgan's method
          }

        }
      }
     }
}

