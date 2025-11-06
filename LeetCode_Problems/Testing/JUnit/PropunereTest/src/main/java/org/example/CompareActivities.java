package org.example;


import static org.example.ActivityType.BIKE;

public class CompareActivities {
    private int cal;

    public CompareActivities(int cal) {
        this.cal = cal;
    }

    /**
     * The method checks the type of the activity and based on this type it calculates the difference between the attribute
     * of the class (cal) and the value returned by calculateCalories called on the corresponding activity.
     * @param  type  specifies the type of the activity (the possible types are provided in the ActivityType enum)
     * @param  v     variable parameter (may contain 0 to n elements and functions as an array) representing the values
     *               of the attributes for the respective activity
     * @return       the difference between the attribute of the class and the value returned by calculateCalories called on
     *               the corresponding activity
     */
    public int compare(ActivityType type, int... v) throws IllegalArgumentException {

        if (type == null)
            throw new IncorrectValueException("Activity type can't be NULL");

        if (v.length == 0) {
            throw new IncorrectNumberOfParameters("'Compare' method received too less args ");
        }

        return switch (type) {
            case BIKE -> {

                if (v.length < 2)
                    throw new IncorrectNumberOfParameters("Insufficient params");

                Bike bike = new Bike(v[0], v[1]);
                yield cal - bike.calculateCalories();
            }
            case RUN -> {
                Run run = new Run(v[0]);
                yield cal - run.calculateCalories();
            }
            case SWIM -> {

                if (v.length < 2)
                    throw new IncorrectNumberOfParameters("Insufficient params");

                Swim swim = new Swim(v[0], v[1]);
                yield cal - swim.calculateCalories();
            }
            default -> 0;
        };
    }
}
