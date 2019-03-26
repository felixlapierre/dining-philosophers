
/**
 * Class DiningPhilosophers
 * The main starter.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class DiningPhilosophers {

    /*
	 * ------------
	 * Data members
	 * ------------
     */

    /**
     * This default may be overridden from the command line
     */
    public static final int DEFAULT_NUMBER_OF_PHILOSOPHERS = 4;

    /**
     * Dining "iterations" per philosopher thread while they are socializing
     * there
     */
    public static final int DINING_STEPS = 10;

    /**
     * Our shared monitor for the philosphers to consult
     */
    public static Monitor soMonitor = null;

    /*
	 * -------
	 * Methods
	 * -------
     */
    /**
     * Main system starts up right here
     */
    public static void main(String[] argv) {
        try {
            int iPhilosophers = getNumberOfPhilosophersFromCommandLine(argv);

            // Make the monitor aware of how many philosophers there are
            soMonitor = new Monitor(iPhilosophers);

            // Space for all the philosophers
            Philosopher aoPhilosophers[] = new Philosopher[iPhilosophers];

            // Let 'em sit down
            for (int j = 0; j < iPhilosophers; j++) {
                aoPhilosophers[j] = new Philosopher();
                aoPhilosophers[j].start();
            }

            System.out.println(
                    iPhilosophers
                    + " philosopher(s) came in for a dinner."
            );

            // Main waits for all its children to die...
            // I mean, philosophers to finish their dinner.
            for (int j = 0; j < iPhilosophers; j++) {
                aoPhilosophers[j].join();
            }

            System.out.println("All philosophers have left. System terminates normally.");
        } catch (InterruptedException e) {
            System.err.println("main():");
            reportException(e);
            System.exit(1);
        }
    } // main()
    
    //Task 4
    //TODO: Print an informative error message when unable to parse to int
    private static int getNumberOfPhilosophersFromCommandLine(String[] argv)
    {
        int result = DEFAULT_NUMBER_OF_PHILOSOPHERS;
        if(argv.length > 0)
        {
            try
            {
                result = Integer.parseInt(argv[0]);
                if(result < 1)
                    throw new NumberFormatException();
            }
            catch (NumberFormatException e)
            {
                System.out.println("\"" + argv[0] + "\" is not a positive decimal integer.\n\nUsage: java DiningPhilosophers [NUMBER_OF_PHILOSOPHERS]");
                System.exit(1);
            }
        }
        return result;
    }

    /**
     * Outputs exception information to STDERR
     *
     * @param poException Exception object to dump to STDERR
     */
    public static void reportException(Exception poException) {
        System.err.println("Caught exception : " + poException.getClass().getName());
        System.err.println("Message          : " + poException.getMessage());
        System.err.println("Stack Trace      : ");
        poException.printStackTrace(System.err);
    }
}

// EOF
