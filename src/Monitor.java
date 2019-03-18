
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
    boolean aPhilosopherIsTalking = false;
    
    //To handle the food cycle of a philosopher
    enum Status {full, hungry, eating};
    
    //To hold the status of all the philosophers
    Status[] state;
    
    //To hold the conditionals of all the philosophers
    
    //To hold the number of philosophers at the table
    int nbPhil;

    /**
     * Constructor
     */
    public Monitor(int piNumberOfPhilosophers) {
        // TODO: set appropriate number of chopsticks based on the # of philosophers
        nbPhil = piNumberOfPhilosophers;
        state = new Status[nbPhil];
        for(int i = 0; i < nbPhil; i++)
        {
            state[i] = Status.full;
        }
    }

    /*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
     */
    
    /**
     * Checks if the philosopher with the given id can
     * pick up the chopsticks and eat
     */
    private void check(int id)
    {
        int left = (id > 0) ? id - 1 : nbPhil - 1;
        
        int right = (id < nbPhil - 1) ? id + 1 : 0;
        
        if(state[left] != Status.eating
            && state[right] != Status.eating
            && state[(id)] == Status.hungry)
        {
            state[id] = Status.eating;
        }
    }
    /**
     * Grants request (returns) to eat when both chopsticks/forks are available.
     * Else forces the philosopher to wait()
     */
    public synchronized void pickUp(final int piTID) {
        //Task2: Implementation of pickUp()
        try{
            state[piTID] = Status.hungry;
            check(piTID);
            if(state[piTID] == Status.hungry)
                wait();
        } catch (InterruptedException e)
        {
            System.err.println("Monitor.pickUp():");
            DiningPhilosophers.reportException(e);
            System.exit(1);
        }
        
    }

    /**
     * When a given philosopher's done eating, they put the chopstiks/forks down
     * and let others know they are available.
     */
    public synchronized void putDown(final int piTID) {
        state[piTID] = Status.full;
    }

    /**
     * Only one philosopher at a time is allowed to philosophy (while she is not
     * eating).
     */
    public synchronized void requestTalk() {
        //Task 2: Implementation of requestTalk()
        //If a philosopher is talking, wait for them to finish, then talk.
        try
        {
            if(aPhilosopherIsTalking)
            {
                wait();
            }
            aPhilosopherIsTalking = true;
        }
        catch (InterruptedException e)
        {
            System.err.println("Monitor.requestTalk():");
            DiningPhilosophers.reportException(e);
            System.exit(1);
        }
    }

    /**
     * When one philosopher is done talking stuff, others can feel free to start
     * talking.
     */
    public synchronized void endTalk() {
        //Task 2: Implementation of endTalk()
        //A philosopher is no longer talking. Notify one waiting philosopher
        //that they can start talking.
        aPhilosopherIsTalking = false;
        notify();
    }
}

// EOF
