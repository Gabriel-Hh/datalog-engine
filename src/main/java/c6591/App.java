package c6591;


public class App {
    public static void main(String[] args) {
        
        //Determine the input file
        String inputDirectory = System.getProperty("user.dir") + "/input/";
        String fileName = "test.dl"; //default test file.
        
        // If the user specifies a file, use that instead.
        if(args.length > 0) {
            fileName = args[0];
        }
        String filePath = inputDirectory + fileName;

        
        System.out.println("Running Token.parse:");
        Token.parse(filePath);

        System.out.println("Running H2test:");
        H2test.test();
    }
}
