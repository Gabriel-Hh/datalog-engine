package c6591;

import java.util.ArrayList;

public class App {
    public static void main(String[] args) {
        
        //Determine the input file
        String inputDirectory = System.getProperty("user.dir") + "/input/";
        String fileName = "test.dl"; //default test file.
        ArrayList<ArrayList<String>> facts = new ArrayList<>();
        
        // If the user specifies a file, use that instead.
        if(args.length > 0) {
            fileName = args[0];
        }
        String filePath = inputDirectory + fileName;

        
        System.out.println("Running Token.parse:");
        facts = Token.parse(filePath);

        System.out.println("Running H2test:");
        H2test.test();

        System.out.println("Running InitDatabase.init:");
        InitDatabase.init(facts, new ArrayList<ArrayList<ArrayList<String>>>());

        System.out.println("Running InitDatabase.printFacts:");
        InitDatabase.printFacts();
    }
}
