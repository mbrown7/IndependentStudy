package edu.umw.cpsc.collegesim;
import sim.engine.*;
import sim.util.*;
import sim.field.network.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Homophily sub-simulation. (Look at Main.java for propinquity
 * sub-simulation.)
 * @author MB
 */
public class Sim extends SimState implements Steppable{

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
    public static final int MAX_ITER = 1000;
    public static final int numYears =  6;

    public static File outF;
	public static BufferedWriter outWriter;

    
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
			person.setYear(random.nextInt(4)+1);
			peopleList.add(person);
			people.addNode(person);
			lastMet.addNode(person);
			schedule.scheduleOnceIn(1.5,person);
		}

		for(int x = 0; x<NUM_GROUPS; x++){
			Group group = new Group(x);
			group.selectStartingStudents(peopleList);
			//group.listMembers();
			schedule.scheduleOnceIn(2.0,group);
			groups.add(group);
		}

		schedule.scheduleOnceIn(1, this);

	}
	
	public static void main(String[] args) throws IOException {
		/*outF = new File("output.txt");
		outF.createNewFile( );
		outWriter = new BufferedWriter(new FileWriter(outF));*/
        doLoop(new MakesSimState() {
            public SimState newInstance(long seed, String[] args) {
                Sim.SEED = seed;
                return instance();
            }
            public Class simulationClass() {
                return Sim.class;
            }
        }, args);
       /*
<<<<<<< HEAD
        //outWriter.close( );
=======
        Bag p = people.getAllNodes( );
        for(int j=0; j<p.size( ); j++){
        	Person person = (Person)p.get(j);
        	String message = Integer.toString(person.getID( )) + " ";
        	Bag b = Sim.instance( ).people.getEdgesIn(person);
        	int numFriends = 0;
        	for (int i=0; i<b.size(); i++) {
        		numFriends++;
        	}
        	message = message + Integer.toString(numFriends) + " "
        		+ Integer.toString(groups.size( )) + " " + person.getRace( ) + " " + person.getGender( )
        		+ " " + person.getWillingnessToMakeFriends( ) + "\n";
        	//Edit this try?
        	try {
        		Sim.outWriter.write(message);
        	} catch (IOException e) {
        		e.printStackTrace();
        	}
        }
        outWriter.close( );
>>>>>>> 4554d6937f367188ee37f28038cf7633f88696b3
*/
	}

	public void finish(){
		/*for(int x = 0; x<NUM_GROUPS; x++){
			groups.get(x).listMembers();
		}*/
		//System.out.println("Person 0 should meet:"); //just an example of how getPeopleInGroups() should work
		Bag peopleInGroups = peopleList.get(0).getPeopleInGroups();
		/*for(int x = 0; x < peopleInGroups.size(); x++){
			System.out.println(peopleInGroups.get(x));
		}*/

	}

	public void step(SimState state){
		if((int) (schedule.getTime()/NUM_MONTHS_IN_YEAR)<=numYears){
			System.out.println("Year: "+(int) schedule.getTime()/NUM_MONTHS_IN_YEAR);
			if(nextMonthInAcademicYear()){
				schedule.scheduleOnceIn(NUM_MONTHS_IN_ACADEMIC_YEAR, this);
				if(outWriter!=null){
					try{
						outWriter.close();
					}catch(IOException e){
						System.out.println("Couldn't close file");
					}
				}
				for(int x = 0; x<peopleList.size(); x++){
					peopleList.get(x).incrementYear();
				}
				String f="year"+(int) (schedule.getTime()/NUM_MONTHS_IN_YEAR);
				try{
					outF = new File(f);
					outF.createNewFile( );
					outWriter = new BufferedWriter(new FileWriter(outF));
				}catch(IOException e){
					System.out.println("Couldn't create file");
				}
			}else{
				System.out.println("End of year: "+(int) (schedule.getTime()/NUM_MONTHS_IN_YEAR));
				schedule.scheduleOnceIn(NUM_MONTHS_IN_SUMMER, this);
				ArrayList<Person> toRemove = new ArrayList<Person>();
				for(int x = 0; x<peopleList.size(); x++){
					if(peopleList.get(x).getYear()==4){
						System.out.println("Person " + peopleList.get(x).getID() + " has graduated! Congrats!");
						toRemove.add(peopleList.get(x));
					}
				}
				for(int x = 0; x<toRemove.size(); x++){
					toRemove.get(x).printToFile();
					peopleList.remove(toRemove.get(x));
				}
			}
		}else{
			schedule.seal();
		}

	}
}
