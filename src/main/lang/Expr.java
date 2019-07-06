package lang;

public class Expr {
    final public String name;
    final public Term term;
    final public Status status;

    public Expr(String name, Term term, Status status) {
        this.name = name;
        this.term = term;
        this.status = status;
    }

    public Expr(Term term, Status status) {
        this.name = term.name;
        this.term = term;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Expr<" + name + ">";
    }

    public enum Status {
        ONE("1") {
            @Override
            public boolean isMandatory() {
                return super.isMandatory();
            }

            @Override
            public boolean isRepetition() {
                return super.isRepetition();
            }
        },
        MANY("+") {
            @Override
            public boolean isMandatory() {
                return true;
            }

            @Override
            public boolean isRepetition() {
                return true;
            }
        },
        OPTIONAL("?"),
        MANY_OPTIONAL("*") {
            @Override
            public boolean isRepetition() {
                return true;
            }
        };

        final String text;

        Status(String c) {
            text = c;
        }

        public boolean isMandatory() {
            return false;
        }

        public boolean isRepetition() {
            return false;
        }
    }

    public boolean isMandatory() {
        return status.isMandatory();
    }

    public boolean isRepetition() {
        return status.isRepetition();
    }

    public boolean isRule() {
        return term.isRule();
    }
}
