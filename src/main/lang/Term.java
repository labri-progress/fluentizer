package lang;

public abstract class Term {
    final String name;
    final String type;

    public Term(String type) {
        this(type.toLowerCase(), type);
    }

    public Term(String name, String type) {
        this.name = name;
        this.type = type;
    }

    abstract String toJavaType();

    boolean isEOI() {
        return accept(new RuleVisitor<>() {
            public Boolean visit(Term other, Void unused) {
                return false;
            }

            public Boolean visit(EOI eoi, Void unused) {
                return true;
            }
        });
    }
    boolean isRule() {
        return accept(new RuleVisitor<>() {
            public Boolean visit(Term other, Void unused) {
                return false;
            }

            public Boolean visit(Rule rule, Void unused) {
                return true;
            }
        });
    }

    @Override
    public String toString() {
        return "" + name + ":" + type;
    }

    abstract <R, P> R accept(RuleVisitor<R, P> visitor, P param);

    final <R> R accept(RuleVisitor<R, Void> visitor) {
        return accept(visitor, visitor.defaultParamValue());
    }

    interface RuleVisitor<R, P> {
        default R visit(Term term, P param) {
            return defaultReturnValue();
        }

        default P defaultParamValue() {
            return null;
        }
        default R defaultReturnValue() {
            return null;
        }

        default R visit(Rule rule, P param) {
            return visit((Term) rule, param);
        }
        default R visit(Java terminal, P param) {
            return visit((Term) terminal, param);
        }
        default R visit(EOI eoi, P param) {
            return visit((Term) eoi, param);
        }
    }

    public static class Java extends Term {
        public Java(String type) {
            this(type.toLowerCase(), type);
        }

        public Java(String name, String type) {
            super(name, type);
        }

        <R, P> R accept(RuleVisitor<R, P> visitor, P param) {
            return visitor.visit(this, param);
        }

        @Override
        String toJavaType() {
            return type;
        }
    }

    public static class Rule extends Term {
        public Rule(String type) {
            this(type.toLowerCase(), type);
        }

        public Rule(String name, String type) {
            super(name, type);
        }

        <R, P> R accept(RuleVisitor<R, P> visitor, P param) {
            return visitor.visit(this, param);
        }

        @Override
        String toJavaType() {
            return type + ".Context<? extends " + type + ">";
        }
    }

    public static class EOI extends Term {
        public EOI() {
            super("EOI");
        }

        <R, P> R accept(RuleVisitor<R, P> visitor, P param) {
            return visitor.visit(this, param);
        }

        @Override
        String toJavaType() {
            return null;
        }
    }
}
