package edu.umw.cpsc.collegesim;
import sim.engine.*;
import sim.util.*;
import sim.field.network.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;


/** The top-level singleton simulation class, with main(). */
public class Sim extends SimState implements Steppable{

//	public static Network lastMet = new Network(false);

    /**
     * The number of people, of random year-in-college (fresh, soph, etc.)
     * that the simulation will begin with. */
	public static final int INIT_NUM_PEOPLE = 100;


    /**
     * The number of groups, with random initial membership, that the
     * simulation will begin with. */
	public static final int INIT_NUM_GROUPS = 30;


    /**
     * The number of newly enrolling freshmen each year.     */
	public static final int NUM_PEOPLE_ENROLLING_EACH_YEAR = 25;


    /**
     * The number of groups, with random initial membership, that the
     * simulation will begin with. */
	public static final int NUM_GROUPS_ADDED_EACH_YEAR = 10;


    /**
     * The per-year probability of each student dropping out of college. */
	public static final double DROPOUT_RATE = .02;

	private static ArrayList<Group> groups = new ArrayList<Group>();
	private static ArrayList<Person> peopleList = new ArrayList<Person>();
    private static long SEED = 0;
    private static Sim theInstance;

    public static final int NUM_MONTHS_IN_ACADEMIC_YEAR = 9;
    public static final int NUM_MONTHS_IN_SUMMER = 3;
    public static final int NUM_MONTHS_IN_YEAR = NUM_MONTHS_IN_ACADEMIC_YEAR +
        NUM_MONTHS_IN_SUMMER;
    public static final int MAX_ITER = 1000;
    public static final int NUM_SIMULATION_YEARS=  8;

    private static File outF;
	private static BufferedWriter outWriter;
	private static int currentStudentID = 0;
	private static int currentGroupID = 0; 

    
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
    	return peopleList.size();
    }

    public int getNumGroups(){
    	return groups.size();
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
		for(int i=0; i<INIT_NUM_PEOPLE; i++){
			Person person = new Person(currentStudentID);
			currentStudentID++;
			person.setYear(random.nextInt(4)+1);
			peopleList.add(person);
			Person.addPerson(person);
//			lastMet.addNode(person);
			schedule.scheduleOnceIn(1.5,person);
		}

		for(int x = 0; x<INIT_NUM_GROUPS; x++){
			Group group = new Group(currentGroupID, peopleList);
			currentGroupID++;
			schedule.scheduleOnceIn(2.0,group);
			groups.add(group);
		}

		schedule.scheduleOnceIn(1.1, this);

	}
	
    /**
     * Run the simulation from the command line (no arguments necessary.) */
	public static void main(String[] args) throws IOException {
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

    private boolean isEndOfSim() {
        return (schedule.getTime()/NUM_MONTHS_IN_YEAR) > NUM_SIMULATION_YEARS;
    }

    private int getCurrYearNum() {
        return (int) schedule.getTime()/NUM_MONTHS_IN_YEAR;
    }

    private void dumpToFiles() {

        if(outWriter!=null){
            try{
                outWriter.close();
            }catch(IOException e){
                System.out.println("Couldn't close file");
            }
        }
        //if((int)(schedule.getTime()/NUM_MONTHS_IN_YEAR)!=NUM_SIMULATION_YEARS){
        if(!isEndOfSim()) {
            String f="year"+(int) (schedule.getTime()/NUM_MONTHS_IN_YEAR);
            try{
                outF = new File(f);
                outF.createNewFile( );
                outWriter = new BufferedWriter(new FileWriter(outF));
            }catch(IOException e){
                System.out.println("Couldn't create file");
                e.printStackTrace();
                System.exit(1);
            }
            for(int x = 0; x<peopleList.size(); x++){
                peopleList.get(x).printToFile(outWriter);
                peopleList.get(x).toString();
            }
        }
    }

	public void step(SimState state){

		if(!isEndOfSim()) {

			if(nextMonthInAcademicYear()){
                /*
                 * August.
                 * Year-start activities. Increment everyone's year, enroll
                 * the new freshman class, create new groups.
                 */
                System.out.println("---------------");
                System.out.println("Starting year: "+getCurrYearNum());
				for(int x = 0; x<peopleList.size(); x++){
					peopleList.get(x).incrementYear();
				}
				for(int x = 0; x<NUM_PEOPLE_ENROLLING_EACH_YEAR; x++){
					Person person = new Person(currentStudentID);
					currentStudentID++;
					person.setYear(1);
					peopleList.add(person);
					Person.addPerson(person);
					schedule.scheduleOnceIn(1.5,person);
				}
				for(int x = 0; x<NUM_GROUPS_ADDED_EACH_YEAR; x++){
					Group group = new Group(currentGroupID, peopleList);
					currentGroupID++;
					schedule.scheduleOnceIn(2.0,group);
					groups.add(group);
				}
                /*
                 * The new academic year is now ready to begin! Schedule
                 * myself to wake up in May.
                 */
				schedule.scheduleOnceIn(NUM_MONTHS_IN_ACADEMIC_YEAR, this);

			}else{

                /*
                 * May.
                 * Year-end activities. Dump output files, graduate and
                 * dropout students, remove some groups.
                 */
				System.out.println("End of year: "+getCurrYearNum());
				ArrayList<Person> toRemove = new ArrayList<Person>();
				ArrayList<Group> toRemoveGroups = new ArrayList<Group>();

                dumpToFiles();
				if(!isEndOfSim()) {
					for(int x = 0; x<peopleList.size(); x++){
						if(peopleList.get(x).getYear()>=4){
							System.out.println("Person " + 
                                peopleList.get(x).getID() +
                                " has graduated! Congrats!");
							toRemove.add(peopleList.get(x));
						}else if(random.nextDouble()<=DROPOUT_RATE){
							System.out.println("Person " +
                                peopleList.get(x).getID() +
                                " has dropped out of school.");
							toRemove.add(peopleList.get(x));
						}
					}
					for(int x = 0; x<groups.size(); x++){
						if(random.nextDouble(true, true)>.75){
							System.out.println("Removing group " +
                                groups.get(x).getID());
							toRemoveGroups.add(groups.get(x));
						}
					}
					for(int x = 0; x<toRemoveGroups.size(); x++){
						toRemoveGroups.get(x).removeEveryoneFromGroup();
						groups.remove(toRemoveGroups.get(x));
					}
					for(int x = 0; x<toRemove.size(); x++){
						toRemove.get(x).leaveUniversity();
						peopleList.remove(toRemove.get(x));
					}
					toRemoveGroups.clear();
					toRemove.clear();
				}
                /*
                 * The academic year is now complete -- have a great summer!
                 * Schedule myself to wake up in August.
                 */
				schedule.scheduleOnceIn(NUM_MONTHS_IN_SUMMER, this);
			}
		}else{
			schedule.seal();
		}

	}

}
