package fr.unice.polytech.steats.steatspico.components;

import fr.unice.polytech.steats.steatspico.entities.delivery.DeliveryLocation;
import fr.unice.polytech.steats.steatspico.exceptions.order.NoOrdersPlacedException;
import fr.unice.polytech.steats.steatspico.entities.order.SimpleOrder;
import fr.unice.polytech.steats.steatspico.entities.order.OrderStatus;
import fr.unice.polytech.steats.steatspico.entities.order.OrderVolume;
import fr.unice.polytech.steats.steatspico.entities.restaurant.Restaurant;

import java.util.ArrayList;
import java.util.List;

public class StatisticsManager {

    final OrderVolume orderVolume;

    public StatisticsManager() {
        this.orderVolume = OrderVolume.getInstance();
    }

    public List<SimpleOrder> getOrderVolumesOverTime() throws NoOrdersPlacedException {
        if(orderVolume == null || orderVolume.getOrderVolume().isEmpty()) {
            throw new NoOrdersPlacedException();
        }
        for(SimpleOrder simpleOrder : orderVolume.getOrderVolume()) {
            if(simpleOrder.getStatus() == OrderStatus.DELIVERED) {
                orderVolume.getOrderVolume().remove(simpleOrder);
            }
        }

        return orderVolume.getOrderVolume();
    }

    public List<SimpleOrder> getRestaurantOrderVolume(Restaurant restaurant) throws NoOrdersPlacedException {
        if(orderVolume == null || orderVolume.getOrderVolume().isEmpty()) {
            throw new NoOrdersPlacedException();
        }
        List<SimpleOrder> restaurantSimpleOrderVolume = new ArrayList<>();
        for(SimpleOrder simpleOrder : orderVolume.getOrderVolume()) {
            if(simpleOrder.getStatus() == OrderStatus.DELIVERED) {
                orderVolume.getOrderVolume().remove(simpleOrder);
            }
            if(simpleOrder.getRestaurant() != null && simpleOrder.getRestaurant().equals(restaurant)){
                restaurantSimpleOrderVolume.add(simpleOrder);
            }
        }
        return restaurantSimpleOrderVolume;
    }

    public DeliveryLocation getDeliveryLocation(SimpleOrder simpleOrder1) throws NoOrdersPlacedException {
        if(orderVolume.getOrderVolume().contains(simpleOrder1) && !(simpleOrder1.getStatus().equals(OrderStatus.DELIVERED))){
            return simpleOrder1.getDeliveryLocation();
        }
        throw new NoOrdersPlacedException();
    }
}
