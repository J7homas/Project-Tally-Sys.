import java.util.*;
import java.sql.*;

public class projectTallySys {
    private List<Integer> totalProjects = new ArrayList<>();
    private List<Character> projectConfirmation = new ArrayList<>();
    private Scanner scanner = new Scanner(System.in);
    private int count = 0;
    private String dbURL = "jdbc:mysql://localhost:3306/projecttally?useSSL=false&serverTimezone=UTC";
    private String username = "root";
    private String password = "RootPassword1$";

    public void fetchLastProjectNumber() {
        String sql = "SELECT MAX(ProjectNumber) FROM tallysys"; // Query to find the highest project number
        try (Connection conn = DriverManager.getConnection(dbURL, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                count = rs.getInt(1); // Set 'count' to the highest project number found, if any
                System.out.println("Starting from project number: " + count);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int numberOfProjectsInput() {
        int numOfProject;

        System.out.println("Enter the number of projects you've done this week:");
        numOfProject = scanner.nextInt();

        if (numOfProject > 0) {
            for (int i = 0; i < numOfProject; i++) { // for each project entered
                totalProjects.add(count + 1); // represent project with index starting from 1
                count++; // increment for each new project
            }
        }

        return numOfProject;
    }

    public void projectConfirmationsInput(int numProjectsAdded) {
        for (int i = 0; i < numProjectsAdded; i++) {
            System.out.println("Confirm project completion (x/o):");
            projectConfirmation.add(scanner.next().charAt(0));
        }
    }

    public void tallySys() {
        char response;
        do {
            projectConfirmationsInput(numberOfProjectsInput());
            System.out.println("Would you like to track another set of projects? (y/n)");
            response = Character.toLowerCase(scanner.next().charAt(0));
        } while (response != 'n');
    }

    public void display() {
        for (int i = 0; i < totalProjects.size(); i++) {
            System.out.printf("%10d\t|\t%c%n", totalProjects.get(i), projectConfirmation.get(i));
        }
    }

    public void db_connection(){
        try {
            Connection conn = DriverManager.getConnection(dbURL, username, password);
            if (conn != null) {
                System.out.println("Connected");
            }
        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void db_data_input() {
        // Using PreparedStatement to prevent SQL injection
        String sql = "INSERT INTO tallysys (ProjectNumber, Confirmation) VALUES (?, ?)";
        
        try (Connection con = DriverManager.getConnection(dbURL, username, password)) {

            // PreparedStatement allows you to insert values safely
            PreparedStatement pstmt = con.prepareStatement(sql);
            
            // Loop over the list and insert data row by row
            for (int i = 0; i < totalProjects.size(); i++) {
                pstmt.setInt(1, totalProjects.get(i)); // Set project number
                pstmt.setString(2, String.valueOf(projectConfirmation.get(i))); // Set confirmation (as string)
                
                // Execute the update (no ResultSet involved)
                pstmt.executeUpdate();
            }

            System.out.println("Data inserted successfully.");

        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateEntry() {
        System.out.println("Enter the project number you want to update:");
        int projectNumber = scanner.nextInt();
        scanner.nextLine();  // Consume newline
        
        // Fetch the current confirmation for this project from the database
        String sqlFetch = "SELECT Confirmation FROM tallysys WHERE ProjectNumber = ?";
        String currentConfirmation = "";
        
        try (Connection conn = DriverManager.getConnection(dbURL, username, password);
             PreparedStatement fetchStmt = conn.prepareStatement(sqlFetch)) {
            
            fetchStmt.setInt(1, projectNumber);
            ResultSet rs = fetchStmt.executeQuery();
            
            if (rs.next()) {
                currentConfirmation = rs.getString("Confirmation");
                System.out.println("Current confirmation for project " + projectNumber + " is: " + currentConfirmation);
            } else {
                System.out.println("Project number " + projectNumber + " does not exist.");
                return;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        // Ask for the new confirmation status
        System.out.println("Enter the new confirmation (x/o):");
        char newConfirmation = scanner.next().charAt(0);
        
        // Update the confirmation in the database
        String sqlUpdate = "UPDATE tallysys SET Confirmation = ? WHERE ProjectNumber = ?";
        
        try (Connection conn = DriverManager.getConnection(dbURL, username, password);
             PreparedStatement updateStmt = conn.prepareStatement(sqlUpdate)) {
            
            updateStmt.setString(1, String.valueOf(newConfirmation));
            updateStmt.setInt(2, projectNumber);
            
            int rowsAffected = updateStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("Project number " + projectNumber + " updated successfully.");
            } else {
                System.out.println("Failed to update project number " + projectNumber);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    public static void main(String[] args) {
        projectTallySys p = new projectTallySys();
        p.db_connection(); // Connect to the database
        p.fetchLastProjectNumber(); // Retrieve the last project number to start from
        p.tallySys(); // Gather new inputs
        p.display(); // Display the gathered data
        p.db_data_input(); // Insert the new data into the database

        System.out.println("Would you like to update any existing project entry? (y/n)");
        char updateResponse = p.scanner.next().charAt(0);
    
        if (Character.toLowerCase(updateResponse) == 'y') {
            p.updateEntry(); // Call the update method
        }
    }
}
