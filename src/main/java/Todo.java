// Represents a task that has no associated date or time.
class Todo extends Task {
    Todo(String description) {
        super(description);
    }

    @Override
    String display() {
        return "[T]" + super.display();
    }
}
