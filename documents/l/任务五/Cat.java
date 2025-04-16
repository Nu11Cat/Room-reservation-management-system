public class Cat extends Animal implements Trainable {
    private String furColor;

    public Cat(String name, int age, String furColor) {
        super(name, age);
        this.furColor = furColor;
    }

    public String getFurColor() {
        return furColor;
    }

    @Override
    public void makeSound() {
        System.out.println(getName() + "（" + furColor + "色）：喵喵喵！");
    }

    @Override
    public void train() {
        System.out.println(getName() + "训练...");
    }
}