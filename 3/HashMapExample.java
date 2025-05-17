import java.util.HashMap;
//3
public class HashMapExample {
    public static void main(String[] args) {
        HashMap<String, Integer> studentGrades = new HashMap<>();
        studentGrades.put("Alice", 85);
        studentGrades.put("Bob", 78);
        studentGrades.put("Charlie", 92);

        String studentName = "Bob";
        if (studentGrades.containsKey(studentName)) {
            int newGrade = studentGrades.get(studentName) + 10;
            studentGrades.put(studentName, newGrade);
        }

        System.out.println("更新后的成绩: " + studentGrades);
    }
}