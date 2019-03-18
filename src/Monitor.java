
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


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
    //To handle sync of talking: a boolean indicating if a philosopher is talking
    private boolean aPhilosopherIsTalking = false;
    
    //To handle the food cycle of a philosopher
    private enum Status {full, hungry, eating};
    
    //To hold the status of all the philosophers
    private Status[] state;
    
    //To hold the conditionals of all the philosophers
    private Lock lock = new ReentrantLock();
    private Condition[] chopsticks;
    private Condition talking;
    
    //To hold the number of philosophers at the table
    int nbPhil;

    /**
     * Constructor
     */
    public Monitor(int piNumberOfPhilosophers) {
        // TODO: set appropriate number of chopsticks based on the # of philosophers
        nbPhil = piNumberOfPhilosophers;
        state = new Status[nbPhil];
        chopsticks = new Condition[nbPhil];
        talking = lock.newCondition();
        for(int i = 0; i < nbPhil; i++)
        {
            state[i] = Status.full;
            chopsticks[i] = lock.newCondition();
        }
    }

    /*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
     */
    private int left(int id)
    {
        return (id > 0) ? id - 1 : nbPhil - 1;
    }
    
    private int right(int id)
    {
        return (id < nbPhil - 1) ? id + 1 : 0;
    }
    /**
     * Checks if the philosopher with the given id can
     * pick up the chopsticks and eat
     */
    private void check(int id)
    {
        if(state[left(id)] != Status.eating
            && state[right(id)] != Status.eating
            && state[id] == Status.hungry)
        {
            state[id] = Status.eating;
        }
    }
    /**
     * Grants request (returns) to eat when both chopsticks/forks are available.
     * Else forces the philosopher to wait()
     */
    public void pickUp(final int piTID) {
        //Task2: Implementation of pickUp()
        lock.lock();
        try{
            state[piTID] = Status.hungry;
            check(piTID);
            if(state[piTID] == Status.hungry)
                chopsticks[piTID].await();
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
     * When a given philosopher's done eating, they put the chopstiks/forks down
     * and let others know they are available.
     */
    public void putDown(final int piTID) {
        lock.lock();
        state[piTID] = Status.full;
        check(left(piTID));
        if(state[left(piTID)] == Status.eating)
            chopsticks[left(piTID)].notify();
        check(right(piTID));
        if(state[right(piTID)] == Status.eating)
            chopsticks[right(piTID)].notify();
        lock.unlock();
    }

    /**
     * Only one philosopher at a time is allowed to philosophy (while she is not
     * eating).
     */
    public void requestTalk() {
        //Task 2: Implementation of requestTalk()
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
     * When one philosopher is done talking stuff, others can feel free to start
     * talking.
     */
    public void endTalk() {
        //Task 2: Implementation of endTalk()
        //A philosopher is no longer talking. Notify one waiting philosopher
        //that they can start talking.
        lock.lock();
        aPhilosopherIsTalking = false;
        talking.notify();
        lock.unlock();
    }
}

// EOF
