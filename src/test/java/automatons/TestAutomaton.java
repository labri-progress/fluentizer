package automatons;

import com.squareup.javapoet.ClassName;
import lang.Expr;
import lang.Expr.Status;
import lang.Term;

import org.junit.jupiter.api.Test;

public class TestAutomaton {

    @Test
    void testAutomata() {
        AutomatonBuilder ab = new AutomatonBuilder(
                new Expr("n", new Term.Java("name", "String"), Status.ONE),
                new Expr("s", new Term.Java("status", "lang.Expr.Status"), Status.OPTIONAL),
                new Expr("e", new Term.Rule("expression", "lang.Expr"), Status.MANY)
        );
        ContextBuilder.compile(ClassName.get("automatons", "Example"), ab);
    }

}

