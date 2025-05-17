public class PrintNumbersThread extends Thread {//1
    @Override
    public void run() {
        for (int i = 1; i <= 10; i++) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        PrintNumbersThread thread = new PrintNumbersThread();
        thread.start();
    }
}