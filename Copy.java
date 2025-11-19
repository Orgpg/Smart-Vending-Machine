import java.util.Scanner;

public class Copy {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        // Display items
        System.out.println("=== Vending Machine ===");
        System.out.println("1. Coke  - 500 Kyats");
        System.out.println("2. Water - 300 Kyats");
        System.out.println("3. Juice - 700 Kyats");
        
        // Step 1: Get user choice
        System.out.print("Choose an item (1-3): ");
        int choice = input.nextInt();

        int price = 0;

        // Step 2: Set price based on choice
        if (choice == 1) {
            price = 500;
        } else if (choice == 2) {
            price = 300;
        } else if (choice == 3) {
            price = 700;
        } else {
            System.out.println("Invalid choice!");
            return;
        }

        // Step 3: Ask for money
        System.out.print("Insert money: ");
        int money = input.nextInt();

        // Step 4: Check if enough money
        if (money >= price) {
            int change = money - price;
            System.out.println("Dispensing your drink...");
            System.out.println("Your change: " + change + " Kyats");
        } else {
            System.out.println("Not enough money. Please try again.");
        }
    }
}