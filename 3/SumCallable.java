import java.util.concurrent.Callable;

public class SumCallable implements Callable<Integer> {//3
    private final int number;

    public SumCallable(int number) {
        this.number = number;
    }

    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 1; i <= number; i++) {
            sum += i;
        }
        return sum;
    }

    public static void main(String[] args) throws Exception {
        SumCallable sumCallable = new SumCallable(10);
        java.util.concurrent.FutureTask<Integer> futureTask = new java.util.concurrent.FutureTask<>(sumCallable);
        Thread thread = new Thread(futureTask);
        thread.start();
        System.out.println("Sum: " + futureTask.get());
    }
}