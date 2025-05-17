import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ThreadExample {
    public static void main(String[] args) throws Exception {
        MyThread myThread = new MyThread();
        myThread.start();


        MyRunnable myRunnable = new MyRunnable();
        Thread runnableThread = new Thread(myRunnable);
        runnableThread.start();

        MyCallable myCallable = new MyCallable();
        FutureTask<String> futureTask = new FutureTask<>(myCallable);
        Thread callableThread = new Thread(futureTask);
        callableThread.start();
        System.out.println(futureTask.get());
    }
}

class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " 正在执行任务");
    }
}

class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + " 正在执行任务");
    }
}

class MyCallable implements Callable<String> {
    @Override
    public String call() throws Exception {
        return Thread.currentThread().getName() + " 执行任务完成";
    }
}