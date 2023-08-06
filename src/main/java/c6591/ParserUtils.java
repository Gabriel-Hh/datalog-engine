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
        Map<String, List<Tuple<Predicate,Integer>>> variableToPredicates = new HashMap<>();

        for (int i = 0; i < predicates.size(); i++) {
            Predicate predicate = predicates.get(i);
            for (int j = 0; j < predicate.terms.size(); j++) {
                Term term = predicate.terms.get(j);
                    variableToPredicates
                        .computeIfAbsent( term.name, k -> new ArrayList<>())
                        .add(new Tuple<Predicate,Integer>(predicate, j+1)); // +1 because SQL is 1-indexed
                }
        }

        List<JoinCondition> joinConditions = new ArrayList<>();
        for ( String variableName : variableToPredicates.keySet()) {
            if (variableToPredicates.get(variableName).size() > 1) {
                
                joinConditions.add(new JoinCondition(variableName, variableToPredicates.get(variableName)));
            }
        }

        return joinConditions;
    }

}
