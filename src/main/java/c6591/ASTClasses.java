package c6591;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTClasses {
    
    public static class Program {
    public List<Rule> rules;
    public List<Fact> facts;
}

    public abstract static class Statement { }

    public static class Rule extends Statement {
        public Head head;
        public Body body;
        public Map<String, Integer> bodyPredicateCounts = new HashMap<>();
    }

    public static class Fact extends Statement {
        public Predicate predicate;
    }

    public static class Head {
        public Predicate predicate;
    }

    public static class Body {
        public List<Predicate> predicates;
        public List<JoinCondition> joinConditions;
    }

    public static class JoinCondition {
        public String variableName;
        public int variableIndex;
        public List<Predicate> predicates;
    }

    public static class Predicate {
        public String name;
        public List<Term> terms;
        public String alias;
    }

    public static abstract class Term { }

    public static class Variable extends Term {
        public String name;
        public String source;
        public int index;
    }

    public static class Constant extends Term {
        public String value;
    }

}
