public class PrintLettersRunnable implements Runnable {//2
    @Override
    public void run() {
        for (char c = 'A'; c <= 'J'; c++) {
            System.out.println(c);
        }
    }

    public static void main(String[] args) {
        Thread thread = new Thread(new PrintLettersRunnable());
        thread.start();
    }
}