/** Project: 1
 *  Task 3
 *  Description: (20%) Complete the implementation of the Alarm class, by
 *  implementing the waitUntil(long x) method. A thread calls waitUntil
 *  to suspend its own execution until time has advanced to at least 
 *  now + x. This is useful for threads that operate in real-time, for 
 *  example, for blinking the cursor once per second. There is no 
 *  requirement that threads start running immediately after waking up; 
 *  just put them on the ready queue in the timer interrupt handler after
 *  they have waited for at least the right amount of time. Do not fork any
 *  additional threads to implement waitUntil(); you need only modify
 *  waitUntil() and the timer interrupt handler. waitUntil is not limited
 *  to one thread; any number of threads may call it and be suspended at any
 *  one time. Note however that only one instance of Alarm may exist at a time 
 *  (due to a limitation of Nachos).
 */ 
package nachos.threads;

import nachos.machine.*;

import java.util.LinkedList;
import java.util.Iterator;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {

   private class ThreadTimePair
   {
      // keeps track of current thread and current time
      public KThread thread;
      public long time;

      public ThreadTimePair(KThread thread, long time)
      {
         this.thread = thread;
         this.time = time;
      }
   }


   private LinkedList<ThreadTimePair> threadList;


   /**
    * Allocate a new Alarm. Set the machine's timer interrupt handler to this
    * alarm's callback.
    * 
    * <p>
    * <b>Note</b>: Nachos will not function correctly with more than one alarm.
    */
   public Alarm() {
      // the threads that alarm is setting a time for
      this.threadList = new LinkedList<ThreadTimePair>();
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
      // timer will interupt once we get x away from current Machine time
      long currentTime = Machine.timer().getTime();
      // checks if have passed alarm time for all the threads
      //  if so remove it and move onto next thread
      for(Iterator<ThreadTimePair> it = this.threadList.iterator(); it.hasNext();)
      {  
         ThreadTimePair pair = it.next();
         if (currentTime > pair.time)
         {
            it.remove();
            pair.thread.ready();
         }
      }
      KThread.currentThread().yield();
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
      long wakeTime = Machine.timer().getTime() + x;
      ThreadTimePair pair = new ThreadTimePair(KThread.currentThread(), wakeTime);
      this.threadList.addLast(pair);

      boolean interruptStatus = Machine.interrupt().disable();
      KThread.currentThread().sleep();


      Machine.interrupt().restore(interruptStatus);
   }
}
