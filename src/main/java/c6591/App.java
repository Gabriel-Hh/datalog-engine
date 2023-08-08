package c6591;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import parser.DatalogParser;
import parser.ParseException;
import c6591.ASTClasses.Program;
import c6591.ASTClasses.Triple;
import c6591.ASTClasses.Tuple;
import c6591.FixedPoint;

import oldTestJavaFiles.H2test;
import oldTestJavaFiles.Token_Raquib;
//import c6591.SQLGenerator;


public class App {
    public static void main(String[] args) {
        Triple<HashMap<String,String>,HashMap<String,List<String>>,HashMap<String,List<String>>> sqlStatements;

        //Determine the input file
        String inputDirectory = System.getProperty("user.dir") + "/input/";
        String fileName = "test.dl"; //default test file.
        ArrayList<ArrayList<String>> facts = new ArrayList<>();
        
        // If the user specifies a file, use that instead.
        if(args.length > 0) {
            fileName = args[0];
        }
        String filePath = inputDirectory + fileName;

        System.out.println("======================================================");
        System.out.println("Running OLD TEST CODE");
        System.out.println("======================================================");
        
        System.out.println("Running Token_Raquib.parse:");
        facts = Token_Raquib.parse(filePath);

        System.out.println("Running H2test:");
        H2test.test();


        //ENGINE CODE
        
        System.out.println("======================================================");
        System.out.println("Running ENGINE CODE");
        System.out.println("======================================================");
        System.out.println("Running the javaCC parser:");
        Program program = new Program();
        
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

        System.out.println("Running SQLGenerator:");
        sqlStatements =  SQLGenerator.generateSQL(program);
        System.out.println("Sql statements generated successfully.");
        SQLGenerator.printAll();

        System.out.println("======================================================");
        System.out.println("Running InitDatabase.init():");
        System.out.println("======================================================");
        InitDatabase.init(new Tuple<>(sqlStatements.first, sqlStatements.second));
        InitDatabase.printAll();

        System.out.println("======================================================");
        System.out.println("Running FixedPoint.find():");
        System.out.println("======================================================");
        try{
            FixedPoint.find(sqlStatements);
        } catch (Exception e) {
            System.out.println("Error: FixedPoint.find() " + e.getMessage());
        }
        System.out.println("Fixed point found successfully.");
        System.out.println("======================================================");
        InitDatabase.printFacts();
    }
}
