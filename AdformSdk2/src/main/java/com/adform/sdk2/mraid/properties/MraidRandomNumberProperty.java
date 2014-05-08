package com.adform.sdk2.mraid.properties;

import java.util.Random;

/**
 * Created by mariusm on 08/05/14.
 */
public class MraidRandomNumberProperty extends MraidBaseProperty {
    private final int mRandomNumber;

    MraidRandomNumberProperty(int randomNumber) {
        mRandomNumber = randomNumber;
    }

    public static MraidRandomNumberProperty createWithRandomNumber() {
        Random r = new Random();
        int randomNumber = r.nextInt();
        return new MraidRandomNumberProperty(
                (randomNumber < 0)?(randomNumber*-1):randomNumber);
    }


    @Override
    public String toGetPair() {
        return "rnd="+ mRandomNumber;
    }
}
