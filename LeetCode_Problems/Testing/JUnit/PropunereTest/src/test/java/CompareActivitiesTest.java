import org.example.ActivityType;
import org.example.CompareActivities;
import org.example.IncorrectNumberOfParameters;
import org.example.IncorrectValueException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CompareActivitiesTest {

    private static CompareActivities comparer;

    @BeforeAll
    static void createInstance() {
        comparer = new CompareActivities(100);
    }

    @Test
    void givenArrayIsEmpty_whenCompare_thenThrowIncorrectNumberOfParameters() {
        assertThrows(IncorrectNumberOfParameters.class, () -> comparer.compare(ActivityType.BIKE));
    }

    @Test
    void givenActivityTypeIsNull_whenCompare_thenThrowIncorrectValueException() {
        assertThrows(IncorrectValueException.class, () -> comparer.compare(null, 10, 3));
    }

    @Test
    void givenActivityBikeAndNotEnoughParams_whenCompare_thenThrowIncorrectNumberOfParameters() {
        assertThrows(IncorrectNumberOfParameters.class, () -> comparer.compare(ActivityType.BIKE, 10));
    }

    @Test
    void givenActivityBikeAndParameters_whenCompare_thenReturnValue() {
        var calories = comparer.compare(ActivityType.BIKE, 10, 2);

        assertEquals(-100, calories);
    }

    @Test
    void givenActivityRunAndParameter_whenCompare_thenReturnValue() {
        var calories = comparer.compare(ActivityType.RUN, 10);

        assertEquals(-900, calories);
    }

    @Test
    void givenActivitySwimAndNotEnoughParams_whenCompare_thenThrowIncorrectNumberOfParameters() {
        assertThrows(IncorrectNumberOfParameters.class, () -> comparer.compare(ActivityType.SWIM, 10));
    }

    @Test
    void givenActivitySwimAndParameters_whenCompare_thenReturnValue() {
        var calories = comparer.compare(ActivityType.SWIM, 10, 2);

        assertEquals(-398, calories);
    }

    @Test
    void givenUnknownActivityAndNotParameter_whenCompare_thenThrowIncorrectNumberOfParameters() {
        assertThrows(IncorrectNumberOfParameters.class, () -> comparer.compare(ActivityType.UNKNOWNACTIVITY));
    }

    @Test
    void givenUnknownActivityAndParameter_whenCompare_thenReturnDefaultValue() {
        var calories = comparer.compare(ActivityType.UNKNOWNACTIVITY, 2);

        assertEquals(0, calories);
    }


}
