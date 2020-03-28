package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
	/**
	 * Allocate a new communicator.
	 */
	Lock lock=new Lock();;
Condition2 speakers = new Condition2(lock);
Condition2 listeners = new Condition2(lock);
	Semaphore done = new Semaphore(0);
private int spoken;
private static int infinity = Integer.MAX_VALUE;
public Communicator() {


		spoken = infinity;

	}

	/**
	 * Wait for a thread to listen through this communicator, and then transfer
	 * <i>word</i> to the listener.
	 * 
	 * <p>
	 * Does not return until this thread is paired up with a listening thread.
	 * Exactly one listener should receive <i>word</i>.
	 * 
	 * @param word the integer to transfer.
	 */
	public void speak(int word) {
lock.acquire();
while (spoken!=infinity){
	speakers.sleep();
	//adding speakers to sleep Q if someone has spoken
}

spoken = word;
listeners.wake();// wake up listener
speakers.sleep();//sleeps speaker that spoke
lock.release();//release lock
	}

	/**
	 * Wait for a thread to speak through this communicator, and then return the
	 * <i>word</i> that thread passed to <tt>speak()</tt>.
	 * 
	 * @return the integer transferred.
	 */
	public int listen() {
int msg;// buffer holder for our incoming message
		lock.acquire();
		while (spoken==infinity){
			//puts listeners to sleep if nothing is being "said"
			listeners.sleep();
		}
		//will copy the message recived to return it and will set spoken to a poison value to ensure other listeners dont hear it
		msg = spoken;
		//System.out.println("heard "+spoken);
		spoken = infinity;
		speakers.wakeAll();
	//wake up all speakers so they can speak again
		lock.release();
//return message that was received
		return msg;
	}
	public static void selfTest() {
		final Communicator t = new Communicator();

		KThread testthread = new KThread(new Runnable() {
			public void run() {

				t.listen();
			}

		});
		testthread.fork();
		KThread testthread2 = new KThread(new Runnable() {
			public void run() {
				t.speak(150);
			}

		});
		testthread2.fork();
		testthread.join();
		testthread2.join();

	}






}
