package nachos.threads;

import nachos.ag.BoatGrader;
enum Location {
    Oahu,
    Molokai
}
class lilboat{
static int passangers;
    Location location;
    boolean intransit;
}
class THreads extends KThread{
    Location mylocation;

}

public class Boat {
    static BoatGrader bg;
    static Lock lock = new Lock();
    static Condition2 onMolokai = new Condition2(lock);
    //will help put threads in a sleeping q while waiting for boat
    static Condition2 onOahu = new Condition2(lock);
    static Condition2 AonOahu = new Condition2(lock);
    static Condition2 AonMolokai = new Condition2(lock);
    static Communicator coms = new Communicator();
    //coms will help with debuging and letting us know when the program is over
    static lilboat boat = new lilboat();
    // boat objevct will help with the position and passangers that can get on the boat
    static int cOahu;
    static int AOahu;
    static int total;
    static int cMolakai=0;
    //these ints help keep track of how many children & adults are on Oahu
    public static void selfTest() {
        BoatGrader b = new BoatGrader();

       // System.out.println("\n ***Testing Boats with only 2 children***");
        //begin(0, 2, b);

        // System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
        // begin(1, 2, b);

         System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
        begin(3, 3, b);
    }

    public static void begin(int adults, int children, BoatGrader b) {
        // Store the externally generated autograder in a class
        // variable to be accessible by children.
        bg = b;
boat.location=Location.Oahu;
//sets boats default location to oahu
boat.passangers=0;
//sets defaults for boat
total=0;
cOahu=children;
AOahu=adults;
//will help keep track of how many left on Oahu
        // Instantiate global variables here
//
        // Create threads here. See section 3.4 of the Nachos for Java
        // Walkthrough linked from the projects page.

        Runnable rchild = new Runnable() {
            public void run() {

                ChildItinerary();
            }
        };
        //creates runnable child thread
        for(int i =0; i<children;i++) {
            KThread t = new KThread(rchild);
            t.setName("Child Thread # "+i);
            t.fork();
        }
        Runnable rAdult = new Runnable() {
            public void run() {

                AdultItinerary();
            }
        };
        //creates adult runnable threads
        for(int i =0; i<adults;i++) {
            KThread t = new KThread(rAdult);
            t.setName("Adult Thread # "+i);
            t.fork();
        }

        while(true) {
            int msg = coms.listen();
            //will help with the transmition of how many people are on molakai
if(msg!=99) {
    System.out.println(msg+ " People on Molokai");
}
            if (msg == 99)
            {
                //this if loop will break the loop and means that everyone is in molokai
                break;
            }
        }



    }

    static void AdultItinerary() {
        /*
         * This is where you should put your solutions. Make calls to the
         * BoatGrader to show that it is synchronized. For example:
         * 		bg.AdultRowToMolokai();
         * indicates that an adult has rowed the boat across to Molokai
         */
        Location mylocation = Location.Oahu;
        // give every thread a location, will help keeping track of whos where
        lock.acquire();
        while(true){

            while(boat.location!=Location.Oahu&&mylocation==Location.Oahu){
               // onMolokai.wake();
                AonOahu.sleep();
                //if the boat isnt here go to sleep
            }

            if(boat.location==Location.Oahu&&mylocation==Location.Oahu){
               // System.out.println("looped "+ KThread.currentThread().getName());

                if(boat.passangers==0) {
                    AOahu--;
                    total++;
                    boat.passangers++;
                    bg.AdultRowToMolokai();
                    coms.speak(total);
                    //everytime someone crosses to Molokai we will annouynce how many ppl are there now

                    mylocation = Location.Molokai;
                    boat.location = Location.Molokai;
                    //set boats location to molokai and wake up any children threads sleeping
                    boat.passangers--;
                    //we have to wake up any children so they can row back and pick up any child left on oahu
                    onMolokai.wake();
                }
             if(AOahu==0&&cOahu==0){
                 coms.speak(99);
//breaks us out of the loop and announces to the program that its done
                 break;
             }
             AonMolokai.sleep();
            }



        }
        lock.release();


    }

    static void ChildItinerary() {
        lock.acquire();

Location mylocation = Location.Oahu;
//sets default location for each thread
        while(true){
while(boat.location!=Location.Oahu&& mylocation==Location.Oahu){
    //sleep on oahu and wake up anyone on the molokai island
    onMolokai.wake();
    onOahu.sleep();
}
            if(boat.location==Location.Oahu&& mylocation==Location.Oahu){
                //if statment that will fill up the boat with passangers if the boat is in oahu and threads are also there
                if(cOahu>=1){
                    if(boat.passangers==0){
                        //loads up the child piolt
                        cOahu--;
                        onOahu.wake();
                        boat.passangers++;
                        cMolakai++;
                        bg.ChildRowToMolokai();
                        mylocation=Location.Molokai;
                        onMolokai.sleep();

                    }
                    if(boat.passangers==1){
                        //loads up the child passanger
                        cOahu--;
                        boat.passangers++;
                        cMolakai++;
                        bg.ChildRideToMolokai();
                        boat.location=Location.Molokai;
                        mylocation=Location.Molokai;
                        boat.passangers=boat.passangers-2;
                        total=total+2;
                        coms.speak(total);
                        if(cOahu==0&&AOahu==0){
                            //chacks if we are done getting everyone
                            coms.speak(99);

                            System.out.println("kill program");
                            break;
                        }
                        if(cOahu==0&&AOahu>=0){
                            //will wake up any child if there are still adults on oahu and then go to sleep
                           onMolokai.wake();
                        }

                        onMolokai.sleep();

                    }

                }
            }
            if(boat.location!=Location.Oahu&& mylocation!=Location.Oahu){
//when a thread is woken up and they are on molokai, they will check if there are any children left on oahu and will get them first
                if(cOahu>=1){

                    if(boat.passangers==0) {
                        cMolakai--;
                        cOahu++;
                        total--;
                        bg.ChildRowToOahu();
                        mylocation = Location.Oahu;
                        boat.location=Location.Oahu;
                        onOahu.wake();
                        onOahu.sleep();

                    }

                }
                //if there are no children but there are adults it will piolt the boat with one child and will go to sleep on oahu while the adult rows with the boat to molokai
                if(cOahu==0&&AOahu>=1){

                    if(boat.passangers==0) {
                      //  System.out.println("looped "+ KThread.currentThread().getName());

                        cMolakai--;
                        cOahu++;
                        total--;
                        bg.ChildRowToOahu();
                        mylocation = Location.Oahu;
                        boat.location=Location.Oahu;
                        AonOahu.wake();
                        onOahu.sleep();

                    }

                }

            }


            if(cOahu==0){
                break;
            }

        }



        lock.release();


    }

    static void SampleItinerary() {
        // Please note that this isn't a valid solution (you can't fit
        // all of them on the boat). Please also note that you may not
        // have a single thread calculate a solution and then just play
        // it back at the autograder -- you will be caught.
        System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
        bg.AdultRowToMolokai();
        bg.ChildRideToMolokai();
        bg.AdultRideToMolokai();
        bg.ChildRideToMolokai();
    }

}