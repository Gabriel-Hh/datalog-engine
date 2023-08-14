package c6591;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Function;
import c6591.ASTClasses.Triple;

public class SFixedPoint {
    private static HashMap<String, List<String>> rules = new HashMap<>();
    private static HashMap<String,List<String>> tables = new HashMap<>();
    //private static HashMap<String,Integer> tablesCounts = new HashMap<>();
    private static Connection conn;
    private static boolean isFixedPoint = false;
    private static boolean isChanged = false;

    public static void find(Triple<HashMap<String,List<String>>,HashMap<String,List<String>>,HashMap<String,List<String>>> sqlStatements) throws SQLException{ 
        
        rules = sqlStatements.third;
        tables = sqlStatements.first;
        
        //tablesCounts = tables.keySet().stream().collect(Collectors.toMap(Function.identity(), k -> 0, (v1, v2) -> v1, HashMap::new));
        conn = App.conn;
        int iteration = 1;
        

        while(!isFixedPoint){
            if(App.verbose){
                System.out.println("Iteration: " + iteration);
            }

            int count = 1;
            for(String ruleHead : rules.keySet()){
                for(String rule : rules.get(ruleHead)){
                    //adding facts to the rules to the tables semi-naively
                    // ddp is made here with all the new rules
                    try{
                        conn.createStatement().execute(rule);
                    }catch(SQLException e){
                        //if(e.getErrorCode() == 23505)
                            System.out.println("Error: " + e.getMessage());
                        //else
                            //throw e;
                    }
                }
            }
            
            
            isChanged = false;
            for(String table : tables.keySet()){

                // int colcount=0;
                // try{
                //     ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table +"'" );
                // if (rs.next()) {
                //     colcount = rs.getInt(1);
                //     System.out.println("colcount: " + colcount);
                // }
                // }catch(SQLException e){
                //     System.out.println("Cant et column count for" + table + e.getMessage()); 
                // }
                // String dtable = "d"+table;

                // //Make NOT In string
                // String whereNotExists = "(SELECT 1 From " + table + " WHERE ";
                // colcount =1;
                // for(int i=1; i<=colcount; i++){
                //     whereNotExists += dtable +".a"+ i + " = " + table + ".a" + i;
                //     if(i != colcount)
                //         whereNotExists += " AND ";
                //     }


                    

                //TODO: get rid of WHERE NOT EXIST, and use ON DUPLICATE DO NONTHING (DONE)   
                String dtable2table = "INSERT INTO " + table + " SELECT * FROM " + "d"+ table + " ON CONFLICT ON CONSTRAINT " + table + "_pkey DO NOTHING";
                conn.createStatement().execute(dtable2table);
        
                //
                String recordDelete = "TRUNCATE TABLE " + "d" + table;
                conn.createStatement().execute(recordDelete);
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + "d" + table );
                if(rs.next()){
                    count = rs.getInt(1);
                }
            }

            for(String table : tables.keySet()){
                int colcount=0;
                try{
                    ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT (*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '" + table +"'" );
                if (rs.next()) {
                    colcount = rs.getInt(1);
                }
                }catch(SQLException e){
                    System.out.println("Cant get column count for " + table + e.getMessage()); 
                }
                //String ddtable = "dd"+table;
                //String dtable = ddtable.substring(1);

                //Make NOT In string
                //String whereNotExists = "(SELECT 1 From " + dtable + " WHERE ";
                //BYPASS
                // colcount =1;
                //BYPASS
                // for(int i=1; i<=colcount; i++){
                //     whereNotExists += ddtable +".a"+ i + " = " + dtable + ".a" + i;
                //     if(i != colcount)
                //         whereNotExists += " AND ";}

                //TODO: we need to add the ONCONFILCT as well as change the where not exist to accept(or postgres set difference)
                //putting in dtable from ddtable not in table.        
                String ddtable2dtable = "INSERT INTO " + "d" + table + " SELECT * FROM " + "dd" + table + " EXCEPT SELECT * FROM " + table; 
                
                
                conn.createStatement().execute(ddtable2dtable);
                ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM " + "d" + table );
                if(rs.next()){
                    count = rs.getInt(1);
                }

                //Emptying ddtable for next iteration
                String recordDelete = "TRUNCATE TABLE " + "dd" + table;
                conn.createStatement().execute(recordDelete);

                if(count != 0){
                    isChanged = true;
                }


                
            }

            if(!isChanged)
               isFixedPoint = true;
            else
                iteration += 1;
        }
        System.out.println("Final Iteration: " + (iteration));
        System.out.println("Fixed point found successfully.");
    }

}
