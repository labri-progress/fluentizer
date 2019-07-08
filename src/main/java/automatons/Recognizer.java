package automatons;

import lang.Expr;
import lang.Term;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

class Recognizer extends Automaton<Recognizer.State, Recognizer.Transition> {
    public State addInitialState(boolean isfinal) {
        State state = new State(new HashSet<>()) {
            {
                this.isFinal = isfinal;
            }
        };
        addState(state);
        initial = state;
        return state;
    }

    public State addState(Set<Expr> terms) {
        return addState(new State(terms) {
        });
    }

    public State addFinalState(Set<Expr> terms) {
        return addState(new FinalState(terms));
    }

    public void addTransition(State source, Term name, State target) {
        addTransition(new Transition(source, name, target));
    }

    protected abstract class State extends Automaton<State, Transition>.State {
        Set<Expr> terms;

        State(Set<Expr> terms) {
            this.terms = Collections.unmodifiableSet(terms);
        }

        @Override
        public String toString() {
            StringJoiner sj = new StringJoiner(", ", "<", ">");
            terms.forEach(t -> sj.add(t.name));
            return sj.toString();
        }
    }

    protected class FinalState extends State {
        FinalState(Set<Expr> terms) {
            super(terms);
            isFinal = true;
        }
    }

    protected class Transition extends Automaton<State, Transition>.Transition {
        Term term;

        public Transition(State source, Term term, State target) {
            this.term = term;
            this.source = source;
            this.target = target;
        }

        @Override
        public String toString() {
            return term.toString();
        }
    }
}
