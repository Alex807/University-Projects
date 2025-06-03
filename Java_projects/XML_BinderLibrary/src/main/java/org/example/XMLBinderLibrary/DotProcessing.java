package org.example.XMLBinderLibrary;

import org.example.outputclasses.dots.Dot;
import org.example.outputclasses.dots.Dots;

import java.util.Random;

public class DotProcessing {

    public static void printState(Dots dots) {
        System.out.println("\n\t Dots class");
        for (Dot dot : dots.dot) {
            System.out.println("Dot: x=" + dot.x + ", y=" + dot.y);
        }
    }

    public static void modifyState(Dots dots) {
        Random random = new Random();
        for (Dot dot : dots.dot) {
            dot.x = random.nextInt(100);
            dot.y = random.nextInt(100);
        }
    }
}
