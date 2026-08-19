// A task with a name and completion state
class Task {
    private final String name;
    private boolean isDone;

    Task(String name) {
        this.name = name;
        this.isDone = false;
    }

    // Marks this task as completed and returns its updated display text
    String mark() {
        isDone = true;
        return display();
    }

    // Marks this task as incomplete and returns its updated display text
    String unmark() {
        isDone = false;
        return display();
    }

    // Returns the checkbox and name used when displaying this task
    String display() {
        String status = isDone ? "[X]" : "[ ]";
        return status + " " + name;
    }
}
