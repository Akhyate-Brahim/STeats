package fr.unice.polytech.steats.exceptions.order;

public class EmptyCartException extends Exception{
    public EmptyCartException(){
        super("Order cannot be created with 0 menus");
    }
}
