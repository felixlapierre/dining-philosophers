
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
    boolean aPhilosopherIsTalking = false;

    /**
     * Constructor
     */
    public Monitor(int piNumberOfPhilosophers) {
        // TODO: set appropriate number of chopsticks based on the # of philosophers
    }

    /*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
     */
    /**
     * Grants request (returns) to eat when both chopsticks/forks are available.
     * Else forces the philosopher to wait()
     */
    public synchronized void pickUp(final int piTID) {
        // ...
    }

    /**
     * When a given philosopher's done eating, they put the chopstiks/forks down
     * and let others know they are available.
     */
    public synchronized void putDown(final int piTID) {
        // ...
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
