package c6591;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ASTClasses {
    class Program {
    List<Rule> rules;
    List<Fact> facts;
}

abstract class Statement { }

class Rule extends Statement {
    Head head;
    Body body;
    Map<String, Integer> bodyPredicateCounts = new HashMap<>();
}

class Fact extends Statement {
    Predicate predicate;
}

class Head {
    Predicate predicate;
}

class Body {
    List<Predicate> predicates;
    List<JoinCondition> joinConditions;
}

class JoinCondition {
    String variableName;
    int variableIndex;
    List<Predicate> predicates;
}

class Predicate {
    String name;
    List<Term> terms;
    String alias;
}

abstract class Term { }

class Variable extends Term {
    String name;
    String source;
    int index;
}

class Constant extends Term {
    String value;
}

}
