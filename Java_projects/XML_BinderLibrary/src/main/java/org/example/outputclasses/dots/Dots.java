package org.example.outputclasses.dots;

import java.util.ArrayList;
import java.util.List;

public class Dots {
    public List<Dot> dot = new ArrayList<Dot>();

    public List<Dot> getDot() {
        if (dot == null) {
            dot = new ArrayList<>();
        }
        return dot;
    }

    public void addDot(Dot x) {
        if (dot == null) {
            dot = new ArrayList<>();
        }
        dot.add(x);
    }
}
