import java.util.HashSet;
//2
public class HashSetExample {
    public static void main(String[] args) {
        HashSet<String> strings = new HashSet<>();
        strings.add("apple");
        strings.add("banana");
        strings.add("apple"); // 重复项
        strings.add("orange");

        int sizeBefore = strings.size();
        strings.add("banana"); // 重复项
        int sizeAfter = strings.size();

        System.out.println("集合大小变化: " + sizeBefore + " -> " + sizeAfter);
        System.out.println("集合元素: " + strings);
    }
}