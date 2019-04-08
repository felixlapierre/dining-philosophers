
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor {

    /*
	 * ------------
	 * Data members
	 * ------------
     */
    //Task 2: Implementation of monitor
    //To handle sync of talking: a boolean indicating if a philosopher is talking
    private boolean aPhilosopherIsTalking = false;
    
    //To handle the food cycle of a philosopher
    private enum Status {full, hungry, hasRightChopstick, eating};
    
    //To hold the status of all the philosophers
    private ArrayList<Status> state;
    
    //To hold the conditionals of all the philosophers
    private Lock lock = new ReentrantLock();
    private ArrayList<Condition> chopsticks;
    private Condition talking;
    
    //To hold the number of philosophers at the table
    int nbPhil;
    
    //Task 5: Map philosopher TIDs to seats at the table
    private HashMap<Integer, Integer> assignedSeats;
    
    //Task 6: Pepper shakers
    int peppers = 0;
    final int MAX_PEPPERS = 2;
    private Condition pepper;

    /**
     * Constructor
     */
    public Monitor(int piNumberOfPhilosophers) {
        //Task 2: Set number of philosophers and initialize data
        nbPhil = piNumberOfPhilosophers;
        state = new ArrayList<Status>(nbPhil);
        chopsticks = new ArrayList<Condition>(nbPhil);
        talking = lock.newCondition();
        for(int i = 0; i < nbPhil; i++)
        {
            state.add(Status.full);
            chopsticks.add(lock.newCondition());
        }
        
        //Task 5: Assign seats based on TID for each philosopher
        assignedSeats = new HashMap<Integer, Integer>();
        //Then, assign the starting philosophers to the seat corresponding
        //to their TID
        for(int i = 0; i < nbPhil; i++)
        {
            assignedSeats.put(i + 1, i);
        }
        
        //Task 6: Pepper shakers
        pepper = lock.newCondition();
    }

    /*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
     */
    //Task 2: Procedures for getting the left and right philosopher
    private int left(int id)
    {
        return (id > 0) ? id - 1 : nbPhil - 1;
    }
    
    private int right(int id)
    {
        return (id < nbPhil - 1) ? id + 1 : 0;
    }
    /**
     * Task 2:
     * Checks if the philosopher with the given id can
     * pick up the chopsticks and eat
     */
    private void check(int id)
    {
        if(bothChopsticksFree(id)
            && iWantToEat(id))
        {
            state.set(id, Status.eating);
        }
        else if(allowedToTakeOneChopstick(id)
                && rightChopstickFree(id)
                && iWantToEat(id))
        {
            state.set(id, Status.hasRightChopstick);
        }
    }
    
    //Task 2: Split check into smaller methods
    private boolean bothChopsticksFree(int id)
    {
        return state.get(left(id)) != Status.eating
            && state.get(left(id)) != Status.hasRightChopstick
            && state.get(right(id)) != Status.eating;
    }
    
    private boolean iWantToEat(int id)
    {
        return state.get(id) == Status.hungry || state.get(id) == Status.hasRightChopstick;
    }
    
    private boolean allowedToTakeOneChopstick(int id)
    {
        return id % 2 == 0;
    }
    
    private boolean rightChopstickFree(int id)
    {
        return state.get(right(id)) != Status.eating;
    }
    
    /**
     * Task 2:
     * Grants request (returns) to eat when both chopsticks/forks are available.
     * Else forces the philosopher to wait()
     */
    public void pickUp(final int piTID) {
        //Task2: Implementation of pickUp()
        lock.lock();
        
        //Task 5: Get the philosopher's assigned seat
        int id = getSeat(piTID);
        
        try{
            state.set(id, Status.hungry);
            check(id);
            if(state.get(id) == Status.hungry)
            {
                System.out.println("Philosopher " + (piTID) + " is waiting to eat.");
                chopsticks.get(id).await();
            }
            else if (state.get(id) == Status.hasRightChopstick)
            {
                System.out.println("Philosopher " + (piTID) + " has taken the right chopstick");
                chopsticks.get(id).await();
            }
            
            //Task 6: Grab a pepper shaker
            peppers++;
            if(peppers > MAX_PEPPERS)
            {
                System.out.println("Philosopher " + piTID + " is waiting for a pepper shaker");
                pepper.await();
            }
            System.out.println("Philosopher " + piTID + " has taken a pepper shaker");
            
            assert(state.get(id) == Status.eating);
            assert(state.get(left(id)) != Status.eating
                    && state.get(left(id)) != Status.hasRightChopstick);
            assert(state.get(right(id)) != Status.eating);
        }
        catch (InterruptedException e)
        {
            System.err.println("Monitor.pickUp():");
            DiningPhilosophers.reportException(e);
            System.exit(1);
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Task 2:
     * When a given philosopher's done eating, they put the chopstiks/forks down
     * and let others know they are available.
     */
    public void putDown(final int piTID) {
        lock.lock();
        
        int id = getSeat(piTID);

        state.set(id, Status.full);
        
        //Task 6: Put down a pepper shaker
        peppers--;
        System.out.println("Philosopher " + piTID + " puts down a pepper shaker");
        pepper.signal();
        
        check(left(id));
        if(state.get(left(id)) == Status.eating)
            chopsticks.get(left(id)).signal();
        
        check(right(id));
        if(state.get(right(id)) == Status.eating)
            chopsticks.get(right(id)).signal();

        lock.unlock();
    }

    /**
     * Task 2:
     * Only one philosopher at a time is allowed to philosophy (while she is not
     * eating).
     */
    public void requestTalk() {
        //If a philosopher is talking, wait for them to finish, then talk.
        lock.lock();
        try
        {
            if(aPhilosopherIsTalking)
            {
                talking.await();
            }
            aPhilosopherIsTalking = true;
        }
        catch (InterruptedException e)
        {
            System.err.println("Monitor.requestTalk():");
            DiningPhilosophers.reportException(e);
            System.exit(1);
        }
        finally
        {
            lock.unlock();
        }
    }

    /**
     * Task 2:
     * When one philosopher is done talking stuff, others can feel free to start
     * talking.
     */
    public void endTalk() {
        //A philosopher is no longer talking. Notify one waiting philosopher
        //that they can start talking.
        lock.lock();

        aPhilosopherIsTalking = false;
        talking.signal();

        lock.unlock();
    }
    
    /**
     * Task 5: Get the assigned seat of a philosopher
     */
    private int getSeat(int TID)
    {
        return assignedSeats.get(TID);
    }
    
    /**
     * Task 5:
     * Allow a philosopher to join the table
     */
    public void joinTable(int threadId)
    {
        lock.lock();
        //This philosopher will sit at the last seat
        state.add(Status.full);
        
        assignedSeats.put(threadId, nbPhil);
        chopsticks.add(lock.newCondition());
        
        //There is now one more philosopher
        nbPhil++;
        
        //Let everyone know
        System.out.println("Philosopher " + threadId + " has joined the table.");
        lock.unlock();
    }
    
    /**
     * Task 5:
     * Allow a philosopher to leave the table
     * @param threadID The thread ID of the philosopher that wants to leave
     */
    public void leaveTable(int threadID)
    {
        lock.lock();
        try
        {
            int id = getSeat(threadID);

            //Wait for my neighbors to finish eating
            state.set(id, Status.hungry);
            check(id);
            if(state.get(id) == Status.hungry)
                chopsticks.get(id).await();
            
            //Leave the table
            state.remove(id);
            chopsticks.remove(id);
            nbPhil--;
            
            //Adjust map for accuracy
            assignedSeats.remove(threadID);
            for(int key : assignedSeats.keySet())
            {
                int seat = assignedSeats.get(key);
                if(seat > id)
                    assignedSeats.put(key, seat - 1);
            }
            
            //Print that someone has left
            System.out.println("Philosopher " + (threadID) + " has left the table.");
        }
        catch(InterruptedException e)
        {
            System.err.println("Monitor.leaveTable(): ");
            DiningPhilosophers.reportException(e);
            System.exit(1);
        }
        
        lock.unlock();
    }
}

// EOF
