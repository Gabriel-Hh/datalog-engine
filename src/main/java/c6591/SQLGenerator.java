package c6591;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import c6591.ASTClasses.*;

public class SQLGenerator {

    private static HashMap<String, List<String>> tables = new HashMap<>(); // 
    private static HashMap<String, List<String>> rules = new HashMap<>();
    private static HashMap<String, List<String>> facts = new HashMap<>();

    public static Triple<HashMap<String,List<String>>,HashMap<String,List<String>>,HashMap<String,List<String>>> generateSQL(Program program) {
        
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
                List<String> createTableStatement = generateCreateTableStatement(fact.predicate);
                tables.put(predicateName, createTableStatement);
                
            }

            facts.computeIfAbsent(predicateName, k -> new ArrayList<>()).add(insertStatement);
        }
    }

    private static void generateSQLForRules(List<Rule> rulesList) {
        //DEBUG
        System.out.println("generateSQLForRules");

        for (Rule rule : rulesList) {
            String predicateName = rule.head.predicate.name;

            if (!tables.containsKey(predicateName)) {
                List<String> createTableStatement = generateCreateTableStatement(rule.head.predicate);
                tables.put(predicateName, createTableStatement);
            
            }

            List<String> ruleQueryStatements = generateRuleQueryStatement(rule);
            rules. computeIfAbsent(predicateName, k -> new ArrayList<>()).addAll(ruleQueryStatements);

        }
    }   

    private static String generateInsertStatement(Fact fact) {
        String predicateName = fact.predicate.name;
        String values = fact.predicate.terms.stream()
            .map(term -> term instanceof Variable ? "?" : "'" + ((Constant) term).name + "'")
            .collect(Collectors.joining(", "));
        return "INSERT INTO d" + predicateName + " VALUES (" + values + ")";
    }

    private static List<String> generateCreateTableStatement(Predicate predicate) {
        String predicateName = predicate.name;
        String columns = IntStream.range(0, predicate.terms.size())
            .mapToObj(i -> "a" + (i + 1) + " VARCHAR(255)")
            .collect(Collectors.joining(", "));
        
        // Generating the primary key clause, this enforces our table are sets (NO DUPLICATES)
        String primaryKeyColumns = IntStream.range(0, predicate.terms.size())
            .mapToObj(i -> "a" + (i + 1))
            .collect(Collectors.joining(", "));
        String primaryKey = "PRIMARY KEY (" + primaryKeyColumns + ")";
        List<String> semiNaiveTables = new ArrayList<>();
        semiNaiveTables.add("CREATE TABLE " + predicateName + " (" + columns + ", " + primaryKey + ")");
        semiNaiveTables.add("CREATE TABLE d" + predicateName + " (" + columns + ", " + primaryKey + ")");
        semiNaiveTables.add("CREATE TABLE dd" + predicateName + " (" + columns + ", " + primaryKey + ")");
        return semiNaiveTables;
    }


    private static List<String> generateRuleQueryStatement(Rule rule) {
        //DEBUG
        System.out.println("generateRuleQueryStatement");
        String head = rule.head.predicate.name;
        List<String> ruleStatements = new ArrayList<>();
        List<String> wheresFromConstants = new ArrayList<>();

        // If there are no join conditions, we can just do a simple select from the body
        if(rule.body.joinConditions == null | rule.body.joinConditions.isEmpty()){
            //SELECT 
            String select = rule.head.predicate.terms.stream()
            .map(term -> ((term instanceof Variable) ? "d" + term.source + "." + "a" + term.index : term.source))
            .collect(Collectors.joining(", "));
            
            //FROM
            String from = rule.body.predicates.stream()
            .map(predicate -> "d" + predicate.name + " AS " + "d" + predicate.alias)
            .collect(Collectors.joining(", "));


            ruleStatements.add("INSERT INTO dd" + head + " SELECT " + select + " FROM " + from  + " ON CONFLICT ON CONSTRAINT dd" + head + "_pkey DO NOTHING");
            System.out.println(rule.head.predicate.name + ": " + ruleStatements);
            return ruleStatements;
        }
        // If there are join conditions, complex semi-naive joining is required
        else{
                HashSet <String> requiredByHead = new HashSet<>();
                HashSet <String> presentInJoinCondition = new HashSet<>();
                List<Predicate> missingPredicates = new ArrayList<>();
                boolean onlyConstants = true;

                //Gather all WHERE conditions for the constants in the body of rule
                for (JoinCondition jc : rule.body.joinConditions) {
                    if(jc.tupleList == null){ // implies jc.constant != null
                        wheresFromConstants.add(jc.constantTuple.first.alias + ".a" + jc.constantTuple.second + " = '" + jc.variableName + "'");
                    }
                }
                System.out.println("wheresFromConstants: " + wheresFromConstants);
            
                // Gather all the Variable conditions to join semi-naively through UNION on condition.
                for (JoinCondition jc : rule.body.joinConditions) {
                    if(jc.tupleList != null){
                    onlyConstants = false;

                    // Which predicates are required by the head in Variable sources
                    for(Term term : rule.head.predicate.terms){
                        if(term instanceof Variable){
                        requiredByHead.add(term.source);
                        }
                    }
                    // Which predicates are present in the join condition and will be added to from1
                    if(jc.tupleList != null){
                        for(Tuple<Predicate,Integer> p : jc.tupleList){
                            presentInJoinCondition.add(p.first.alias);
                        }
                    }
                    // Which predicates are required by the head but not present in the join condition, will be added to from3
                    for (Predicate p : rule.body.predicates){
                            if (!presentInJoinCondition.contains(p.alias) && requiredByHead.contains(p.alias)){
                                missingPredicates.add(p);
                            }
                    }

                    //SELECT 
                    String select = rule.head.predicate.terms.stream()
                    .map(term -> ((term instanceof Variable) ? term.source + "." + "a" + term.index : term.source))
                    .collect(Collectors.joining(", "));
                    
                    System.out.println("select: " + select);
                    
                    //FROM
                    //TODO make set with predicates as we process, to avoid duplicates
                    
                    HashSet<String> duplicateCheck = new HashSet<>();
                    Tuple<Predicate,Integer> tuple;

                    tuple = jc.tupleList.get(0);
                    String from1 = "d" + tuple.first.name + " AS " + tuple.first.alias;
                    duplicateCheck.add(tuple.first.alias);

                    for(int i = 1; i < jc.tupleList.size(); i++){
                        tuple = jc.tupleList.get(i);

                        if(!duplicateCheck.contains(tuple.first.name)){
                            from1 += ", (SELECT * FROM " + tuple.first.name + " UNION SELECT * FROM d" + tuple.first.name + ") AS " + tuple.first.alias;
                            
                            duplicateCheck.add(tuple.first.name);
                        }

                    }System.out.println("from1: " + from1 );

                    duplicateCheck.clear();

                    tuple = jc.tupleList.get(0);
                    String from2 = tuple.first.name + " AS " + tuple.first.alias;
                    duplicateCheck.add(tuple.first.alias);

                    for(int i = 1; i < jc.tupleList.size(); i++){
                        tuple = jc.tupleList.get(i);
                        
                        if(!duplicateCheck.contains(tuple.first.name)){
                            from2 += ", d" + tuple.first.name + " AS " + tuple.first.alias;
                            
                            duplicateCheck.add(tuple.first.name);
                        }
                        
                    }System.out.println("form2: " + from2);

                    String from3 = "";
                    for(Predicate p : missingPredicates){
                        from3 += p.name + " AS " + p.alias + ", ";
                    }System.out.println("form3: " + from3);


                    String  where = "";
                        List<String> conditions = new ArrayList<>();
                        for (int i = 0; i < jc.tupleList.size() - 1; i++) {
                            Tuple<Predicate, Integer> current = jc.tupleList.get(i);
                            Tuple<Predicate, Integer> next = jc.tupleList.get(i + 1);
                            conditions.add(current.first.alias + ".a" + current.second + " = " + next.first.alias + ".a" + next.second);
                        }
                        where += String.join(" AND ", conditions);
                        
                        where += (wheresFromConstants.isEmpty() ? "" : " AND " + String.join(" AND ", wheresFromConstants));
                    //}

                    // String onDuplicateKeyUpdate = "";
                    // List<Term> list = rule.head.predicate.terms;
                    // for (int i = 1; i < list.size()+1; i++) {
                    // if(list.get(i-1) instanceof Constant){
                    //     //DONT DO IT
                    // }
                    // else {
                    //     onDuplicateKeyUpdate += "a" + i + "=VALUES(a" + i + "), ";
                    // }
                    // }
                    // onDuplicateKeyUpdate = onDuplicateKeyUpdate.substring(0, onDuplicateKeyUpdate.length()-2);
                    
                    String returnStr = "INSERT INTO " + "dd" + head + 
                    "( SELECT " + select + " FROM " + from1 + (from3.isEmpty() ? from3 : "") +
                    (where.isEmpty() ? "" : " WHERE " + where) +
                    " UNION " +
                    "SELECT " + select + " FROM " + from2 + (from3.isEmpty() ? from3 : "") + 
                    (where.isEmpty() ? "" : " WHERE " + where) + ") " 
                    //+ " )ON DUPLICATE KEY UPDATE " + onDuplicateKeyUpdate
                    + "ON CONFLICT ON CONSTRAINT dd" + head + "_pkey DO NOTHING"
                    ;
                    
                    //DEBUG
                    System.out.println(rule.head.predicate.name + ": " + returnStr);

                    ruleStatements.add( returnStr);
                }    
                }
            if(onlyConstants){
                //TODO reuse select, + standard from + constant only where.
                ;
            }

            return ruleStatements;
            }
    }
    

       

    public Map<String, List<String>> getTables() {
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

