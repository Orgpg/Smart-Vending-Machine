import java.util.*;

/**
 * Smart Vending Machine - Admin Panel (Commented Version)
 * Implements all steps from Admin Algorithm (Step 1–14)
 */
public class AdminPanelApp {

    // ------------------------------------------
    // Item Class (Used to store item information)
    // ------------------------------------------
    static class Item {
        String name;
        int price;
        int quantity;

        Item(String name, int price, int quantity) {
            this.name = name;
            this.price = price;
            this.quantity = quantity;
        }
    }

    // Inventory Database (in-memory)
    private final Map<String, Item> inventory = new LinkedHashMap<>();
    private final Scanner in = new Scanner(System.in);

    // Admin Credentials
    private final String adminUsername = "admin";
    private final String adminPassword = "1234";


    // =====================================================
    // (STEP 1) Program Start → Go to Admin Login
    // =====================================================
    public static void main(String[] args) {
        new AdminPanelApp().run();
    }


    // =====================================================
    // (STEP 2 & 3 & 4) Admin Login Flow
    // =====================================================
    private void run() {
        System.out.println("===== SMART VENDING MACHINE - ADMIN LOGIN =====");

        while (true) {
            // Ask Admin to enter username & password
            System.out.print("Username: ");
            String username = in.nextLine().trim();

            System.out.print("Password: ");
            String password = in.nextLine().trim();

            // STEP 4: Validate Login
            if (username.equals(adminUsername) && password.equals(adminPassword)) {

                // Login Success
                System.out.println("\nLogin Successful");
                showMenu();  // Go to Admin Menu (STEP 5)
                break;

            } else {
                // Invalid Login → Return to login page
                System.out.println("Access Denied. Try again.\n");
            }
        }
    }


    // =====================================================
    // (STEP 5) Display Admin Menu
    // =====================================================
    private void showMenu() {

        // Load sample stock into inventory
        seedInitialStock();

        while (true) {

            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. View Stock");
            System.out.println("2. Add New Item");
            System.out.println("3. Update Price");
            System.out.println("4. Refill Stock");
            System.out.println("5. Remove Item");
            System.out.println("6. Logout");

            // STEP 6: Ask admin to select an option
            System.out.print("Select an option (1-6): ");
            String choice = in.nextLine().trim();


            // =====================================================
            // (STEP 7) Perform Selected Task
            // =====================================================
            switch (choice) {
                case "1":
                    viewStock();   // View Stock
                    break;

                case "2":
                    addNewItem(); // Add Item
                    break;

                case "3":
                    updatePrice(); // Update Price
                    break;

                case "4":
                    refillStock(); // Refill Stock
                    break;

                case "5":
                    removeItem(); // Remove Item
                    break;

                case "6":
                    saveAndLogout(); // Logout (STEP 12–14)
                    return;

                default:
                    System.out.println("Invalid option. Try again.");
            }


            // =====================================================
            // (STEP 9) Ask “Perform another operation?”
            // =====================================================
            System.out.print("\nPerform another operation? (Y/N): ");
            String again = in.nextLine().trim();

            if (!again.equalsIgnoreCase("Y")) {
                saveAndLogout();  // Go to Step 10, 11, 12, 13, 14
                break;
            }
        }
    }


    // =====================================================
    // (OPTION A) View Stock
    // =====================================================
    private void viewStock() {
        System.out.println("\nCurrent Stock List:");
        System.out.printf("%-20s %-10s %-10s%n", "Item Name", "Price(Ks)", "Quantity");
        System.out.println("---------------------------------------------");

        // Display each item’s details
        for (Item item : inventory.values()) {
            System.out.printf("%-20s %-10d %-10d%n",
                    item.name, item.price, item.quantity);
        }
    }


    // =====================================================
    // (OPTION B) Add New Item
    // =====================================================
    private void addNewItem() {

        // Admin inputs new item details
        System.out.print("Enter new item name: ");
        String name = in.nextLine().trim();

        // Check if item already exists
        if (inventory.containsKey(name)) {
            System.out.println("Item already exists.");
            return;
        }

        int price = askInt("Enter price (Ks): ");
        int qty = askInt("Enter quantity: ");

        // Add to inventory
        inventory.put(name, new Item(name, price, qty));
        System.out.println("Item added successfully.");
    }


    // =====================================================
    // (OPTION C) Update Price
    // =====================================================
    private void updatePrice() {
        System.out.print("Enter item name to update price: ");
        String name = in.nextLine().trim();

        Item item = inventory.get(name);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        int newPrice = askInt("Enter new price (Ks): ");
        item.price = newPrice;

        System.out.println("Price updated successfully.");
    }


    // =====================================================
    // (OPTION D) Refill Stock
    // =====================================================
    private void refillStock() {
        System.out.print("Enter item name to refill: ");
        String name = in.nextLine().trim();

        Item item = inventory.get(name);

        if (item == null) {
            System.out.println("Item not found.");
            return;
        }

        int addQty = askInt("Enter quantity to add: ");
        item.quantity += addQty;

        System.out.println("Stock refilled successfully.");
    }


    // =====================================================
    // (OPTION E) Remove Item
    // =====================================================
    private void removeItem() {
        System.out.print("Enter item name to remove: ");
        String name = in.nextLine().trim();

        if (inventory.remove(name) != null) {
            System.out.println("Item removed successfully.");
        } else {
            System.out.println("Item not found.");
        }
    }


    // =====================================================
    // (STEP 10–14) Save Changes & Logout
    // =====================================================
    private void saveAndLogout() {
        System.out.println("\nSaving changes to database...");
        System.out.println("All changes saved successfully.");
        System.out.println("Logging out...");
        System.out.println("Goodbye.");
    }


    // -----------------------------------------------------
    // Helper: Input number safely
    // -----------------------------------------------------
    private int askInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int val = Integer.parseInt(in.nextLine().trim());
                if (val < 0) throw new NumberFormatException();
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid positive number.");
            }
        }
    }


    // -----------------------------------------------------
    // Sample data for testing
    // -----------------------------------------------------
    private void seedInitialStock() {
        inventory.put("Coca-Cola", new Item("Coca-Cola", 2000, 10));
        inventory.put("Pepsi", new Item("Pepsi", 1800, 8));
        inventory.put("Sprite", new Item("Sprite", 1500, 6));
        inventory.put("Lays Chips", new Item("Lays Chips", 1500, 5));
    }
}
