package c6591;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;

public class InitDatabase {
    private static HashSet<String> fact_predicates = new HashSet<>();
    private static HashSet<String> rules_predicates = new HashSet<>();
    private static Connection conn;
    ;

    public static void init(ArrayList<ArrayList<String>> facts, ArrayList<ArrayList<ArrayList <String>>> rules) {
        // Connect to the H2 database
        System.out.println("Connecting to database...");
        try {
            conn = DriverManager.getConnection("jdbc:h2:mem:test");

            for(ArrayList<String> fact : facts) {
                processFact(fact);
            }
            
            //For now rules is null
            for(ArrayList<ArrayList<String>> rule : rules) {
                processRule(rule);
            }
        } catch (SQLException e) {
            System.out.println("Error: InitDatabase" + e.getMessage());
        }
        System.out.println("Database initialized.");
    }


    //Process fact
    private static void processFact(ArrayList<String> fact){
        if(fact_predicates.add(fact.get(0))){
            createFactTable(fact);
        }
        insertFact(fact);
    }


    // Create fact table
    private static void createFactTable(ArrayList<String> fact){
        
        // Create an attribute ai for each constant in the fact
        int numCols = fact.size() - 1;
        String colStatements = "";
        for(int i=1; i<=numCols; i++){
            colStatements += "a" + i + " VARCHAR(255), ";
        }
        colStatements = colStatements.substring(0, colStatements.length() - 2); // get rid of extra comma


        String sql = "CREATE TABLE " + fact.get(0) + " (" + colStatements + ")";
        // Create the table
        try{
            conn.createStatement().executeUpdate(sql);
        }   catch (SQLException e) {
            System.out.println("Error: InitDatabase - Could not create " + fact.get(0) + " table");
        }
    }

    // Insert fact
    private static void insertFact(ArrayList<String> fact){

            String values = "";
            for(int i=1; i<fact.size(); i++){
                values += "'" + fact.get(i) + "', ";
            }
            values = values.substring(0, values.length() - 2); // get rid of extra comma
            
            String sql = "INSERT INTO " + fact.get(0) + " VALUES (" + values + ")";
        try{
            conn.createStatement().executeUpdate(sql);
        }   catch (SQLException e) {
            System.out.println("Error: InitDatabase - Could not insert fact");
        }
    }

    // Process rule
    private static void processRule(ArrayList<ArrayList<String>> rule){
        if(rules_predicates.add(rule.get(0).get(0))){
            createRuleTable(rule);
        }
    }

    private static void createRuleTable(ArrayList<ArrayList<String>> rule){
        //TODO: Create rule table
    }

    public static void printFacts(){
        for(String predicate : fact_predicates){
            String sql = "SELECT * FROM " + predicate;
            try{
                
                ResultSet rs = conn.createStatement().executeQuery(sql);
                while(rs.next()){
                    String fact = (predicate + "(");
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int numCols = rsmd.getColumnCount();
                    for(int i=1; i<=numCols; i++){
                        fact+= rs.getString(i) + ", ";
                    }
                    fact = fact.substring(0, fact.length() - 2); // get rid of extra comma
                    System.out.println(fact +").");
                }
            }   catch (SQLException e) {
                System.out.println("Error: InitDatabase - Could not print facts");
            }
        }
    }
}

