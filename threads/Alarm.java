package nachos.threads;

import nachos.machine.*;

import java.util.PriorityQueue;
import java.util.TreeSet;
import java.util.Comparator;
/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */


public class Alarm {
	/**
	 * Allocate a new Alarm. Set the machine's timer interrupt handler to this
	 * alarm's callback.
	 *
	 * <p>
	 * <b>Note</b>: Nachos will not function correctly with more than one alarm.
	 */
	public KThread current; // Define a Kthread var for our Timer intterpt
	//WE set the Priority Queue as having an initial capactity of 1 with a instance of our comparator SortByTime
	private PriorityQueue<ThreadShleep> WakeQueue = new PriorityQueue<ThreadShleep>(1,new SortByTime()); //created a PriorityQueue beacsue we can assign each element a priortiy.

	// private TreeSet<ThreadShleep> WakeQueue = new TreeSet<ThreadShleep>(); //First though to make TreeSet but it would want to take in the corrent paramaters so switching to Priority Queue

	public Alarm() {
		Machine.timer().setInterruptHandler(new Runnable() {
			public void run() {
				timerInterrupt();
			}
		});
	}

	/**
	 * The timer interrupt handler. This is called by the machine's timer
	 * periodically (approximately every 500 clock ticks). Causes the current
	 * thread to yield, forcing a context switch if there is another thread that
	 * should be run.
	 */
	public void timerInterrupt() {
		//KThread.currentThread().yield();
		//KThread.currentThread().sleep();
		//Machine.interrupt().disable(); //we may or maynot need this
		long Time = Machine.timer().getTime(); // Created time variable for current time
		if (WakeQueue.size()!=0) { //Need to check if our Priority Queue is empty first or else it wont run
			int i;
			for(i=0;i<=WakeQueue.size();i++){ //we iterate through each element in the queue and compare the wakeTime to the current Time
				//KThread Alarm = KThread.currentThread();
				ThreadShleep Alarm =WakeQueue.peek(); //Take in the waketime and thread
				if(Alarm.wakeTime <= Time){ //If our alarm is less than or equal to the current time we wake up our thread
					ThreadShleep wakey = WakeQueue.poll(); //since we awake we gotta get our of the queue
					KThread Wakethread = wakey.current; //calling the current KThread
					Wakethread.ready(); //Tried using Condition 2 Wake() but i got some errors
					//	Wakethread.Condition2.wake();
				}
			}
		}

		current.yield(); //once we done waking up well can transfer priority to the next thread to be woken up

		//Machine.interrupt().enable(); //we may or may not need this
	}

	/**
	 * Put the current thread to sleep for at least <i>x</i> ticks, waking it up
	 * in the timer interrupt handler. The thread must be woken up (placed in
	 * the scheduler ready set) during the first timer interrupt where
	 *
	 * <p>
	 * <blockquote> (current time) >= (WaitUntil called time)+(x) </blockquote>
	 *
	 * @param x the minimum number of clock ticks to wait.
	 *
	 * @see nachos.machine.Timer#getTime()
	 */
	public void waitUntil(long x) {
		// for now, cheat just to get something working (busy waiting is bad)

		//while (wakeTime > Machine.timer().getTime())
		//KThread.yield();
		Machine.interrupt().disable();

		long wakeTime = Machine.timer().getTime() + x;

		KThread current = KThread.currentThread(); //we need to call the current thread so we can eventually add to our queue

		ThreadShleep WakeThread= new ThreadShleep(wakeTime,current); //we Make a object from our class that holds our Queue parameters

		WakeQueue.add(WakeThread); //Add the Thread and the Time to our Queue

		//System.out.println(WakeQueue.peek());
		if (WakeThread!=null){


			current.sleep(); //We relinquish KThreads as we wont need it here anymore also program runs into issues if we dont have this for some reason
		}


		Machine.interrupt().enable();



	}


	public static void alarmTest1() {
		int durations[] = {1000, 10*1000, 100*1000};
		long t0, t1;

		for (int d : durations) {
			t0 = Machine.timer().getTime();
			ThreadedKernel.alarm.waitUntil (d);
			t1 = Machine.timer().getTime();
			System.out.println ("alarmTest1: waited for " + (t1 - t0) + " ticks");
		}
	}

	// Implement more test methods here ...

	// Invoke Alarm.selfTest() from ThreadedKernel.selfTest()
	public static void selfTest() {
		alarmTest1();

		// Invoke your other test methods here ...
	}






	class ThreadShleep{ //make a struct/class to store the paramaters i.e the waking thread and specific time
		public long wakeTime; //Time we supposed to wake up
		public KThread current; //the thread that is to wake up

		public ThreadShleep(long wakeTime, KThread current){ //Created a Constructor initilizae our paramaters
			this.wakeTime = wakeTime;
			this.current = current;
		}
	}

	class SortByTime implements Comparator<ThreadShleep>{ // Created a Compararator Interface so we can properly compare and sort by the WakeTime
		public int compare(ThreadShleep a, ThreadShleep b){ // Kept getting the java.lang.Comparator error so this will fix that
			return(int)(a.wakeTime - b.wakeTime); //needed to return as int not long so added (int)
		}
	}


}
