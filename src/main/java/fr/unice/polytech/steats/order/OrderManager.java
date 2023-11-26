package fr.unice.polytech.steats.order;

import fr.unice.polytech.steats.delivery.DeliveryRegistry;
import fr.unice.polytech.steats.exceptions.order.EmptyCartException;
import fr.unice.polytech.steats.exceptions.restaurant.DeliveryDateNotAvailable;
import fr.unice.polytech.steats.order.factory.SimpleOrderFactory;
import fr.unice.polytech.steats.payment.PaymentManager;
import fr.unice.polytech.steats.delivery.DeliveryLocation;
import fr.unice.polytech.steats.exceptions.order.PaymentException;
import fr.unice.polytech.steats.restaurant.Menu;
import fr.unice.polytech.steats.restaurant.Restaurant;
import fr.unice.polytech.steats.restaurant.TimeSlot;
import fr.unice.polytech.steats.users.CampusUser;


import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

public class OrderManager {
    PaymentManager paymentManager;
    OrderRepository orderRepository;
    DeliveryRegistry deliveryRegistry;

    public OrderManager(OrderRepository orderRepository, PaymentManager paymentManager, DeliveryRegistry deliveryRegistry) {
        this.orderRepository = orderRepository;
        this.paymentManager = paymentManager;
        this.deliveryRegistry = deliveryRegistry;
    }

    public SimpleOrder register(Restaurant restaurant, CampusUser customer, Map<Menu, Integer> menusOrdered,
                                LocalTime localTime, DeliveryLocation deliveryLocation)
            throws EmptyCartException, PaymentException, DeliveryDateNotAvailable {


        int menusNumber = menusOrdered.values().stream().mapToInt(Integer::intValue).sum();
        TimeSlot timeSlot = getTimeSlot(restaurant, localTime, menusNumber);
        Map<Menu, Integer> menusOrderedCopy = menusOrdered.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> new Menu(entry.getKey()),  // Using the copy constructor
                        Map.Entry::getValue
                ));
        SimpleOrderFactory factory= new SimpleOrderFactory(restaurant, customer, menusOrderedCopy, deliveryLocation, localTime);
        SimpleOrder simpleOrder = factory.createOrder();
        simpleOrder.setStatus(OrderStatus.PREPARING);
        paymentManager.completePayment(customer);
        timeSlot.subtractCapacity(menusNumber);
        orderRepository.save(simpleOrder, simpleOrder.getId());
        deliveryRegistry.register(simpleOrder);
        customer.getCart().emptyCart();
        return simpleOrder;
    }






    public TimeSlot getTimeSlot(Restaurant restaurant, LocalTime deliveryDate, int menusNumber)
            throws EmptyCartException, DeliveryDateNotAvailable {
        if (menusNumber == 0){

            throw new EmptyCartException();
        }
        if(restaurant.getSchedule().getTimeSlot(deliveryDate,menusNumber).isEmpty()){
            throw new DeliveryDateNotAvailable(deliveryDate);
        }
        return restaurant.getSchedule().getTimeSlot(deliveryDate,menusNumber).get();
    }

    public List<SimpleOrder> getPreviousOrders(CampusUser user) {
        List<SimpleOrder> previousSimpleOrders = new ArrayList<>();
        for (SimpleOrder simpleOrder : orderRepository.findAll()) {
            if (simpleOrder.getCustomer().equals(user)) {
                previousSimpleOrders.add(simpleOrder);
            }
        }
        return previousSimpleOrders;
    }

    public List<SimpleOrder> getOrdersWaitingForPreparation(Restaurant restaurant) {
        List<SimpleOrder> previousSimpleOrders = new ArrayList<>();
        for (SimpleOrder simpleOrder : orderRepository.findAll()) {
            if (simpleOrder.getStatus()!=null && simpleOrder.getRestaurant().equals(restaurant) && simpleOrder.getStatus().equals(OrderStatus.WAITING_FOR_PREPARATION)) {
                previousSimpleOrders.add(simpleOrder);
            }
        }
        return previousSimpleOrders;
    }


    public DeliveryRegistry getDeliveryRegistry() {
        return deliveryRegistry;
    }
}
