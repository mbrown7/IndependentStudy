
package edu.umw.cpsc.collegesim;

import java.util.ArrayList;
import ec.util.*;
//811




public class Group{
	//all hard coded rands are subject to change
	private int size = 0;//based off how many people join-- affects it for now by decreasing the recruitment factor when increased-- gotta think of a way to scale it though to effect the closeness appropriately 
	private int tightness=0;//based on individual students' willingness to make friends in the group
	private int frequency;//random 1-10
	private double recruitmentFactor;//random 1-10, slowly decreased as more people join
	static MersenneTwisterFast rand;
	
	
	private ArrayList<Person> students;
	
	public Group(){
		rand = new MersenneTwisterFast();
		frequency=rand.nextInt(10)+1; 
		recruitmentFactor=rand.nextInt(10)+1; 
		students = new ArrayList<Person>();
		
		
	}
	
	private void setSize(int s){
		size=s;
	}
	
	private void setTightness(int t){
		tightness=t;
	}
	
	private void setFrequency(int f){
		frequency=f;
	}
	
	private void setRecruitmentFactor(double r){
		recruitmentFactor=r;
	}
	
	private int getSize(){
		return size;
	}
	
	private int getTightness(){
		return tightness;
	}
	
	private int getFrequency(){
		return frequency;
	}
	
	private double getRecruitmentFactor(){
		return recruitmentFactor;
	}
	
	private int getCloseness(){
		return (int) (tightness+frequency+recruitmentFactor)/3;
	}

	public String toString(){
		return "Closeness: "+ getCloseness() + " (Size: " + size + " Tightness: " + tightness + " Frequency: " + frequency + " Recruitment Factor: "+ recruitmentFactor + ")";
	}
	
	void recruitStudent(Person s){
		double r = (recruitmentFactor + s.getWillingnessToMakeFriends()+rand.nextInt(10)+1)/3.0;
		if(r>7){
			students.add(s);
			s.incNumGroupsJoined();
			double rf= recruitmentFactor-rand.nextDouble();
			if(rf>1){
				recruitmentFactor=rf;
			}
		}
		size = students.size();
		if(size>0){
			int t=0;
			for(int x = 0; x<size; x++){
				t += students.get(x).getWillingnessToMakeFriends();
			}
			tightness = t/size;
		}
	}
	
}

