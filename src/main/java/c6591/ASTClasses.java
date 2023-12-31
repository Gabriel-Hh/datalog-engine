package c6591;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTClasses {
    
    public static class Program {
    public List<Rule> rules;
    public List<Fact> facts;

    public Program() {
    }   

        public Program(List<Rule> rules, List<Fact> facts) {
            this.rules = rules;
            this.facts = facts;
        }
    }

    public abstract static class Statement { }

    public static class Rule extends Statement {
        public Head head;
        public Body body;
        public Map<String, Integer> bodyPredicateCounts = new HashMap<>();

        public Rule() {
        }

        public Rule(Head head, Body body) {
            this.head = head;
            this.body = body;
        }
    }

    public static class Fact extends Statement {
        public Predicate predicate;

        public Fact(Predicate predicate) {
            this.predicate = predicate;
        }
    }

    public static class Head {
        public Predicate predicate;

        public Head(Predicate predicate) {
            this.predicate = predicate;
        }
    }

    public static class Body {
        public List<Predicate> predicates;
        public List<JoinCondition> joinConditions;

        public Body(List<Predicate> predicates){
            this.predicates = predicates;
        } // For facts (no join conditions
        

        public Body(List<Predicate> predicates, List<JoinCondition> joinConditions) {
            this.predicates = predicates;
            this.joinConditions = joinConditions;
        }
    }

    public static class JoinCondition {
        public String variableName;
        public List<Tuple<Predicate,Integer>> tupleList;
        public Tuple<Predicate,Integer> constantTuple;

        public JoinCondition(String variableName, List<Tuple<Predicate,Integer>> tupleList) {
            this.variableName = variableName;
            this.tupleList = tupleList;
            this.constantTuple = null;
        }

        public JoinCondition(String variableName, Tuple<Predicate,Integer> constantTuple) {
            this.variableName = variableName;
            this.tupleList = null;
            this.constantTuple = constantTuple;
        }
    }

    public static class Predicate {
        public String name;
        public List<Term> terms;
        public String alias;

        public Predicate(String name, List<Term> terms, String alias) {
            this.name = name;
            this.terms = terms;
            this.alias = alias;
        }
    }

    public static class Term {
        public String name;
        public String source;
        public int index;

        public Term(String name, String source, int index) {
            this.name = name;
            this.source = source;
            this.index = index;
        }
    }

    public static class Variable extends Term {

        public Variable(String name, String source, int index) {
            super(name, source, index);
        }
    }

    public static class Constant extends Term {

        public Constant(String value) {
            super(value, null, 0);
        }

        public Constant(String value, String source, int index) {
            super(value, source, index);
        }
    }

    //Cause Java doesn't have tuples
    public static class Tuple<A, B> {
        public A first;
        public B second;

        public Tuple(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
    
    public static class Triple<A, B, C> {
        public A first;
        public B second;
        public C third;

        public Triple(A first, B second, C third) {
            this.first = first;
            this.second = second;
            this.third = third;
        }
    }

}
