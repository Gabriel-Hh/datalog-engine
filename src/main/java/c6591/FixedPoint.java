package c6591;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import c6591.ASTClasses.Tuple;
import c6591.ASTClasses.Triple;
import c6591.InitDatabase;

public class FixedPoint {
    private static HashMap<String, List<String>> rules = new HashMap<>();
    private static Connection conn;
    
    public static void find(Triple<HashMap<String,String>,HashMap<String,List<String>>,HashMap<String,List<String>>> sqlStatements){
        rules = sqlStatements.third;
        
        //Connect to the H2 database
        try{
            conn = DriverManager.getConnection("jdbc:h2:mem:test");
            
            InitDatabase.printAll();

        } catch (SQLException e) {
            System.out.println("Error: FixedPoint" + e.getMessage());
        }


    }
}
