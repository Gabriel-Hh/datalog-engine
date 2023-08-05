package c6591;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;

import c6591.ASTClasses.*;


public class ParserUtils {
    boolean checkSafety(Rule rule) {
    Set<String> bodyVariables = new HashSet<>();
    for (Predicate predicate : rule.body.predicates) {
        for (Term term : predicate.terms) {
            if (term instanceof Variable) {
                bodyVariables.add(((Variable) term).name);
            }
        }
    }
    for (Term term : rule.head.predicate.terms) {
        if (term instanceof Variable && !bodyVariables.contains(((Variable) term).name)) {
            return false; // Variable in head not found in body
        }
    }
    return true;
}

List<JoinCondition> identifyJoinConditions(List<Predicate> predicates) {
    Map<String, List<Predicate>> variableToPredicates = new HashMap<>();

    for (Predicate predicate : predicates) {
        for (int i = 0; i < predicate.terms.size(); i++) {
            Term term = predicate.terms.get(i);
            if (term instanceof Variable) {
                variableToPredicates
                    .computeIfAbsent(((Variable) term).name, k -> new ArrayList<>())
                    .add(predicate);
            }
        }
    }

    List<JoinCondition> joinConditions = new ArrayList<>();
    for (Map.Entry<String, List<Predicate>> entry : variableToPredicates.entrySet()) {
        if (entry.getValue().size() > 1) {
            JoinCondition joinCondition = new JoinCondition();
            joinCondition.variableName = entry.getKey();
            joinCondition.predicates = entry.getValue();
            joinConditions.add(joinCondition);
        }
    }

    return joinConditions;
}

}
