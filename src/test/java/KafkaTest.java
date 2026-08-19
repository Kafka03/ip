import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

//Tests Kafka java file
class KafkaTest {

    @Test
    void replacesLowercaseAndUppercaseL() {
        assertEquals("Hewwo Wiwy uwu~", Kafka.makeUwu("Hello Lily"));
    }

    @Test
    void leavesOtherCharactersUnchanged() {
        assertEquals("cat uwu~", Kafka.makeUwu("cat"));
    }

    @Test
    void handlesEmptyInput() {
        assertEquals(" uwu~", Kafka.makeUwu(""));
    }
}
