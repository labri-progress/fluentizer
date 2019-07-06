package automatons;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import automatons.Recognizer.State;
import automatons.Recognizer.Transition;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class ContextBuilder {
    public static final int INDENT = 4;
    private final Recognizer recognizer;
    private Map<State, String> stateNames;
    private Map<State, List<Transition>> transitionsFrom;
    private final PrintStream out;
    private int $ = 0;

    public ContextBuilder(Recognizer recognizer, PrintStream out) {
        this.recognizer = recognizer;
        this.out = out;
        stateNames = new HashMap<>();
        transitionsFrom = new HashMap<>();
    }

    public static void compile(String name, AutomatonBuilder ab) {
        Recognizer r = ab.compile();
        r.toDot(System.err);
        System.err.flush();
        ContextBuilder cb = new ContextBuilder(r, System.out);
        cb.build(name);
    }

    private void $(String... strs) {
        if (strs.length == 0) {
            out.println();
            return;
        }
        char[] indent = new char[$ * INDENT];
        StringBuilder sb = new StringBuilder();

        Arrays.fill(indent, ' ');
        sb.append(indent);

        for (String str : strs)
            sb.append(str);

        out.println(sb);
    }

    public void build(String name) {
        nameStates();
        for (Recognizer.Transition transition : recognizer.transitions) {
            List<Recognizer.Transition> transitions = transitionsFrom.get(transition.source);
            if (transitions == null) {
                transitions = new ArrayList<>();
                transitionsFrom.put(transition.source, transitions);
            }
            transitions.add(transition);
        }
        var ctx = makeContext(name, makeInterfaces("Context"));
        System.out.println(ctx);
    }

    private void nameStates() {
        int id = 0;
        stateNames.put(recognizer.initial, Integer.toString(id++));
        for (Recognizer.State state : recognizer.states) {
            if (!stateNames.containsKey(state))
                stateNames.put(state, Integer.toString(id++));
        }
    }

    public TypeSpec makeContext(String name, Collection<TypeSpec> interfaces) {
        ClassName recognizerName = ClassName.get("", name);
        ClassName context = ClassName.get("", "Context");

        return TypeSpec.classBuilder(recognizerName)
                .addSuperinterface(ParameterizedTypeName.get(context, recognizerName))
                .addSuperinterfaces(interfaces.stream().map(i -> ClassName.get("", i.name)).collect(Collectors.toList()))
                .addTypes(interfaces)
                .build();
    }

    private Collection<TypeSpec> makeInterfaces(String baseName) {
        Collection<TypeSpec> interfaces = new ArrayList<>();
        for (Recognizer.State state : recognizer.states) {
            TypeSpec.interfaceBuilder(baseName +"$" + stateNames.get(state))
                    .build();

        }
        return interfaces;
    }

    public void dumpElementClass(String name) {
        // term States
        $("public class ", name, " implements Element.Context<" +name + "> {");
        $++;

        $("@Override");
        $("public Production build() {");
        $++;
        $("return this;");
        $--;
        $("}");
        $();

        for (Recognizer.State state : recognizer.states) {
            $("public interface Context$", stateNames.get(state),
                    (state.isFinal ? " extends " + name + ".Context<" + name + ">" : "")," {");
            $++;
            if (transitionsFrom.containsKey(state))
                for (Recognizer.Transition transition : transitionsFrom.get(state)) {
                    $(name, ".Context$", stateNames.get(transition.target), " ",
                            transition.term.name,
                            "(", transition.term.toJavaType(), " ", transition.term.name,");");
                }
            $--;
            $("}");
        }

        $--;
        $("}");
    }
}
