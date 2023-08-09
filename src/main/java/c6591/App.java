package c6591;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;

import parser.DatalogParser;
import parser.ParseException;
import c6591.ASTClasses.Program;
import c6591.ASTClasses.Triple;
import c6591.ASTClasses.Tuple;



public class App {
    public static boolean verbose = false; // TRUE prints intermediate steps and final results to console.
    public static Connection conn = null;

    public static void main(String[] args) {
        Triple<HashMap<String,String>,HashMap<String,List<String>>,HashMap<String,List<String>>> sqlStatements;
        
    //     //Delete old DB file if it exists.
    //     try {
    //     Files.deleteIfExists(Paths.get("./diskDB_test" + ".mv.db"));
    //     // Add additional lines here if you use any other H2 storage options that create additional files
    // } catch (IOException e) {
    //     System.out.println("Error deleting the previous database files: " + e.getMessage());
    // }

        try{
            conn = DriverManager.getConnection("jdbc:h2:./diskDB_test"); //Persistent connection.
        } catch (Exception e) {System.out.println("Error: Main Connection to database failed. " + e.getMessage());}

        
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
            FixedPoint.find(sqlStatements);
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
        


        // RUNTIME BREAKDOWN
        System.out.println("======================================================");
        System.out.println("Runtime Breakdown:");
        System.out.println("Program Parser: " + (parseEnd - parseStart) + "ms");
        System.out.println("SQL Generation: " + (sqlEnd - sqlStart) + "ms");
        System.out.println("Database Initialization: " + (initEnd - initStart) + "ms");
        System.out.println("Fixed Point: " + (fixedPointEnd - fixedPointStart) + "ms");
        System.out.println("Write to File: " + (writeEnd - writeStart) + "ms");


        try {
        conn.close();
    } catch (SQLException e) {
        System.out.println("Error closing main database connection " + e.getMessage());
    }

        //Delete DB file if it exists.
        try {
        Files.deleteIfExists(Paths.get("./diskDB_test" + ".mv.db"));
        // Add additional lines here if you use any other H2 storage options that create additional files
    } catch (IOException e) {
        System.out.println("Error deleting the previous database files: " + e.getMessage());
    }
    }
}
