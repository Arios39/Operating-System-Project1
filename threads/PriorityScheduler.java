package nachos.threads;


import nachos.machine.*;


import java.util.LinkedList;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the thread
 * that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has the
 * potential to starve a thread if there's always a thread waiting with higher
 * priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
	/**
	 * Allocate a new priority scheduler.
	 */
	public PriorityScheduler() {
	}

	/**
	 * Allocate a new priority thread queue.
	 *
	 * @param transferPriority <tt>true</tt> if this queue should transfer
	 * priority from waiting threads to the owning thread.
	 * @return a new priority thread queue.
	 */


	public ThreadQueue newThreadQueue(boolean transferPriority) {
		return new PriorityQueue(transferPriority);
	}

	public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getPriority();
	}

	public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());

		return getThreadState(thread).getEffectivePriority();
	}

	public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());

		Lib.assertTrue(priority >= priorityMinimum
				&& priority <= priorityMaximum);

		getThreadState(thread).setPriority(priority);
	}

	public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();
		boolean ret = true;

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMaximum)
			ret = false;
		else
			setPriority(thread, priority + 1);

		Machine.interrupt().restore(intStatus);
		return ret;
	}

	public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();
		boolean ret = true;

		KThread thread = KThread.currentThread();

		int priority = getPriority(thread);
		if (priority == priorityMinimum)
			ret = false;
		else
			setPriority(thread, priority - 1);

		Machine.interrupt().restore(intStatus);
		return ret;
	}

	/**
	 * The default priority for a new thread. Do not change this value.
	 */
	public static final int priorityDefault = 1;

	/**
	 * The minimum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMinimum = 0;

	/**
	 * The maximum priority that a thread can have. Do not change this value.
	 */
	public static final int priorityMaximum = 7;

	/**
	 * Return the scheduling state of the specified thread.
	 *
	 * @param thread the thread whose scheduling state to return.
	 * @return the scheduling state of the specified thread.
	 */
	protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
			thread.schedulingState = new ThreadState(thread);

		return (ThreadState) thread.schedulingState;
	}

	/**
	 * A <tt>ThreadQueue</tt> that sorts threads by priority.
	 */
	protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
			this.transferPriority = transferPriority;
		}

		public void waitForAccess(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).waitForAccess(this);
		}

		public void acquire(KThread thread) {
			Lib.assertTrue(Machine.interrupt().disabled());
			getThreadState(thread).acquire(this);
		}

		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			// implement me
			ThreadState ts = this.pickNextThread(); // get threadstate associated with the next thread
			if(ts != null) {
				waitQueue.remove(ts); //remove this threadstate from the waitQueue
				ts.queuesIOwn.remove(this); //remove this waitQueue from the Linked List
				ts.holding = null; //it is not the queueholder of any queue
				ts.queuesIOwn.add(this); //the thread is now waiting in this queue
				return ts.thread;
			}

			return null;

		}

		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return the next thread that <tt>nextThread()</tt> would return.
		 */
		protected ThreadState pickNextThread() {
			// implement me
			if(waitQueue.size() > 0) {
				return waitQueue.first(); //returns ThreadState associated with next thread in the queue
			}
			else {
				return null;
			}
		}

		public void print() {
			Lib.assertTrue(Machine.interrupt().disabled());
			//System.out.println("LinkedList:" + this.waitQueue.first().thread.getName());
		}
		public void add(ThreadState ts) { //adds thread to waitqueue
			Lib.assertTrue(Machine.interrupt().disabled());
			waitQueue.add(ts);
			ts.queuesIOwn.add(this);


		}

		public boolean isEmpty() {
			if(waitQueue.size() == 0) {
				return true;
			}
			else {
				return false;
			}
		}

		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
		public boolean transferPriority = true;
		public TreeSet<ThreadState> waitQueue = new TreeSet<ThreadState>();
		protected ThreadState queueholder = null;

	}

	/**
	 * The scheduling state of a thread. This should include the thread's
	 * priority, its effective priority, any objects it owns, and the queue it's
	 * waiting for, if any.
	 *
	 * @see nachos.threads.KThread#schedulingState
	 */
	protected class ThreadState implements Comparable<ThreadState>{
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param thread the thread this state belongs to.
		 */

		public int compareTo(ThreadState a) {//stores descending order(largest epriority to smallest)
			if(a.ePriority == this.ePriority) {
				if(a.waitTime > this.waitTime) {
					return 1;
				}
				else {
					return -1;
				}
			}
			else if(a.ePriority > this.ePriority) {
				return 1;
			}
			else if(a == null) {
				return 1;
			}
			else {
				return -1;
			}
		}


		public ThreadState(KThread thread) {
			this.thread = thread;

			setPriority(priorityDefault);
		}

		/**
		 * Return the priority of the associated thread.
		 *
		 * @return the priority of the associated thread.
		 */
		public int getPriority() {
			return priority;
		}

		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return the effective priority of the associated thread.
		 */
		public int getEffectivePriority() {
			// implement me
			return ePriority;
		}
		public void setEffectivePriority(int newPriority) {
			this.ePriority = newPriority;
		}
		public boolean isEmpty() {
			if(this == null) {
				return true;
			}
			return false;
		}
		public void UpdateEPriority() {
			int newPriority = this.getPriority(); //initialize are update (set) value as original priority
			if(this.queuesIOwn != null ) {
				//Iterator<PriorityQueue> itr = queuesIOwn.iterator();
				for(PriorityQueue queue: queuesIOwn) { //iterate through linked list of priority queues
					if(queue != null) {
						//PriorityQueue queue = (PriorityQueue)itr.next(); //choose the next priority queue
						ThreadState DonatingThread = queue.pickNextThread();//Within the queue, find the next thread
						if(DonatingThread != null && queue.transferPriority == true ) { // as long as such a thread exists,
							//System.out.println(this.thread.getName() + " DonatingThread is " + DonatingThread.thread.getName());
							if(DonatingThread.getEffectivePriority() > newPriority) {// check the effective priority of that thread versus our original priority
								newPriority = DonatingThread.getEffectivePriority();// if effective priority is greater, assign set value to it
							}
						}
					}
				}
			}
			if(newPriority < this.getPriority()) { // double check that our set value if changed, is greater than our initial priority
				newPriority = this.getPriority(); //if it is not , reassign the initial priority value for setting
			}
			this.setEffectivePriority(newPriority);// send set value to update function
			if(this.holding != null && this.holding.queueholder != this) { // check for existence of queue holder for this thread

				this.holding.queueholder.UpdateEPriority();	// call function for queue holder as their value needs to be updated since this value was updated
			}
		}

		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param priority the new priority.
		 */
		public void setPriority(int priority) {
			if (this.priority == priority)
				return;

			this.priority = priority;

			// implement me

			this.UpdateEPriority(); //must make sure to update effective priority

		}

		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the resource
		 * guarded by <tt>waitQueue</tt>. This method is only called if the
		 * associated thread cannot immediately obtain access.
		 *
		 * @param waitQueue the queue that the associated thread is now waiting
		 * on.
		 *
		 * @see nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
			// implement me
			//Machine.interrupt().disable();
			Lib.assertTrue(Machine.interrupt().disabled());
			waitTime = Machine.timer().getTime(); //record wait time for comparison
			waitQueue.add(this); //add this thread to the waitqueue
			this.holding = waitQueue; // the specific queue that the thread is a part of
			/*if thread has higher priority that queueholder, then it needs to donate priority*/
			this.UpdateEPriority();

		}

		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see nachos.threads.ThreadQueue#acquire
		 * @see nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			// implement me
			//Machine.interrupt().disable();

			if(waitQueue.isEmpty()) { //make sure queue is empty
				////System.out.println("--------- Queue is empty---------------------");
				waitQueue.queueholder = this; //set thread as queueholder
				//System.out.println("---------queue holder set---------------------");
				this.queuesIOwn.add(waitQueue);
				//	System.out.println("---------queue added to linked list---------------------");
				this.UpdateEPriority();
			}

			//	Machine.interrupt().enable();
		}

		/** The thread with which this object is associated. */
		protected KThread thread;

		/** The priority of the associated thread. */
		protected int priority;
		protected int ePriority;
		protected PriorityQueue holding; // the queue that contains the thread being utilized
		protected long waitTime; // for recording the wait time of each thread
		protected ArrayList<PriorityQueue> queuesIOwn = new ArrayList<PriorityQueue>();
		//queues that the thread being utilized is the queue holder of
	}

}