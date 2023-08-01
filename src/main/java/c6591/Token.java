package c6591;

import java.util.ArrayList;
//import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

//class to split apart tokens from inputed datalog file.
public class Token {

	public static void main(String[] args) {
		String file = "datalog_program.txt"; //the input file name.
		ArrayList<ArrayList<String>> result = new ArrayList<>();
		
		//int pos = 0;

  
       
		
		try(FileReader filereader = new FileReader(file);
				BufferedReader bufferedreader = new BufferedReader(filereader)){
			Pattern pattern = Pattern.compile("([A-Za-z]+|[A-Za-z])");
			
			
			String newline;
			while((newline = bufferedreader.readLine()) != null) {
				//System.out.println(result);
				ArrayList<String> placeholder = new ArrayList<>();
				//System.out.println(newline);
			    Matcher matcher = pattern.matcher(newline);
			    
			    while(matcher.find()) {	
			     String header = matcher.group(); 
		         //String[] elements = content.split(","); 
		         //System.out.println(content);
			     //System.out.println(header);
		         //ArrayList<String> temp = new ArrayList<>();
			     System.out.println(header);
			     placeholder.add(header);
			     }
			    
			    System.out.println(placeholder);
			    result.add(placeholder);
			    //System.out.println(result);
			    //pos +=1;
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println(result);
	}

    // Parse the input file and print out the tokens.
    public static void parse(String filePath) {
        
		ArrayList<ArrayList<String>> result = new ArrayList<>();
       
		
		try(FileReader filereader = new FileReader(filePath);
				BufferedReader bufferedreader = new BufferedReader(filereader)){
			Pattern pattern = Pattern.compile("([A-Za-z]+|[A-Za-z])");
			
			
			String newline;
			while((newline = bufferedreader.readLine()) != null) {
				//System.out.println(result);
				ArrayList<String> placeholder = new ArrayList<>();
				//System.out.println(newline);
			    Matcher matcher = pattern.matcher(newline);
			    
			    while(matcher.find()) {	
			     String header = matcher.group(); 
		         //String[] elements = content.split(","); 
		         //System.out.println(content);
			     //System.out.println(header);
		         //ArrayList<String> temp = new ArrayList<>();
			     //System.out.println(header);
			     placeholder.add(header);
			     }
			    
			    //System.out.println(placeholder);
			    result.add(placeholder);
			    //System.out.println(result);
			    //pos +=1;
			}
			
		}catch(IOException e) {
			e.printStackTrace();
		}
		System.out.println(result);

        
    }

}
