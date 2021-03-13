package main.util;

import java.util.Random;

public class Randomizer {
    public static int getRandomInt(int min, int max){
        return new Random().nextInt(max-min+1)+min;
    }
}
