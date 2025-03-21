package dev.vexor.radium;

import java.util.ArrayList;
import java.util.List;

public class Hooks {
    public static final List<Runnable> CLIENT_TICK = new ArrayList<>();
    public static final List<Runnable> WORLD_TICK = new ArrayList<>();
}
