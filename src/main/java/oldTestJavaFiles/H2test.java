package oldTestJavaFiles;

import java.sql.*;

public class H2test {
    public static void test() {
        try {

            // Connect to the H2 database
            System.out.println("Connecting to database...");
            Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");

            // Create a table
            System.out.println("Creating table...");
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE test (id INT, name VARCHAR(255))");

            // Insert some data
            System.out.println("Inserting data...");
            PreparedStatement ps = conn.prepareStatement("INSERT INTO test VALUES (?, ?)");
            ps.setInt(1, 1);
            ps.setString(2, "John");
            ps.executeUpdate();

            // Retrieve the data
            System.out.println("Retrieving data...");
            ResultSet rs = stmt.executeQuery("SELECT * FROM test");
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " " + rs.getString("name"));
            }

            // Close the connections
            rs.close();
            ps.close();
            stmt.close();
            conn.close();

            System.out.println("Program completed successfully.");
        } catch (Exception e) {
            System.out.println("An error occurred:");
            e.printStackTrace();
        }
    }
}
