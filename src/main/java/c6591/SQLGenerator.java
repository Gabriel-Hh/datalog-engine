package c6591;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import c6591.ASTClasses.*;

public class SQLGenerator {

    private static HashMap<String, String> tables = new HashMap<>();
    private static HashMap<String, List<String>> rules = new HashMap<>();
    private static HashMap<String, List<String>> facts = new HashMap<>();

    public static Triple<HashMap<String,String>,HashMap<String,List<String>>,HashMap<String,List<String>>> generateSQL(Program program) {
        generateSQLForFacts(program.facts);
        generateSQLForRules(program.rules);
        System.out.println("Sql statements generated successfully.");
        return new Triple<>(tables, facts, rules);
    }

    private static void generateSQLForFacts(List<Fact> factsList) {
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

    private static void generateSQLForRules(List<Rule> rulesList) {
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

    private static String generateInsertStatement(Fact fact) {
        String predicateName = fact.predicate.name;
        String values = fact.predicate.terms.stream()
            .map(term -> term instanceof Variable ? "?" : "'" + ((Constant) term).name + "'")
            .collect(Collectors.joining(", "));
        return "INSERT INTO " + predicateName + " VALUES (" + values + ")";
    }

    private static String generateCreateTableStatement(Predicate predicate) {
        String predicateName = predicate.name;
        String columns = IntStream.range(0, predicate.terms.size())
            .mapToObj(i -> "a" + (i + 1) + " VARCHAR(255)")
            .collect(Collectors.joining(", "));
        
        // Generating the primary key clause, this enforces our table are sets (NO DUPLICATES)
        String primaryKeyColumns = IntStream.range(0, predicate.terms.size())
            .mapToObj(i -> "a" + (i + 1))
            .collect(Collectors.joining(", "));
        String primaryKey = "PRIMARY KEY (" + primaryKeyColumns + ")";

        return "CREATE TABLE " + predicateName + " (" + columns + ", " + primaryKey + ")";
    }

    private static String generateRuleQueryStatement(Rule rule) {
        String head = rule.head.predicate.name;

        String select = rule.head.predicate.terms.stream()
            .map(term -> ((term instanceof Variable) ? term.source + "." + "a" + term.index : term.source))
            .collect(Collectors.joining(", "));

        String from = rule.body.predicates.stream()
            .map(p -> p.name + " AS " + p.alias)
            .collect(Collectors.joining(", "));

            String where = rule.body.joinConditions.stream()
            .map(jc -> {
                if (jc.constantTuple != null) {
                    return jc.constantTuple.first.alias + ".a" + jc.constantTuple.second + " = '" + jc.variableName + "'";
                }
                else {
                    List<String> conditions = new ArrayList<>();
                    for (int i = 0; i < jc.tupleList.size() - 1; i++) {
                        Tuple<Predicate, Integer> current = jc.tupleList.get(i);
                        Tuple<Predicate, Integer> next = jc.tupleList.get(i + 1);
                        conditions.add(current.first.alias + ".a" + current.second + " = " + next.first.alias + ".a" + next.second);
                    }
                    return String.join(" AND ", conditions);
                }
            })
            .collect(Collectors.joining(" AND "));
    
        return "INSERT INTO " + head + " SELECT " + select + " FROM " + from + (where.isEmpty() ? "" : " WHERE " + where);
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

    public static void printAll() {
        System.out.println("------------------------------------------------------");
        System.out.println("Tables:");
        System.out.println("------------------------------------------------------");
        tables.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("------------------------------------------------------");
        System.out.println("Rules:");
        System.out.println("------------------------------------------------------");
        rules.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("------------------------------------------------------");
        System.out.println("Facts:");
        System.out.println("------------------------------------------------------");
        facts.forEach((k, v) -> System.out.println(k + ": " + v));
    }
}

