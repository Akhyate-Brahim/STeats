package fr.unice.polytech.steats.cart;

import fr.unice.polytech.steats.exceptions.cart.MenuRemovalFromCartException;
import fr.unice.polytech.steats.restaurant.Menu;
import fr.unice.polytech.steats.users.CampusUser;

import java.util.Map;

public class CartService {
    private Cart cart;

    public CartService(Cart cart) {
        this.cart = cart;
    }

    public Cart getCart() {
        return cart;
    }

    public void addItem(Menu menu, int quantity){
        int existingQuantity = cart.getMenuMap().getOrDefault(menu, 0);
        cart.getMenuMap().put(menu, existingQuantity + quantity);
    }

    public void removeItem(Menu menu, int quantityToRemove) throws MenuRemovalFromCartException {
        if (cart.getMenuMap().containsKey(menu)) {
            int existingQuantity = cart.getMenuMap().get(menu);

            if (existingQuantity > quantityToRemove) {
                cart.getMenuMap().put(menu, existingQuantity - quantityToRemove);
            } else if (existingQuantity == quantityToRemove) {
                cart.getMenuMap().remove(menu);
            } else {
                throw new MenuRemovalFromCartException();
            }
        }
    }

    public double getPriceForUser(CampusUser campusUser){
        double total = 0;
        for (Map.Entry<Menu, Integer> entry : cart.getMenuMap().entrySet()) {
            Menu menu = entry.getKey();
            int quantity = entry.getValue();
            if (menu.getCampusUserStatusPrice().containsKey(campusUser.getStatus())) {
                double priceForUser = menu.getCampusUserStatusPrice().get(campusUser.getStatus());
                total += priceForUser * quantity;
            } else {
                total += menu.getBasePrice() * quantity;
            }
        }
        return total;
    }
}
