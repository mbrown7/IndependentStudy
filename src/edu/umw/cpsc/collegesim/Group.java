
package edu.umw.cpsc.collegesim;

import java.util.ArrayList;
import ec.util.*;
//811




public class Group{
	//all hard coded rands are subject to change
	private int id;
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
	
	void recruitStudent(Person s){
		double r = (recruitmentFactor + s.getWillingnessToMakeFriends()+rand.nextInt(10)+1)/3.0;
		if(r>7){
			students.add(s);
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
	
	private boolean doesGroupContainStudent(Person p){
		
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
	
	public void setRecruitmentFactor(double r){
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
		return (int) (tightness+frequency+recruitmentFactor)/3;
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
}

