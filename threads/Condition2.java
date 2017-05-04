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
      while (!this.threadWaitQueue.isEmpty())
      {
         wake();
      }
      Machine.interrupt().restore(interruptStatus);   
   }


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
