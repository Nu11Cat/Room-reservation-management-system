package shape;

public class Main {
    public static void main(String[] args) {
        Circle circle = new Circle(5);
        System.out.println("圆的面积是：" + circle.getArea());
        circle.draw();
    }
}