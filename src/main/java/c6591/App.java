package c6591;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import parser.DatalogParser;
import parser.ParseException;
import c6591.ASTClasses.Program;
import c6591.ASTClasses.Triple;
import c6591.ASTClasses.Tuple;



public class App {
    public static boolean verbose = false; // TRUE prints intermediate steps and final results to console.
    public static Connection conn;

    public static void main(String[] args) {
        Triple<HashMap<String,List<String>>,HashMap<String,List<String>>,HashMap<String,List<String>>> sqlStatements;
        
        //DATABASE CONNECTION
        String dbUrl = "jdbc:postgresql://localhost:5432/test";
        String dbUser = "myuser";
        String dbPassword = "password";
        try{
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        } catch (SQLException e) { System.out.println("Error: Connection to database failed."+ e.getMessage());}
        
        dropAllTables();

        //ARGS (INPUT FILE + VERBOSE) 
        // Both arguments are optional. If no arguments are given, the default test file is used with verbose = false.
        String inputDirectory = System.getProperty("user.dir") + "/input/";
        String fileName = "test.dl"; //default test file.
        
        // One argument, either filename or -verbose.
        if(args.length > 0) {
            if(args[0].toLowerCase().equals("-verbose")) {
                verbose = true;
            }
            else { // filename is the first argument.
                fileName = args[0];
            }
        }
        String filePath = inputDirectory + fileName;
        
        // Two arguments, last argument is -verbose.
        if(args.length > 1) {
            if(args[1].toLowerCase().equals("-verbose")) {
                verbose = true;
            }
        }


        //PARSER
        System.out.println("======================================================");
        System.out.println("Running DatalogParser.Program():");
        Program program = new Program();
        
        long parseStart = System.currentTimeMillis();
        try {
            // Create a FileInputStream for the file to be parsed
            FileInputStream fis = new FileInputStream(filePath);
            
            // Create a new instance of the parser
            DatalogParser parser = new DatalogParser(fis);

            // Parse the file
            program = parser.Program();
            
            System.out.println("Program parsed successfully.");
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
        long parseEnd = System.currentTimeMillis();
        

        // SQL GENERATOR
        System.out.println("======================================================");
        System.out.println("Running SQLGenerator.generateSQL():");
    
        long sqlStart = System.currentTimeMillis();
        sqlStatements =  SQLGenerator.generateSQL(program);
        long sqlEnd = System.currentTimeMillis();
        
        if(verbose) {SQLGenerator.printAll();}


        // DATABASE INITIALIZATION
        System.out.println("======================================================");
        System.out.println("Running InitDatabase.init():");
        
        long initStart = System.currentTimeMillis();
        InitDatabase.init(new Tuple<>(sqlStatements.first, sqlStatements.second));
        long initEnd = System.currentTimeMillis();
        
        if(verbose){InitDatabase.printAll();}


        // FIXED POINT
        System.out.println("======================================================");
        System.out.println("Running FixedPoint.find():");
        
        long fixedPointStart = System.currentTimeMillis();
        try{
            SFixedPoint.find(sqlStatements);
        } catch (Exception e) {
            System.out.println("Error: FixedPoint.find() " + e.getMessage());
        }
        long fixedPointEnd = System.currentTimeMillis();

        if(verbose) {InitDatabase.printFacts();}


        //WRITE TO FILE
        System.out.println("======================================================");
        System.out.println("Running InitDatabase.writeFacts():");
        
        long writeStart = System.currentTimeMillis();
        InitDatabase.writeFacts();
        long writeEnd = System.currentTimeMillis();
        
        
        //dropAllTables();
        try{
            conn.close();
        } catch (Exception e) {
            System.out.println("Error: InitDatabase.close() " + e.getMessage());
        }

        // RUNTIME BREAKDOWN
        System.out.println("======================================================");
        System.out.println("Runtime Breakdown:");
        System.out.println("Program Parser: " + (parseEnd - parseStart) + "ms");
        System.out.println("SQL Generation: " + (sqlEnd - sqlStart) + "ms");
        System.out.println("Database Initialization: " + (initEnd - initStart) + "ms");
        System.out.println("Fixed Point: " + (fixedPointEnd - fixedPointStart) + "ms");
        System.out.println("Write to File: " + (writeEnd - writeStart) + "ms");
        
    }
    
    

    public static void dropAllTables() {
    try (Statement stmt = conn.createStatement()) {
        // Fetch all table names
        ResultSet rs = stmt.executeQuery("SELECT tablename FROM pg_tables WHERE schemaname = 'public';");
        List<String> tableNames = new ArrayList<>();
        while (rs.next()) {
            tableNames.add(rs.getString(1));
        }
        rs.close();

        for (String tableName : tableNames) {
            stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName + " CASCADE;");
        }
    } catch (SQLException e) {
        System.out.println("Error dropping tables: " + e.getMessage());
    }
}

}
