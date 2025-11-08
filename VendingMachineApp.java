import java.util.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Smart Vending Machine (User Flow Only) - Java Console
 * Features:
 *  - Browse items (drinks/snacks) with price & stock
 *  - Add to Cart (validates stock)
 *  - Checkout (cash only). Supports adding more cash or cancel.
 *  - Dispense simulation + Receipt printing
 *  - Stock auto-updates after successful purchase
 *
 * How to run:
 *   javac VendingMachineApp.java
 *   java VendingMachineApp
 */
public class VendingMachineApp {

    // --- Domain Models ---
    static class Item {
        final String name;
        final int price;    // kyats
        int stock;

        Item(String name, int price, int stock) {
            this.name = name;
            this.price = price;
            this.stock = stock;
        }
    }

    static class Cart {
        // keep insertion order for pretty receipt
        private final LinkedHashMap<Item, Integer> lines = new LinkedHashMap<>();

        void add(Item item, int qty) {
            lines.put(item, lines.getOrDefault(item, 0) + qty);
        }

        int total() {
            int sum = 0;
            for (Map.Entry<Item, Integer> e : lines.entrySet()) {
                sum += e.getKey().price * e.getValue();
            }
            return sum;
        }

        boolean isEmpty() {
            return lines.isEmpty();
        }

        Set<Map.Entry<Item, Integer>> entries() {
            return lines.entrySet();
        }

        void clear() {
            lines.clear();
        }
    }

    // --- App State ---
    private final Map<Integer, Item> inventory = new LinkedHashMap<>(); // menu number -> item
    private final Cart cart = new Cart();
    private final Scanner in = new Scanner(System.in);

    // Metadata for receipt
    private static final String MACHINE_ID = "VM-102";
    private static final String LOCATION   = "Building 209, Time City Complex";

    public static void main(String[] args) {
        new VendingMachineApp().run();
    }

    private void run() {
        seedInventory();
        printlnHeader();

        mainLoop:
        while (true) {
            showMenu();
            System.out.print("\nChoose: [number=add / C=Checkout / Q=Quit] : ");
            String choice = in.nextLine().trim();

            if (choice.equalsIgnoreCase("Q")) {
                System.out.println("\nGoodbye! Thanks for visiting.");
                break;
            }
            if (choice.equalsIgnoreCase("C")) {
                if (cart.isEmpty()) {
                    System.out.println("Cart is empty. Please add at least one item.");
                    continue;
                }
                if (checkout()) {
                    // after successful checkout, ask to continue shopping
                    System.out.print("\nBuy another item? (Y/N): ");
                    String again = in.nextLine().trim();
                    if (!again.equalsIgnoreCase("Y")) {
                        System.out.println("\nThank you! See you again.");
                        break mainLoop;
                    }
                }
                continue;
            }

            // Add to cart by number
            int num;
            try {
                num = Integer.parseInt(choice);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice. Try again.");
                continue;
            }
            Item item = inventory.get(num);
            if (item == null) {
                System.out.println("Invalid selection number.");
                continue;
            }
            if (item.stock <= 0) {
                System.out.println("Out of stock: " + item.name);
                continue;
            }

            int qty = askInt("Enter quantity for " + item.name + ": ", 1, item.stock);
            cart.add(item, qty);
            System.out.printf("Added to cart: %s x%d (%,d Ks each)%n", item.name, qty, item.price);

           System.out.print("Add more items? (Y to continue adding, anything else to return menu): ");
            String more = in.nextLine().trim();
            if (!more.equalsIgnoreCase("Y")) {
                // return to main menu
                continue;
            }

        }
    }

    // --- Checkout & Payment Flow ---
    private boolean checkout() {
        // Show cart summary
        System.out.println("\n================= CHECKOUT =================");
        printCart();
        int total = cart.total();
        System.out.printf("TOTAL: %,d Ks%n", total);

        // Confirm
        System.out.print("Proceed to payment? (Y/N): ");
        String ok = in.nextLine().trim();
        if (!ok.equalsIgnoreCase("Y")) {
            System.out.println("Returning to menu...");
            return false;
        }

        // Payment loop: allow adding more cash or cancel
        int paid = 0;
        while (paid < total) {
            System.out.printf("Insert cash (current paid %,d / total %,d): ", paid, total);
            String s = in.nextLine().trim();

            if (s.equalsIgnoreCase("CANCEL")) {
                System.out.println("Transaction canceled. Returning to main menu.");
                return false;
            }

            int add;
            try {
                add = Integer.parseInt(s);
                if (add <= 0) {
                    System.out.println("Please insert a positive amount (or type CANCEL).");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Type a number or 'CANCEL'.");
                continue;
            }

            paid += add;
            if (paid < total) {
                System.out.printf("Insufficient funds. Need %,d Ks more.%n", (total - paid));
                System.out.print("Add more cash or type CANCEL to abort.\n");
            }
        }

        int change = paid - total;

        // Validate stock before finalizing (in case stock changed meanwhile)
        for (Map.Entry<Item, Integer> e : cart.entries()) {
            Item it = e.getKey();
            int q = e.getValue();
            if (it.stock < q) {
                System.out.printf("Sorry, '%s' stock changed. Only %d left. Transaction canceled.%n", it.name, it.stock);
                return false;
            }
        }

        // Dispense + Update Stock
        System.out.println("\nDispensing items...");
        for (Map.Entry<Item, Integer> e : cart.entries()) {
            Item it = e.getKey();
            int q = e.getValue();
            it.stock -= q;
            System.out.printf(" - %s x%d%n", it.name, q);
        }

        // Print Receipt
        printReceipt(paid, change);

        // Clear cart after purchase
        cart.clear();

        return true;
    }

    private void printCart() {
        System.out.println("-------------------------------------------");
        for (Map.Entry<Item, Integer> e : cart.entries()) {
            Item it = e.getKey();
            int q = e.getValue();
            System.out.printf("%-16s x%-3d  %,8d Ks%n", it.name, q, it.price * q);
        }
        System.out.println("-------------------------------------------");
    }

    private void printReceipt(int paid, int change) {
        int total = 0;
        for (Map.Entry<Item, Integer> e : cart.entries()) {
            total += e.getKey().price * e.getValue();
        }

        String txnId = genTxnId();
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-uuuu"));
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("h:mm a"));
        System.out.println("\n---------------------------------------");
        System.out.println("      Gusto Vending Machine");
        System.out.println("      Machine ID: " + MACHINE_ID);
        System.out.println("      Location: " + LOCATION);
        System.out.println("---------------------------------------");
        System.out.println("Transaction ID: " + txnId);
        System.out.println("Date: " + date);
        System.out.println("Time: " + time);
        System.out.println("---------------------------------------");
        System.out.println("Item               Qty    Price");
        for (Map.Entry<Item, Integer> e : cart.entries()) {
            Item it = e.getKey();
            int q = e.getValue();
            System.out.printf("%-18s %-6d %,8d Ks%n", it.name, q, it.price * q);
        }
        System.out.println("---------------------------------------");
        System.out.printf("Total:           %,12d Ks%n", total);
        System.out.printf("Payment:         %,12d Ks%n", paid);
        System.out.printf("Change:          %,12d Ks%n", change);
        System.out.println("---------------------------------------");
        System.out.println("Thank you for your purchase!");
        System.out.println("Contact: 09-123456789");
        System.out.println("---------------------------------------");
    }

    private static String genTxnId() {
        // Short txn id from UUID
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // --- Menu & Utility ---
    private void showMenu() {
        System.out.println("\n=========== Available Items ===========");
        System.out.printf("%-4s %-18s %-10s %-8s%n", "No.", "Name", "Price(Ks)", "Stock");
        int i = 1;
        for (Item it : inventory.values()) {
            System.out.printf("%-4d %-18s %,10d %-8d%n", i, it.name, it.price, it.stock);
            i++;
        }
        System.out.println("=======================================");
        if (!cart.isEmpty()) {
            System.out.printf("Cart Total: %,d Ks%n", cart.total());
        }
        System.out.println("(Type item number to add, C=Checkout, Q=Quit)");
    }

private void printlnHeader() {
    // Use dividers for better visual separation
        System.out.println("-------------------------------------");

        // Center the main title and use capital letters for emphasis
        System.out.println("** Wellcome to GUSTO COLLEGE VENDING MACHINE **");
        
        // Use dividers for better visual separation
        System.out.println("------------------------------------------------------------------");
        
        // Display the warning message clearly with an icon
        System.out.println("Cash Payment Only | Type 'CANCEL' during payment to abort");
        
        System.out.println("------------------------------------------------------------------");
    }

    private void seedInventory() {
        // Drinks
        inventory.put(1, new Item("Coca-Cola", 2000, 10));
        inventory.put(2, new Item("Pepsi",     1800, 10));
        inventory.put(3, new Item("Sprite",    1500, 10));
        inventory.put(4, new Item("Water",     1000, 10));
        // Snacks
        inventory.put(5, new Item("Lays Chips", 1500, 8));
        inventory.put(6, new Item("Cookies",    1200, 6));
        inventory.put(7, new Item("Nutter Butter", 1300, 5));
        inventory.put(8, new Item("Kit Kat",    1500, 7));
    }

    private int askInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                int n = Integer.parseInt(s);
                if (n < min || n > max) {
                    System.out.printf("Please enter a number between %d and %d.%n", min, max);
                    continue;
                }
                return n;
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }
}
