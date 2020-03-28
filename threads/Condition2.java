package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 *
 * <p>
 * You must implement this.
 *
 * @see nachos.threads.Condition
 */
public class Condition2 {
	/**
	 * Allocate a new condition variable.
	 *
	 * @param conditionLock the lock associated with this condition variable.
	 * The current thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 * <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	private LinkedList<KThread> SleepyList = new LinkedList<KThread>();//created a link list so we can dynamically add and remove our sleeping threads with ease
	//not sure why threaded Queue nor my Queue/linked list didn't want to work
	//private Queue<KThread> SleepyList = newLinkedBlockingDeque<KThread>();
	private KThread SleepingThread; //Value to store our current Thread
	private KThread Wakeup;
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock; //possible to keep track of threads with a thread count with this
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		Machine.interrupt().disable();//we can disable interripts here then shift thread to wait and then sleep after restore the machine
		conditionLock.release(); //since we going to sleep we can release our lock
		//ThreadedQueue sleepyQueue = ThreadedKernel.scheduler.newThreadQueue(true);
		KThread SleepingThread = KThread.currentThread();
		SleepyList.add(SleepingThread);//add the current thread from Kthread to the queue thanks to our linked List
		SleepingThread.sleep(); //sleep currentn thread

		conditionLock.acquire(); //since current thread is sleeping will reaquire the lock
		Machine.interrupt().enable(); //restore interrupts
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());


		/*Machine.interrupt().disable();

		Machine.interrupt().enable();*/

		//if (!SleepyList.isEmpty()) { //We can check is the SleepyList List has any threads
		if(SleepyList.size()!=0){ //Need to make sure our list has elements inside
			Machine.interrupt().disable();
			KThread Wakeup = SleepyList.removeFirst();//Initialze a var for our thread that is gonna wake
			//Well remove the first thread from the list with removeFirst instead and make sure we actually have a thread in the list.
			//System.out.println(Wakeup.getName());

			Wakeup.ready(); //Ready will move the thread to a ready queue effectively "Wakeing up the thread
		}
		Machine.interrupt().enable(); //We enable again
	}
	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */



	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		//Basiclly the same as wake() except we run the wake function on every single element of the LinkedList
		int i;
		int size = SleepyList.size(); //we just iterate through our linked list and wake all the threads up
		for(i=0; i <=size;i++){
			wake();
		}
	}

	/**
	 * Atomically release the associated lock and go to sleep on
	 * this condition variable until either (1) another thread
	 * wakes it using <tt>wake()</tt>, or (2) the specified
	 * <i>timeout</i> elapses.  The current thread must hold the
	 * associated lock.  The thread will automatically reacquire
	 * the lock before <tt>sleep()</tt> returns.
	 */
	public void sleepFor(long timeout) {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	}

/*public static void cvTest5() {
        final Lock lock = new Lock();
        // final Condition empty = new Condition(lock);
        final Condition2 empty = new Condition2(lock);
        final LinkedList<Integer> list = new LinkedList<>();

        KThread consumer = new KThread( new Runnable () {
                public void run() {
                    lock.acquire();
                    while(list.isEmpty()){
                        empty.sleep();
                    }
                    Lib.assertTrue(list.size() == 5, "List should have 5 values.");
                    while(!list.isEmpty()) {
                        // context swith for the fun of it
                        KThread.currentThread().yield();
                        System.out.println("Removed " + list.removeFirst());
                    }
                    lock.release();
                }
            });

        KThread producer = new KThread( new Runnable () {
                public void run() {
                    lock.acquire();
                    for (int i = 0; i < 5; i++) {
                        list.add(i);
                        System.out.println("Added " + i);
                        // context swith for the fun of it
                        KThread.currentThread().yield();
                    }
                    empty.wake();
                    lock.release();
                }
            });

        consumer.setName("Consumer");
        producer.setName("Producer");
        consumer.fork();
        producer.fork();

        // We need to wait for the consumer and producer to finish,
        // and the proper way to do so is to join on them.  For this
        // to work, join must be implemented.  If you have not
        // implemented join yet, then comment out the calls to join
        // and instead uncomment the loop with yield; the loop has the
        // same effect, but is a kludgy way to do it.
        consumer.join();
        producer.join();
        //for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
    }*/

	// Place Condition2 testing code in the Condition2 class.

	// Example of the "interlock" pattern where two threads strictly
	// alternate their execution with each other using a condition
	// variable.

	private static class InterlockTest {
		private static Lock lock;
		private static Condition2 cv;

		private static class Interlocker implements Runnable {
			public void run () {
				lock.acquire();
				for (int i = 0; i < 10; i++) {
					System.out.println(KThread.currentThread().getName());
					cv.wake();   // signal
					cv.sleep();  // wait

				}
				lock.release();
			}
		}

		public InterlockTest () {
			lock = new Lock();
			cv = new Condition2(lock);

			KThread ping = new KThread(new Interlocker());
			ping.setName("ping");
			KThread pong = new KThread(new Interlocker());
			pong.setName("pong");

			ping.fork();
			pong.fork();

			// We need to wait for ping to finish, and the proper way
			// to do so is to join on ping.  (Note that, when ping is
			// done, pong is sleeping on the condition variable; if we
			// were also to join on pong, we would block forever.)
			// For this to work, join must be implemented.  If you
			// have not implemented join yet, then comment out the
			// call to join and instead uncomment the loop with
			// yields; the loop has the same effect, but is a kludgy
			// way to do it.
			ping.join();
			// for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
		}
	}

	// Invoke Condition2.selfTest() from ThreadedKernel.selfTest()

	public static void selfTest() {
		new InterlockTest();
	}


	private Lock conditionLock;

}
