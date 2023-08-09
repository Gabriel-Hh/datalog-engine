package c6591;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;
import c6591.ASTClasses.Triple;

public class FixedPoint {
    private static HashMap<String, List<String>> rules = new HashMap<>();
    private static HashMap<String,String> tables = new HashMap<>();
    private static HashMap<String,Integer> tablesCounts = new HashMap<>();
    public static Connection conn;
    private static boolean isFixedPoint = false;
    private static boolean isChanged = false;
    
    public static void find(Triple<HashMap<String,String>,HashMap<String,List<String>>,HashMap<String,List<String>>> sqlStatements) throws SQLException{
        rules = sqlStatements.third;
        tables = sqlStatements.first;
        //Initialize the tablesCounts to 0 for all table
        tablesCounts = tables.keySet().stream()
        .collect(Collectors.toMap(Function.identity(), k -> 0, (v1, v2) -> v1, HashMap::new));

        
        //Connect to the H2 database
        //conn = DriverManager.getConnection("jdbc:h2:mem:test");
        conn = App.conn;


        int iteration = 1;

        //Evaluate until a fixed point is reached
        while(!isFixedPoint){
            // Run an iteration of rules
            if(App.verbose) {System.out.println("Iteration: " + iteration);}

            for (String ruleHead : rules.keySet()){
                for (String rule : rules.get(ruleHead)){
                    try {
                        conn.createStatement().execute(rule);
                    } catch (SQLException e) {
                        // If the error code matches the unique constraint violation, ignore it
                        if (e.getErrorCode() == 23505) {
                            // This 'error' is normal is enforcing that are tables are sets.
                            continue;
                        }
                        // Otherwise, rethrow the exception
                        throw e;
                    }
                } 
            }     
            
            // Check if the tables have changed
                isChanged = false;
            for (String table : tables.keySet()){
                // Get the count of the table
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + table);
                int count = 0;
                if (rs.next()) {
                    count = rs.getInt(1);
                }
                // Check if the count has changed
                if (!isChanged && count != tablesCounts.get(table)){
                    isChanged = true;
                }
                tablesCounts.put(table, count);
                if(App.verbose) {System.out.println("Table: " + table + " Count: " + count);}
            }

            // If the tables have not changed, we have reached a fixed point
            if (!isChanged){
                isFixedPoint = true;
            }
            iteration++;   
        }
    System.out.println("Final Iteration: " + (iteration-1));
    System.out.println("Fixed point found successfully.");
    }
}