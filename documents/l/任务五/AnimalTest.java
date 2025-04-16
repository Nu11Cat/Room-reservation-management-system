public class AnimalTest {
    public static void main(String[] args) {
        // 创建动物数组
        Animal[] animals = new Animal[2];
        animals[0] = new Dog("狗", 2, "金毛");
        animals[1] = new Cat("猫", 1, "橘");

        // 测试多态性 - makeSound()
        System.out.println("=== 测试动物叫声 ===");
        for (Animal animal : animals) {
            animal.makeSound();
        }

        // 测试 Swimmable 接口
        System.out.println("\n=== 测试游泳能力 ===");
        for (Animal animal : animals) {
            if (animal instanceof Swimmable) {
                ((Swimmable) animal).swim();
            }
        }

        // 测试 Trainable 接口
        System.out.println("\n=== 测试训练能力 ===");
        for (Animal animal : animals) {
            if (animal instanceof Trainable) {
                ((Trainable) animal).train();
            }
        }
    }
}