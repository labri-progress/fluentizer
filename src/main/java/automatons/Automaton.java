package automatons;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

public class Automaton<S extends Automaton<S, T>.State, T extends Automaton<S, T>.Transition> {
    S initial;

    Set<S> states = new HashSet<>();
    Set<T> transitions = new HashSet<>();

    protected S addState(S state) {
        states.add(state);
        return state;
    }

    protected T addTransition(T transition) {
        transitions.add(transition);
        return transition;
    }

     class State {
        boolean isFinal;
    }

    protected class Transition {
        protected S source;
        S target;
    }

    public void toDot(PrintStream out) {
        out.println("digraph G {");
        out.println("\tgraph [splines=false];");
        for (S state : states) {
            out.println("\t\"" + state.hashCode() + "\" ["
                    + "label=\"" + state.toString() + "\","
                    + (state.isFinal ? "style=dashed," : "")
                    + (state.equals(initial) ? "shape=box," : "")
                    + "] ;");
        }
        for (T transition : transitions) {
            out.println("\t\""
                    + transition.source.hashCode() + "\" ->  \""
                    +  transition.target.hashCode()
                    + "\" [label=\"" + transition.toString() +"\"] ;");
        }
        out.println("};");
    }
}
