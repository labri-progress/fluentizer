package automatons;

import lang.Expr;

import java.util.*;

public class AutomatonBuilder {

    final Expr[] exprs;
    final Set<Expr> mandatories;

    public AutomatonBuilder(Expr... exprs) {
        this.exprs = exprs;

        Set<String> names = new HashSet<>();
        for (Expr expr : exprs) {
            if (!names.add(expr.name))
                throw new RuntimeException("Expression should have unique terms");
        }
        Set<Expr> mandatories = new HashSet<>();
        for (Expr expr : exprs) {
            if (expr.status.isMandatory())
                mandatories.add(expr);
        }
        this.mandatories = Collections.unmodifiableSet(mandatories);
    }

    public Recognizer compile() {
        Recognizer auto = new Recognizer();
        Map<Set<Expr>, Recognizer.State> states = new HashMap<>();
        Deque<Recognizer.State> newStates = new ArrayDeque<>();

        newStates.addLast(auto.addInitialState(mandatories.isEmpty()));

        while (!newStates.isEmpty()) {
            Recognizer.State state = newStates.pollFirst();
            for (Expr expr : exprs) {
                if (!state.terms.contains(expr)) {
                    Set<Expr> targetExprs = new HashSet<>(state.terms);
                    targetExprs.add(expr);
                    Recognizer.State target = states.get(targetExprs);
                    if (target == null) {
                        if (targetExprs.containsAll(mandatories))
                            target = auto.addFinalState(targetExprs);
                        else
                            target = auto.addState(targetExprs);
                        states.put(targetExprs, target);
                        newStates.add(target);
                    }
                    auto.addTransition(state, expr.term, target);
                } else if (expr.status.isRepetition()) {
                    auto.addTransition(state, expr.term, state);
                }
            }
        }

        return auto;
    }
}
