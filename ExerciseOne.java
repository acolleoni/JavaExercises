import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/*
    Exercise One: Create a class containing a static list of its instances.
    This list must be automatically updated in case of garbage collection of an instance.

    Implementation (Java 8): Creating a parallel thread that takes care of updating the instance
    list in case of garbage collection.
 */

class ExampleClass {

    // List of weak references to all instances of the class
    private static final List<WeakReference<ExampleClass>> instances = new ArrayList<>();

    // Queue of references to class instances that have been garbage collected
    private static final ReferenceQueue<ExampleClass> referenceQueue = new ReferenceQueue<>();

    private String name;
    private String objectUUID;

    public static ExampleClass createInstance(String name) {
        ExampleClass instance = new ExampleClass();
        instance.name = name;
        instance.objectUUID = UUID.randomUUID().toString();
        instances.add(new WeakReference<>(instance, referenceQueue));
        return instance;
    }

    public static List<WeakReference<ExampleClass>> getInstances() {
        return instances;
    }

    public String getName() {
        return name;
    }

    public String getObjectUUID() {
        return objectUUID;
    }

    private ExampleClass() {
    }

    private static void cleanupReferences() {
        WeakReference<?> ref;
        while ((ref = (WeakReference<?>) referenceQueue.poll()) != null) {
            System.out.println("Cleaning " + ref);
            instances.remove(ref);
        }
    }

    static {
        Thread cleanupThread = new Thread(() -> {
            while (!Thread.interrupted()) {
                cleanupReferences();
                try {
                    Thread.sleep(1000); // Cleaning time interval
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        cleanupThread.setDaemon(true);
        cleanupThread.start();
    }

}

public class ExerciseOne {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("I create four instances, three assigned to a local variable, one unassigned.");
        ExampleClass instance1 = ExampleClass.createInstance("One");
        ExampleClass instance2 = ExampleClass.createInstance("Two");
        ExampleClass instance3 = ExampleClass.createInstance("Three");
        ExampleClass.createInstance("Four");

        System.out.println("\n"+ExampleClass.getInstances().size() + " instances:");

        ExampleClass.getInstances().stream()
                .map(x -> "- " + Objects.requireNonNull(x.get()).getName() + ": " + Objects.requireNonNull(x.get()).getObjectUUID())
                .forEach(System.out::println);

        System.out.println("\nI set one of the assigned instances to null.");
        instance2 = null;
        System.out.println("\nNow two of them are eligible for garbage collection, so I explicitly invoke GC.");
        System.gc();
        System.out.println("I wait 2 seconds to let the cleaning thread do its work.\n");
        Thread.sleep(2000);

        System.out.println("\nNow the saved instances are:");
        ExampleClass.getInstances().stream()
                .map(x -> "- " + Objects.requireNonNull(x.get()).getName() + ": " + Objects.requireNonNull(x.get()).getObjectUUID())
                .forEach(System.out::println);
    }
}
