package edu.umw.cpsc.collegesim;
import sim.engine.*;
import sim.util.*;
import sim.field.network.*;
import java.util.ArrayList;


/**
 * Homophily sub-simulation. (Look at Main.java for propinquity
 * sub-simulation.)
 * @author MB
 */
public class Sim extends SimState{

//	public Continuous2D room = new Continuous2D(1.0,100,100);

    // Undirected graph.
	public static Network people = new Network(false);
	public static Network lastMet = new Network(false);
	private static final int NUM_PEOPLE = 100;
	private static final int NUM_GROUPS = 30;
	private static ArrayList<Group> groups = new ArrayList<Group>();
	private static ArrayList<Person> peopleList = new ArrayList<Person>();
    private static long SEED = 0;
    private static Sim theInstance;
    public static int NUM_MONTHS_IN_ACADEMIC_YEAR = 9;
    public static int NUM_MONTHS_IN_SUMMER = 3;
    public static int NUM_MONTHS_IN_YEAR = NUM_MONTHS_IN_ACADEMIC_YEAR +
        NUM_MONTHS_IN_SUMMER;

    // Here is the schedule!
    // Persons run at clock time 0.5, 1.5, 2.5, ..., 8.5.
    // Groups run at clock time 1, 2, 3, ..., 9.
    boolean nextMonthInAcademicYear() {
        double curTime = Sim.instance().schedule.getTime();
        int curTimeInt = (int) Math.ceil(curTime);
        // if curTimeInt is 1, that means we are at the first month.
        int monthsWithinYear = curTimeInt % NUM_MONTHS_IN_YEAR;
        return (monthsWithinYear < NUM_MONTHS_IN_ACADEMIC_YEAR);
    }

    public static int getNumPeople( ){
    	return NUM_PEOPLE;
    }

    public int getNumGroups(){
    	return NUM_GROUPS;
    }

    public static synchronized Sim instance( ){
        if (theInstance == null){
            theInstance = new Sim(SEED);
        }
        return theInstance;
    }

    public static ArrayList<Person> getPeople(){
    	return peopleList;
    }
    
	public Sim(long seed){
		super(seed);
	}
	
	public void start( ){
		super.start( );
		//create people, put them in the network, and add them to the
		//schedule
		for(int i=0; i<NUM_PEOPLE; i++){
			Person person = new Person(i);
			peopleList.add(person);
			people.addNode(person);
			lastMet.addNode(person);
			schedule.scheduleOnceIn(1.5,person);
		}

		for(int x = 0; x<NUM_GROUPS; x++){
			Group group = new Group(x);
			group.selectStartingStudents(peopleList);
			group.listMembers();
			schedule.scheduleOnceIn(2.0,group);
			groups.add(group);
		}

	}
	
	public static void main(String[] args) {
        doLoop(new MakesSimState() {
            public SimState newInstance(long seed, String[] args) {
                Sim.SEED = seed;
                return instance();
            }
            public Class simulationClass() {
                return Sim.class;
            }
        }, args);
	}

	public void finish(){
		for(int x = 0; x<NUM_GROUPS; x++){
			groups.get(x).listMembers();
		}
		System.out.println("Person 0 should meet:"); //just an example of how getPeopleInGroups() should work
		Bag peopleInGroups = peopleList.get(0).getPeopleInGroups();
		for(int x = 0; x < peopleInGroups.size(); x++){
			System.out.println(peopleInGroups.get(x));
		}
	}

}
