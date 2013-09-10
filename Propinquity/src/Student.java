import ec.util.*;

//very basic student class-- has a factor of "willingness to make friends" which affects the closeness of groups. Also keeps track of how many groups the student is in

public class Student {
	
	static MersenneTwisterFast rand;
	int willingnessToMakeFriends;
	int numGroupsJoined=0;
	
	public Student(){
		rand = new MersenneTwisterFast();
		willingnessToMakeFriends = rand.nextInt(10)+1;
	}

	int getWillingnessToMakeFriends(){
		return willingnessToMakeFriends;
	}
	
	void incNumGroupsJoined(){
		numGroupsJoined++;
	}
	
	void printStatement(){
		System.out.println("Willingness: "+willingnessToMakeFriends+" Number of Groups: " + numGroupsJoined);
	}
	
}
