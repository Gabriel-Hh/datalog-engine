package c6591;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import c6591.ASTClasses.*;

public class SQLGenerator {

    private Map<String, String> tables = new HashMap<>();
    private Map<String, List<String>> rules = new HashMap<>();
    private Map<String, List<String>> facts = new HashMap<>();

    public SQLGenerator(Program program) {
        generateSQLForFacts(program.facts);
        generateSQLForRules(program.rules);
    }

    private void generateSQLForFacts(List<Fact> factsList) {
        for (Fact fact : factsList) {
            String predicateName = fact.predicate.name;
            String insertStatement = generateInsertStatement(fact);

            if (!tables.containsKey(predicateName)) {
                String createTableStatement = generateCreateTableStatement(fact.predicate);
                tables.put(predicateName, createTableStatement);
            }

            facts.computeIfAbsent(predicateName, k -> new ArrayList<>()).add(insertStatement);
        }
    }

    private void generateSQLForRules(List<Rule> rulesList) {
        for (Rule rule : rulesList) {
            String predicateName = rule.head.predicate.name;
            List<String> ruleStatements = new ArrayList<>();

            if (!tables.containsKey(predicateName)) {
                String createTableStatement = generateCreateTableStatement(rule.head.predicate);
                tables.put(predicateName, createTableStatement);
            }

            String ruleQueryStatement = generateRuleQueryStatement(rule);
            ruleStatements.add(ruleQueryStatement);

            rules.put(predicateName, ruleStatements);
        }
    }

    private String generateInsertStatement(Fact fact) {
        String predicateName = fact.predicate.name;
        String values = fact.predicate.terms.stream()
            .map(term -> term instanceof Variable ? "?" : "'" + ((Constant) term).value + "'")
            .collect(Collectors.joining(", "));
        return "INSERT INTO " + predicateName + " VALUES (" + values + ");";
    }

    private String generateCreateTableStatement(Predicate predicate) {
        String predicateName = predicate.name;
        String columns = IntStream.range(0, predicate.terms.size())
            .mapToObj(i -> "a" + (i + 1) + " VARCHAR(255)")
            .collect(Collectors.joining(", "));
        return "CREATE TABLE " + predicateName + " (" + columns + ");";
    }

    private String generateRuleQueryStatement(Rule rule) {
        String head = rule.head.predicate.name;

        String select = rule.head.predicate.terms.stream()
            .map(term -> ((Variable) term).source + "." + "a" + (((Variable) term).index + 1))
            .collect(Collectors.joining(", "));

        String from = rule.body.predicates.stream()
            .map(p -> p.name + " AS " + p.alias)
            .collect(Collectors.joining(", "));

        String where = rule.body.joinConditions.stream()
            .map(jc -> jc.predicates.stream()
                .map(p -> p.alias + "." + "a" + (jc.variableIndex + 1))
                .collect(Collectors.joining(" = ")))
            .collect(Collectors.joining(" AND "));

        return "INSERT INTO " + head + " SELECT " + select + " FROM " + from + " WHERE " + where + ";";
    }

    public Map<String, String> getTables() {
        return tables;
    }

    public Map<String, List<String>> getRules() {
        return rules;
    }

    public Map<String, List<String>> getFacts() {
        return facts;
    }

    public void printAll() {
        System.out.println("Tables:");
        tables.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("Rules:");
        rules.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("Facts:");
        facts.forEach((k, v) -> System.out.println(k + ": " + v));
    }
}

