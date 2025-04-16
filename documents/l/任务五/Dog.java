public class Dog extends Animal implements Trainable, Swimmable {
    private String breed;

    public Dog(String name, int age, String breed) {
        super(name, age);
        this.breed = breed;
    }

    public String getBreed() {
        return breed;
    }

    @Override
    public void makeSound() {
        System.out.println(getName() + "（" + breed + "）：汪汪");
    }

    @Override
    public void train() {
        System.out.println(getName() + "训练！");
    }

    @Override
    public void swim() {
        System.out.println(getName() + "游泳！");
    }
}