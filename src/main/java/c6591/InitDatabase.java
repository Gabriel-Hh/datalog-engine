package c6591;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import c6591.ASTClasses.Tuple;


public class InitDatabase {
    private static Connection conn;
    private static HashMap<String, List<String>> tables = new HashMap<>();
    private static HashMap<String, List<String>> facts = new HashMap<>();

    public static void init(Tuple<HashMap<String,List<String>>,HashMap<String,List<String>>> tables_facts) {
        tables = tables_facts.first;
        facts = tables_facts.second;
        

        // Create and Connect to the H2 database
        System.out.println("Connecting to database...");
        try 
        {
        
        conn = DriverManager.getConnection("jdbc:h2:mem:test");

        // Create tables 
        System.out.println("Creating tables...");
        for(List<String> threeTables : tables.values()) {
            for(String sql : threeTables) {
                conn.createStatement().execute(sql);
            }
            
        }

        // Insert facts
        System.out.println("Inserting facts...");
        for (String table : facts.keySet()) {
            for (String sql : facts.get(table)) {
                conn.createStatement().execute(sql);
            }
        }

        System.out.println("Database initialized.");    
        } catch (SQLException e) {
            System.out.println("Error: InitDatabase" + e.getMessage());
        }
    }


    public static void printAll(){
        printTableList();
        printFacts();
    }

    public static void printTableList(){
        System.out.println("Table List: ");
        try{
        String sql = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'TABLE' ORDER BY TABLE_NAME";
            ResultSet rs = conn.createStatement().executeQuery(sql)  ;
            while(rs.next()){
                System.out.println(rs.getString(3));
            }
        }   catch (SQLException e) {
            System.out.println("Error: InitDatabase - Could not print table list");
        }
    }

    public static void printFacts(){
        System.out.println("Facts: ");
        for(String table : tables.keySet()){
            String sql = "SELECT * FROM " + table;
            try{
                
                ResultSet rs = conn.createStatement().executeQuery(sql);
                while(rs.next()){
                    String fact = (table + "(");
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

    public static void writeFacts() {
    String timestamp = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss").format(new Date());
    String outputPath = "output/output" + timestamp + ".txt";

    try (FileWriter writer = new FileWriter(outputPath)) {
        for (String table : tables.keySet()) {
            String sql = "SELECT * FROM " + table;

            try {
                ResultSet rs = conn.createStatement().executeQuery(sql);
                while (rs.next()) {
                    String fact = (table + "(");
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int numCols = rsmd.getColumnCount();
                    for (int i = 1; i <= numCols; i++) {
                        fact += rs.getString(i) + ", ";
                    }
                    fact = fact.substring(0, fact.length() - 2); // get rid of extra comma
                    writer.write(fact + ").\n");
                }
            } catch (SQLException e) {
                System.out.println("Error: InitDatabase - Could not access facts for output file " + e.getMessage());
            }
        }
        writer.flush();
        writer.close();
        System.out.println("Facts written to file successfully.");
    } catch (IOException e) {
        System.out.println("Error: Could not write facts to output file." + e.getMessage());
    }
}







    
}
