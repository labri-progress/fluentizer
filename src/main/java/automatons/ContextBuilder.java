package automatons;

import com.squareup.javapoet.*;
import automatons.Recognizer.State;
import automatons.Recognizer.Transition;
import lang.Expr;

import javax.lang.model.element.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class ContextBuilder {
    private final ClassName grammarName;
    private final ClassName contextName;

    private final Recognizer recognizer;
    private final Expr[] exprs;

    private Map<State, String> stateNames;
    private Map<State, List<Transition>> transitionsFrom;

    private static final ClassName COLLECTION = ClassName.get("java.util", "ArrayList");
    public static final ClassName FUNCTION = ClassName.get("java.util.function", "Function");

    public ContextBuilder(ClassName name, Recognizer recognizer, Expr[] exprs) {
        this.grammarName = name;
        this.contextName = ClassName.get(grammarName.packageName(), grammarName.simpleName(), "Context");
        this.recognizer = recognizer;
        this.exprs = exprs;

        stateNames = new HashMap<>();
        transitionsFrom = new HashMap<>();
    }

    public static void compile(ClassName name, AutomatonBuilder ab) {
        Recognizer r = ab.compile();
        r.toDot(System.err);
        System.err.flush();

        ContextBuilder cb = new ContextBuilder(name, r, ab.exprs);
        System.out.println(cb.build());
    }

    public JavaFile build() {
        nameStates();

        for (Recognizer.Transition transition : recognizer.transitions) {
            List<Recognizer.Transition> transitions = transitionsFrom.get(transition.source);
            if (transitions == null) {
                transitions = new ArrayList<>();
                transitionsFrom.put(transition.source, transitions);
            }
            transitions.add(transition);
        }

        var states = makeStates();
        var grammar = makeGrammar(states, makeContext(states));
        return JavaFile.builder(grammarName.packageName(), grammar).build();
    }

    private void nameStates() {
        int id = 0;
        stateNames.put(recognizer.initial, Integer.toString(id++));
        for (Recognizer.State state : recognizer.states) {
            if (!stateNames.containsKey(state))
                stateNames.put(state, Integer.toString(id++));
        }
    }

    private ClassName stateName(State state) {
        return ClassName.get(
                grammarName.packageName(), grammarName.simpleName(),
                "State$" + stateNames.get(state));
    }

    public Collection<TypeSpec> makeStates() {
        var states = new ArrayList<TypeSpec>(recognizer.states.size());
        for (var state: recognizer.states) {
            var iface = TypeSpec.interfaceBuilder(stateName(state));
            if (state.isFinal)
                iface.addMethod(MethodSpec.methodBuilder("$").
                        addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC).
                        returns(grammarName).build());
            for (var to: transitionsFrom.get(state))
                iface.addMethod(MethodSpec.methodBuilder(to.term.name)
                        .addModifiers(Modifier.ABSTRACT, Modifier.PUBLIC)
                        .returns(stateName(to.target))
                        .addParameter(ClassName.get("", to.term.type), to.term.name)
                        .build());
            states.add(iface.build());
        }
        return states;
    }

    private TypeSpec makeGrammar(Collection<TypeSpec> states, TypeSpec ctx) {
        ParameterizedTypeName function = ParameterizedTypeName.get(
                FUNCTION, contextName, grammarName);
        return TypeSpec.classBuilder(grammarName)
                .addMethod(MethodSpec.methodBuilder("start")
                        .returns(stateName(recognizer.initial))
                        .addParameter(function, "builder")
                        .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                        .addStatement("return new $T(builder)", contextName)
                        .build())
                .addType(ctx)
                .addTypes(states)
                .build();
    }

    public TypeSpec makeContext(Collection<TypeSpec> interfaces) {
        var ctx = TypeSpec.classBuilder(contextName)
                .addModifiers(Modifier.STATIC)
                .addSuperinterfaces(interfaces.stream()
                        .map(i -> ClassName.get(grammarName.packageName(), grammarName.simpleName(), i.name))
                        .collect(Collectors.toList()));
        for (var expr : exprs)
            if (expr.status.isRepetition())
                ctx.addField(
                        FieldSpec.builder(ParameterizedTypeName.get(COLLECTION, ClassName.get("", expr.term.type)),
                                expr.term.name,
                                Modifier.PUBLIC)
                            .initializer("new $T<>()", COLLECTION)
                            .build());
            else
                ctx.addField(
                        FieldSpec.builder(
                                ClassName.get("", expr.term.type),
                                expr.term.name,
                                Modifier.PRIVATE).build());
        ParameterizedTypeName function = ParameterizedTypeName.get(
                FUNCTION, contextName, grammarName);
        ctx.addField(function, "builder", Modifier.PRIVATE);
        ctx.addMethod(MethodSpec.constructorBuilder()
                .addParameter(function, "builder")
                .addStatement("this.builder = builder")
                .build());
        ctx.addMethod(MethodSpec.methodBuilder("$")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(grammarName)
                .addStatement("return builder.apply(this)")
                .build());
        for (var expr : exprs)
            ctx.addMethod(MethodSpec.methodBuilder(expr.term.name)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(contextName)
                    .addParameter(ClassName.get("", expr.term.type), expr.term.name)
                    .addStatement(
                            (expr.status.isRepetition())
                                    ? "this." + expr.term.name + ".add(" + expr.term.name + ")"
                                    : "this." + expr.term.name + " = " + expr.term.name)
                    .addStatement("return this")
                    .build());
        return ctx.build();
    }

    private Collection<TypeSpec> makeInterfaces(String baseName) {
        Collection<TypeSpec> interfaces = new ArrayList<>();
        for (Recognizer.State state : recognizer.states) {
            TypeSpec.interfaceBuilder(baseName + "$" + stateNames.get(state))
                    .build();
        }
        return interfaces;
    }
}
