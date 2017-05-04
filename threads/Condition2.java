/** Project: 1
 *  Task 2
 *  Description: (30%) Implement condition variables directly, by using
 *  interrupt enable and disable to provide atomicity. We have provided
 *  a sample implementation that uses semaphores; your job is to provide
 *  an equivalent implementation without directly using semaphores. Once 
 *  you are done, you will have two alternative implementations that provide
 *  the exact same functionality. Your second implementation of condition 
 *  variables must reside in class nachos.threads.Condition2.
 */ 
package nachos.threads;

import nachos.machine.*;
import java.util.LinkedList;

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
   public Condition2(Lock conditionLock) {
      this.conditionLock = conditionLock;
      // list of threads waiting in line (mimacing semiphore)
      this.threadWaitQueue = new LinkedList<KThread>();


   }

   /**
    * Atomically release the associated lock and go to sleep on this condition
    * variable until another thread wakes it using <tt>wake()</tt>. The current
    * thread must hold the associated lock. The thread will automatically
    * reacquire the lock before <tt>sleep()</tt> returns.
    */
   public void sleep() {
      Lib.assertTrue(conditionLock.isHeldByCurrentThread());
      // add thread to back of queue, then put to sleep
      this.threadWaitQueue.addLast(KThread.currentThread());

      conditionLock.release();
      boolean interruptStatus = Machine.interrupt().disable();
      KThread.currentThread().sleep();

      Machine.interrupt().restore(interruptStatus);   
      conditionLock.acquire();
   }

   /**
    * Wake up at most one thread sleeping on this condition variable. The
    * current thread must hold the associated lock.
    */
   public void wake() {
      Lib.assertTrue(conditionLock.isHeldByCurrentThread());
      // retrieve the next thread in line
      boolean interruptStatus = Machine.interrupt().disable();
      if(!this.threadWaitQueue.isEmpty())
      {
         this.threadWaitQueue.removeFirst().ready();
      }
      Machine.interrupt().restore(interruptStatus);   
   }

   /**
    * Wake up all threads sleeping on this condition variable. The current
    * thread must hold the associated lock.
    */
   public void wakeAll() {
      Lib.assertTrue(conditionLock.isHeldByCurrentThread());

      boolean interruptStatus = Machine.interrupt().disable();
      // go through and retrieve all threads in waitline
      while (!this.threadWaitQueue.isEmpty())
      {
         wake();
      }
      Machine.interrupt().restore(interruptStatus);   
   }

   /**
    *  Creates a test for Task 2 Project one, testing our implementation
    *  of condition variables and interupts without the use of semiphores
    *  (compared to Condition.java)
    */
   public static void selfTest()
   {
      System.out.println("Testing Condition2");
      Lock lock = new Lock();
      lock.acquire();
      Condition2 c2 = new Condition2(lock);  
      c2.sleep();
      c2.wakeAll();
      System.out.println("Done testing Condition2");
   }

   private Lock conditionLock;
   private LinkedList<KThread> threadWaitQueue;
}
