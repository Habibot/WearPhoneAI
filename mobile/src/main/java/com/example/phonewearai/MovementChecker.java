package com.example.phonewearai;

public class MovementChecker {

    private static final int STEPS_TO_MIN_MULTIPLIER = 12;

    public static int updateCadence(String strStepCounter, String strOldStepCounter){

        return calcToMin(calcDifference(strStepCounter, strOldStepCounter));
    }

    private static int calcToMin(int stepDifference){
        return stepDifference * STEPS_TO_MIN_MULTIPLIER;
    }

    private static int calcDifference(String strStepCounter, String strOldStepCounter){
        int stepCounter = Integer.parseInt(strStepCounter);
        int oldStepCounter = Integer.parseInt(strOldStepCounter);

        if (stepCounter == 0) {
            return 0;
        }
        return stepCounter - oldStepCounter;
    }
}
