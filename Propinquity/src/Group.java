import java.util.ArrayList;
import ec.util.*;




public class Group{
	//all hard coded rands are subject to change
	int size = 0;//based off how many people join-- affects it for now by decreasing the recruitment factor when increased-- gotta think of a way to scale it though to effect the closeness appropriately 
	int tightness=0;//based on individual students' willingness to make friends in the group
	int frequency;//random 1-10
	double recruitmentFactor;//random 1-10, slowly decreased as more people join
	static MersenneTwisterFast rand;
	
	
	ArrayList<Student> students;
	
	public Group(){
		rand = new MersenneTwisterFast();
		frequency=rand.nextInt(10)+1; 
		recruitmentFactor=rand.nextInt(10)+1; 
		students = new ArrayList<Student>();
		
		
	}
	
	void setSize(int s){
		size=s;
	}
	
	void setTightness(int t){
		tightness=t;
	}
	
	void setFrequency(int f){
		frequency=f;
	}
	
	void setRecruitmentFactor(double r){
		recruitmentFactor=r;
	}
	
	int getSize(){
		return size;
	}
	
	int getTightness(){
		return tightness;
	}
	
	int getFrequency(){
		return frequency;
	}
	
	double getRecruitmentFactor(){
		return recruitmentFactor;
	}
	
	int getCloseness(){
		return (int) (tightness+frequency+recruitmentFactor)/3;
	}

	void printStatement(){
		System.out.println("Closeness: "+ getCloseness() + " (Size: " + size + " Tightness: " + tightness + " Frequency: " + frequency + " Recruitment Factor: "+ recruitmentFactor + ")");
	}
	
	void recruitStudent(Student s){
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

