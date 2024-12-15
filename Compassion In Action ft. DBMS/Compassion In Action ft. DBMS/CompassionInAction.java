import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

class Notification {
    private String message;
    private boolean isRead;

    public Notification(String message) {
        this.message = message;
        this.isRead = false;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void markAsRead() {
        isRead = true;
    }
}

class User {
    private String username;
    private String password;
    private String location;
    private String preferredCauses;
    private String skills;
    private ArrayList<Notification> notifications;

    public User(String username, String password, String location) {
        this.username = username;
        this.password = password;
        this.location = location;
        this.preferredCauses = "";
        this.skills = "";
        this.notifications = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getLocation() {
        return location;
    }

    public boolean authenticate(String password) {
        return this.password.equals(password);
    }

    public void setProfile(Scanner scanner) {
        System.out.print("Enter your preferred causes (e.g., Education, Health, Environment): ");
        this.preferredCauses = scanner.nextLine();
        System.out.print("Enter your skills (e.g., Teaching, First Aid, Cooking): ");
        this.skills = scanner.nextLine();
    }

    public void displayProfile() {
        System.out.println("User Profile:");
        System.out.println("Preferred Causes: " + (preferredCauses.isEmpty() ? "Not set" : preferredCauses));
        System.out.println("Skills: " + (skills.isEmpty() ? "Not set" : skills));
    }

    public String getPreferredCauses() {
        return preferredCauses;
    }

    public String getSkills() {
        return skills;
    }

    public ArrayList<Notification> getNotifications() {
        return notifications;
    }

    public void addNotification(String message) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO notifications (user_id, message) VALUES (?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(2, message);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setPreferredCauses(String preferredCauses) {
        this.preferredCauses = preferredCauses;
    }
    
    public void setSkills(String skills) {
        this.skills = skills;
    }

    public void viewNotifications() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM notifications WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
    
            System.out.println("\nNotifications:");
            if (!rs.isBeforeFirst()) { // check notifs
                System.out.println("No new notifications.");
            } else {
                while (rs.next()) {
                    String message = rs.getString("message");
                    boolean isRead = rs.getBoolean("is_read");
                    String status = isRead ? "[Read]" : "[Unread]";
                    System.out.println(status + " " + message);
                }
                markNotificationsAsRead(); // mark all as read depends
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void markNotificationsAsRead() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

class Admin extends User {
    private ArrayList<Donation> pendingDonations;
    private ArrayList<VolunteerWork> volunteerWorks;
    private ArrayList<VolunteerApplication> applications;
    private String flashMessage;

    public Admin(String username, String password, String location) {
        super(username, password, location);
        this.pendingDonations = new ArrayList<>();
        this.volunteerWorks = new ArrayList<>();
        this.applications = new ArrayList<>();
        this.flashMessage = "";
    }

    public void addVolunteerWork(String description) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO volunteer_works (description) VALUES (?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, description);
            pstmt.executeUpdate();
            System.out.println("Added new volunteer work: " + description);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    public void removeVolunteerWork(int index) {
        if (index >= 0 && index < volunteerWorks.size()) {
            VolunteerWork removed = volunteerWorks.remove(index);
            System.out.println("Removed volunteer work: " + removed.getDescription());
        } else {
            System.out.println("Invalid volunteer work index.");
        }
    }

    public void acceptDonation(Donation donation) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO donations (donor_name, type, details) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, donation.getDonorName());
            pstmt.setString(3, donation.getDetails());
            pstmt.executeUpdate();
            System.out.println("Accepted donation from " + donation.getDonorName() + ": " + donation.getDetails());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeDonation(int index) {
        if (index >= 0 && index < pendingDonations.size()) {
            Donation removed = pendingDonations.remove(index);
            // notif related
            User donor = findUserByUsername(removed.getDonorName());
            if (donor != null) {
                donor.addNotification("Your donation (" + removed.getDetails() + ") was removed by the admin.");
            }
            System.out.println("Removed donation from " + removed.getDonorName() + ": " + removed.getDetails());
        } else {
            System.out.println("Invalid donation index.");
        }
    }

    public void displayRecentDonations() {
        System.out.println("Recent Donations:");
        for (int i = 0; i < Math.min(10, pendingDonations.size()); i++) {
            Donation donation = pendingDonations.get(i);
            System.out.println(i + ". " + donation.getDonorName() + ": " + donation.getDetails());
        }
    }

    public ArrayList<VolunteerWork> getVolunteerWorks() {
        ArrayList<VolunteerWork> volunteerWorks = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM volunteer_works";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
    
            while (rs.next()) {
                String description = rs.getString("description");
                volunteerWorks.add(new VolunteerWork(description));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return volunteerWorks;
    }

    public String getFlashMessage() {
        return flashMessage;
    }

    public void setFlashMessage(String message) {
        this.flashMessage = message;
        System.out.println("Flash message updated.");
    }

    public void clearFlashMessage() {
        this.flashMessage = "";
        System.out.println("Flash message cleared.");
    }

    public void addApplication(VolunteerApplication application) {
        applications.add(application);
        System.out.println("Application received from " + application.getApplicantName() + " for work: " + application.getWorkDescription());
    }

    public void reviewApplications() {
        if (applications.isEmpty()) {
            System.out.println("No applications to review.");
            return;
        }

        System.out.println("Pending Volunteer Applications:");
        for (int i = 0; i < applications.size(); i++) {
            VolunteerApplication app = applications.get(i);
            System.out.println(i + ". " + app.getApplicantName() + " applied for: " + app.getWorkDescription() + " [Status: " + app.getStatus() + "]");
        }

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the index of the application to review or -1 to cancel: ");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index >= 0 && index < applications.size()) {
            VolunteerApplication app = applications.get(index);
            System.out.println("Reviewing application from " + app.getApplicantName() + " for " + app.getWorkDescription());
            System.out.println("Applicant's Skills: " + app.getApplicantSkills());
            System.out.println("Applicant's Preferred Causes: " + app.getApplicantPreferredCauses());
            System.out.println("1. Accept\n2. Decline\n3. Cancel");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            User applicant = findUserByUsername(app.getApplicantName());
            if (choice == 1) {
                app.setStatus("Accepted");
                System.out.println("Application accepted.");
                if (applicant != null) {
                    applicant.addNotification("Your application for volunteer work (" + app.getWorkDescription() + ") was accepted.");
                }
            } else if (choice == 2) {
                app.setStatus("Declined");
                System.out.println("Application declined.");
                if (applicant != null) {
                    applicant.addNotification("Your application for volunteer work (" + app.getWorkDescription() + ") was declined.");
                }
            } else {
                System.out.println("Cancelled.");
            }
        } else {
            System.out.println("No action taken.");
        }
    }

    private User findUserByUsername(String username) {
        for (User user : CompassionInAction.users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }
}

class Donation {
    private String donorName;
    private String type;
    private String details;

    public Donation(String donorName, String type, String details) {
        this.donorName = donorName;
        this.type = type;
        this.details = details;
    }

    public String getDonorName() {
        return donorName;
    }

    public String getDetails() {
        return type + " - " + details;
    }
}

class VolunteerWork {
    private String description;

    public VolunteerWork(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

class VolunteerApplication {
    private String applicantName;
    private String workDescription;
    private String applicantSkills;
    private String applicantPreferredCauses;
    private String status;

    public VolunteerApplication(String applicantName, String workDescription, String skills, String causes) {
        this.applicantName = applicantName;
        this.workDescription = workDescription;
        this.applicantSkills = skills;
        this.applicantPreferredCauses = causes;
        this.status = "Pending";
    }

    public String getApplicantName() {
        return applicantName;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public String getApplicantSkills() {
        return applicantSkills;
    }

    public String getApplicantPreferredCauses() {
        return applicantPreferredCauses;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

public class CompassionInAction {
    public static ArrayList<User> users = new ArrayList<>();
    private static Admin admin = new Admin("admin", "admin123", "Headquarters");

    public static void main(String[] args) {
        users.add(admin); // The admin is added for access~

        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Compassion in Action!");
        while (true) {
            System.out.println("\n1. Register\n2. Login\n3. Exit");
            System.out.print("Choose an option: ");
            
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid option. Try again.");
                scanner.next();
                continue;
            }

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    registerUser(scanner);
                    break;
                case 2:
                    loginUser(scanner);
                    break;
                case 3:
                    System.out.println("Thank you for using Compassion in Action!");
                    return;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void registerUser (Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter location: ");
        String location = scanner.nextLine();
    
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO users (username, password, location) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, location);
            pstmt.executeUpdate();
            System.out.println("User  registered successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

private static void loginUser (Scanner scanner) {
    System.out.print("Enter username: ");
    String username = scanner.nextLine();
    System.out.print("Enter password: ");
    String password = scanner.nextLine();

    try (Connection conn = DatabaseConnection.getConnection()) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            System.out.println("Welcome, " + username + "!");
            // Handle user menu or admin menu
        } else {
            System.out.println("Invalid username or password.");
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    private static void handleUserMenu(User user, Scanner scanner) {
        while (true) {
            if (!admin.getFlashMessage().isEmpty()) {
                System.out.println("** " + admin.getFlashMessage() + " **");
            }
    
            System.out.println("\nHello, please pick your desired help~");
            System.out.println("1. Donate\n2. Volunteer\n3. Recent Participation\n4. View Notifications\n5. Exit");
            System.out.print("Choose an option: ");
    
            
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1 and 5.");
                scanner.next();
                continue;
            }
    
            int choice = scanner.nextInt();
            scanner.nextLine();
    
            switch (choice) {
                case 1:
                    handleDonation(user, scanner);
                    break;
                case 2:
                    applyForVolunteerWork(user, scanner);
                    break;
                case 3:
                    admin.displayRecentDonations();
                    break;
                case 4:
                    user.viewNotifications();
                    break;
                case 5:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Please choose a valid menu item.");
            }
        }
    }

    private static void handleAdminMenu(Admin admin, Scanner scanner) {
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Manage Donations\n2. Add Volunteer Work\n3. Remove Volunteer Work\n4. Manage Flash Message\n5. Review Volunteer Applications\n6. Logout");
            System.out.print("Choose an option: ");
    
            
            if (!scanner.hasNextInt()) {
                System.out.println("Invalid input. Please enter a number between 1 and 6.");
                scanner.next();
                continue;
            }
    
            int choice = scanner.nextInt();
            scanner.nextLine();
    
            switch (choice) {
                case 1:
                    manageDonations(admin, scanner);
                    break;
                case 2:
                    System.out.print("Enter description of volunteer work: ");
                    String description = scanner.nextLine();
                    admin.addVolunteerWork(description);
                    break;
                case 3:
                    removeVolunteerWork(admin, scanner);
                    break;
                case 4:
                    manageFlashMessage(admin, scanner);
                    break;
                case 5:
                    admin.reviewApplications();
                    break;
                case 6:
                    System.out.println("Logging out...");
                    return;
                default:
                    System.out.println("Invalid option. Please choose a valid menu item.");
            }
        }
    }

    private static void manageDonations(Admin admin, Scanner scanner) {
        admin.displayRecentDonations();
        System.out.print("Enter the index of the donation to remove or -1 to cancel: ");
        int index = scanner.nextInt();
        scanner.nextLine();
        if (index != -1) {
            admin.removeDonation(index);
        }
    }

    private static void removeVolunteerWork(Admin admin, Scanner scanner) {
        ArrayList<VolunteerWork> volunteerWorks = admin.getVolunteerWorks();
        if (volunteerWorks.isEmpty()) {
            System.out.println("No volunteer works available to remove.");
            return;
        }

        System.out.println("Current Volunteer Works:");
        for (int i = 0; i < volunteerWorks.size(); i++) {
            System.out.println(i + ". " + volunteerWorks.get(i).getDescription());
        }

        System.out.print("Enter the index of the volunteer work to remove or -1 to cancel: ");
        int index = scanner.nextInt();
        scanner.nextLine();

        if (index != -1) {
            admin.removeVolunteerWork(index);
        }
    }

    private static void manageFlashMessage(Admin admin, Scanner scanner) {
        System.out.println("Current Flash Message: " + (admin.getFlashMessage().isEmpty() ? "None" : admin.getFlashMessage()));
        System.out.println("1. Set Flash Message\n2. Clear Flash Message\n3. Cancel");
        System.out.print("Choose an option: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                System.out.print("Enter new flash message: ");
                String message = scanner.nextLine();
                admin.setFlashMessage(message);
                break;
            case 2:
                admin.clearFlashMessage();
                break;
            case 3:
                System.out.println("Cancelled.");
                break;
            default:
                System.out.println("Invalid option. Try again.");
        }
    }

    private static void handleDonation(User user, Scanner scanner) {
        System.out.println("Enter the type of donation (e.g., Clothes, Food, Toiletries, Money, Other): ");
        String type = scanner.nextLine();
        System.out.println("Enter details of your donation: ");
        String details = scanner.nextLine();
        admin.acceptDonation(new Donation(user.getUsername(), type, details));
        System.out.println("Thank you for donating " + type + ": " + details);
    }

    private static void applyForVolunteerWork(User user, Scanner scanner) {
        System.out.println("Available Volunteer Opportunities:");
        ArrayList<VolunteerWork> volunteerWorks = admin.getVolunteerWorks();
        if (volunteerWorks.isEmpty()) {
            System.out.println("No available volunteer opportunities at the moment.");
            return;
        }

        for (int i = 0; i < volunteerWorks.size(); i++) {
            System.out.println(i + ". " + volunteerWorks.get(i).getDescription());
        }

        System.out.print("Enter the index of the work you'd like to apply for, or -1 to cancel: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice >= 0 && choice < volunteerWorks.size()) {
            VolunteerWork selectedWork = volunteerWorks.get(choice);
            VolunteerApplication application = new VolunteerApplication(
                user.getUsername(),
                selectedWork.getDescription(),
                user.getSkills(),
                user.getPreferredCauses()
            );
            admin.addApplication(application);
            System.out.println("Application submitted for volunteer work: " + selectedWork.getDescription());
        } else {
            System.out.println("Cancelled application.");
        }
    }
}