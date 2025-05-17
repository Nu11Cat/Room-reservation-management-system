import java.util.ArrayList;
//1
public class ArrayListExample {
    public static void main(String[] args) {
        ArrayList<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            numbers.add(i);
        }
        numbers.removeIf(n -> n % 3 == 0);
        System.out.println("剩余元素: " + numbers);
    }
}