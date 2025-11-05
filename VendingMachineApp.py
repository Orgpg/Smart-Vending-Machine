# Smart Vending Machine (User Flow) - Python Console
# Features:
#  - Browse items with price & stock
#  - Add to Cart (validates stock)
#  - Checkout (cash only). Can add more cash or cancel.
#  - Dispense simulation + Receipt printing
#  - Stock auto-updates after purchase
# How to run:  python vending_machine.py

from collections import OrderedDict
from datetime import datetime
import uuid

class Item:
    def __init__(self, name: str, price: int, stock: int):
        self.name = name
        self.price = price  # kyats
        self.stock = stock

class Cart:
    def __init__(self):
        # keep insertion order for pretty receipt
        self.lines: "OrderedDict[Item,int]" = OrderedDict()

    def add(self, item: Item, qty: int):
        self.lines[item] = self.lines.get(item, 0) + qty

    def total(self) -> int:
        return sum(i.price * q for i, q in self.lines.items())

    def is_empty(self) -> bool:
        return len(self.lines) == 0

    def clear(self):
        self.lines.clear()

class VendingMachine:
    MACHINE_ID = "VM-102"
    LOCATION   = "Cafeteria Building A"

    def __init__(self):
        self.inventory: "OrderedDict[int, Item]" = OrderedDict()
        self.cart = Cart()
        self.seed_inventory()

    # ---------- Inventory & UI ----------
    def seed_inventory(self):
        # Drinks
        self.inventory[1] = Item("Coca-Cola", 2000, 10)
        self.inventory[2] = Item("Pepsi",     1800, 10)
        self.inventory[3] = Item("Sprite",    1500, 10)
        self.inventory[4] = Item("Water",     1000, 10)
        # Snacks
        self.inventory[5] = Item("Lays Chips",     1500, 8)
        self.inventory[6] = Item("Cookies",        1200, 6)
        self.inventory[7] = Item("Nutter Butter",  1300, 5)
        self.inventory[8] = Item("Kit Kat",        1500, 7)

    def show_menu(self):
        print("\n=========== Available Items ===========")
        print(f"{'No.':<4} {'Name':<18} {'Price(Ks)':>10} {'Stock':>7}")
        for no, it in self.inventory.items():
            print(f"{no:<4} {it.name:<18} {it.price:>10,} {it.stock:>7}")
        print("=======================================")
        if not self.cart.is_empty():
            print(f"Cart Total: {self.cart.total():,} Ks")
        print("(Type item number to add, C=Checkout, Q=Quit)")

    @staticmethod
    def ask_int(prompt: str, min_v: int, max_v: int) -> int:
        while True:
            s = input(prompt).strip()
            try:
                n = int(s)
                if n < min_v or n > max_v:
                    print(f"Please enter a number between {min_v} and {max_v}.")
                    continue
                return n
            except ValueError:
                print("Invalid number. Try again.")

    # ---------- Main Loop ----------
    def run(self):
        print("Welcome to Smart Vending Machine")
        print("Cash Payment Only | Type 'CANCEL' during payment to abort")

        while True:
            self.show_menu()
            choice = input("\nChoose: [number=add / C=Checkout / Q=Quit] : ").strip()

            if choice.lower() == "q":
                print("\nGoodbye! Thanks for visiting.")
                break

            if choice.lower() == "c":
                if self.cart.is_empty():
                    print("Cart is empty. Please add at least one item.")
                    continue
                if self.checkout():
                    again = input("\nBuy another item? (Y/N): ").strip()
                    if again.lower() != "y":
                        print("\nThank you! See you again.")
                        break
                continue

            # add to cart
            try:
                num = int(choice)
            except ValueError:
                print("Invalid choice. Try again.")
                continue

            item = self.inventory.get(num)
            if not item:
                print("Invalid selection number.")
                continue
            if item.stock <= 0:
                print(f"Out of stock: {item.name}")
                continue

            qty = self.ask_int(f"Enter quantity for {item.name}: ", 1, item.stock)
            self.cart.add(item, qty)
            print(f"Added to cart: {item.name} x{qty} ({item.price:,} Ks each)")
            input("Press Enter to continue...")

    # ---------- Checkout & Payment ----------
    def checkout(self) -> bool:
        print("\n================= CHECKOUT =================")
        self.print_cart()
        total = self.cart.total()
        print(f"TOTAL: {total:,} Ks")

        ok = input("Proceed to payment? (Y/N): ").strip()
        if ok.lower() != "y":
            print("Returning to menu...")
            return False

        paid = 0
        while paid < total:
            s = input(f"Insert cash (current paid {paid:,} / total {total:,}): ").strip()
            if s.lower() == "cancel":
                print("Transaction canceled. Returning to main menu.")
                return False
            try:
                add = int(s)
                if add <= 0:
                    print("Please insert a positive amount (or type CANCEL).")
                    continue
            except ValueError:
                print("Invalid amount. Type a number or 'CANCEL'.")
                continue

            paid += add
            if paid < total:
                print(f"Insufficient funds. Need {(total - paid):,} Ks more.")

        change = paid - total

        # Final stock validation
        for it, q in self.cart.lines.items():
            if it.stock < q:
                print(f"Sorry, '{it.name}' stock changed. Only {it.stock} left. Canceling.")
                return False

        # Dispense + stock update
        print("\nDispensing items...")
        for it, q in self.cart.lines.items():
            it.stock -= q
            print(f" - {it.name} x{q}")

        # Print receipt
        self.print_receipt(paid, change)

        # Clear cart
        self.cart.clear()
        return True

    def print_cart(self):
        print("-------------------------------------------")
        for it, q in self.cart.lines.items():
            line_total = it.price * q
            print(f"{it.name:<16} x{q:<3} {line_total:>10,} Ks")
        print("-------------------------------------------")

    def print_receipt(self, paid: int, change: int):
        total = sum(it.price * q for it, q in self.cart.lines.items())
        txn_id = str(uuid.uuid4())[:8].upper()
        now = datetime.now()
        date_str = now.strftime("%d-%b-%Y")
        time_str = now.strftime("%I:%M %p").lstrip("0")

        print("\n---------------------------------------")
        print("      University Vending Machine")
        print(f"      Machine ID: {self.MACHINE_ID}")
        print(f"      Location: {self.LOCATION}")
        print("---------------------------------------")
        print(f"Transaction ID: {txn_id}")
        print(f"Date: {date_str}")
        print(f"Time: {time_str}")
        print("---------------------------------------")
        print("Item               Qty    Price")
        for it, q in self.cart.lines.items():
            print(f"{it.name:<18} {q:<6} {it.price * q:>8,} Ks")
        print("---------------------------------------")
        print(f"Total:           {total:>12,} Ks")
        print(f"Payment:         {paid:>12,} Ks")
        print(f"Change:          {change:>12,} Ks")
        print("---------------------------------------")
        print("Thank you for your purchase!")
        print("Contact: 09-123456789")
        print("---------------------------------------")

if __name__ == "__main__":
    VendingMachine().run()
