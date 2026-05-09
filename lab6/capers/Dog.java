package capers;

import java.io.File;
import java.io.Serializable;
import static capers.Utils.*;

/** Represents a dog that can be serialized.
 * @author D
*/
public class Dog implements Serializable {

    /** Folder that dogs live in. */
    static final File DOG_FOLDER = Utils.join(".capers", "dogs");

    /** Age of dog. */
    private int age;
    /** Breed of dog. */
    private String breed;
    /** Name of dog. */
    private String name;

    /**
     * Creates a dog object with the specified parameters.
     * @param name Name of dog
     * @param breed Breed of dog
     * @param age Age of dog
     */
    public Dog(String name, String breed, int age) {
        this.age = age;
        this.breed = breed;
        this.name = name;
    }

    /**
     * Reads in and deserializes a dog from a file with name NAME in DOG_FOLDER.
     *
     * @param name Name of dog to load
     * @return Dog read from file
     */
    public static Dog fromFile(String name) {
        File dog_file = Utils.join(".capers", "dogs", name);
        if (!dog_file.exists()) {
            return null;
        }

        return (Dog) Utils.readObject(dog_file, Dog.class);
    }

    /**
     * Increases a dog's age and celebrates!
     */
    public void haveBirthday() {
        File dog_file = Utils.join(".capers", "dogs", this.name);
        if (!dog_file.exists()) {
            return;
        }
        age += 1;
        Utils.writeObject(dog_file, this);
    }

    /**
     * Saves a dog to a file for future use.
     */
    public void saveDog() {
        File dog_file = Utils.join(".capers", "dogs", this.name);
        if (!dog_file.exists()) {
            Utils.writeContents(dog_file, "");
        }

        Utils.writeObject(dog_file, this);
    }

    @Override
    public String toString() {
        return String.format(
            "Woof! My name is %s and I am a %s! I am %d years old! Woof!",
            name, breed, age);
    }

}
