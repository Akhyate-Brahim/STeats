package fr.unice.polytech.steats.order;

import fr.unice.polytech.steats.delivery.DeliveryRegistry;
import fr.unice.polytech.steats.exceptions.order.EmptyCartException;
import fr.unice.polytech.steats.exceptions.restaurant.DeliveryDateNotAvailable;
import fr.unice.polytech.steats.payment.PaymentManager;
import fr.unice.polytech.steats.delivery.DeliveryLocation;
import fr.unice.polytech.steats.exceptions.order.PaymentException;
import fr.unice.polytech.steats.restaurant.Menu;
import fr.unice.polytech.steats.restaurant.Restaurant;
import fr.unice.polytech.steats.restaurant.Schedule;
import fr.unice.polytech.steats.restaurant.Timeslot;
import fr.unice.polytech.steats.users.CampusUser;


import java.time.LocalDateTime;
import java.util.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public Order process(Restaurant restaurant, CampusUser customer, Map<Menu, Integer> menusOrdered,
                         LocalDateTime localDateTime, DeliveryLocation deliveryLocation)
            throws EmptyCartException, PaymentException, DeliveryDateNotAvailable {


        int menusNumber = menusOrdered.values().stream().mapToInt(Integer::intValue).sum();
        Timeslot timeSlot = calculateTimeslot(restaurant.getSchedule(), localDateTime, menusNumber).get();
        Map<Menu, Integer> menusOrderedCopy = menusOrdered.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> new Menu(entry.getKey()),  // Using the copy constructor
                        Map.Entry::getValue
                ));
        Order order = new Order(restaurant, customer, menusOrderedCopy, deliveryLocation, timeSlot);
        order.setStatus(OrderStatus.PREPARING);
        paymentManager.completePayment(customer);
        timeSlot.subtractCapacity(menusNumber);
        orderRepository.save(order, order.getId());
        deliveryRegistry.register(order);
        customer.getCart().emptyCart();
        return order;
    }
    public Optional<Timeslot> calculateTimeslot(Schedule schedule, LocalDateTime deliveryTime, int numberOfMenus)
            throws DeliveryDateNotAvailable, EmptyCartException {

        if (numberOfMenus == 0){
            throw new EmptyCartException();
        }

        LocalDateTime currentTimeSlotStart = deliveryTime.minusHours(2);
        while (currentTimeSlotStart.isAfter(LocalDateTime.of(deliveryTime.toLocalDate(), schedule.getOpeningTime()))) {
            Optional<Timeslot> foundTimeslot = schedule.findTimeSlotByStartTime(currentTimeSlotStart);
            if (foundTimeslot.isPresent()) {
                Timeslot timeslot = foundTimeslot.get();
                if (timeslot.getCapacity() >= numberOfMenus) {
                    return Optional.of(timeslot);
                }
            } else {
                Timeslot newTimeslot = new Timeslot(currentTimeSlotStart, schedule.getMaxCapacity() - numberOfMenus);
                schedule.getTimeSlots().add(newTimeslot);
                return Optional.of(newTimeslot);
            }
            currentTimeSlotStart = currentTimeSlotStart.minusMinutes(Schedule.SLOT_DURATION_IN_MINUTES);
        }
        throw new DeliveryDateNotAvailable(deliveryTime);
    }


    public List<Order> getPreviousOrders(CampusUser user) {
        List<Order> previousOrders = new ArrayList<>();
        for (Order order : orderRepository.findAll()) {
            if (order.getCustomer().equals(user)) {
                previousOrders.add(order);
            }
        }
        return previousOrders;
    }

    public List<Order> getOrdersWaitingForPreparation(Restaurant restaurant) {
        List<Order> previousOrders = new ArrayList<>();
        for (Order order : orderRepository.findAll()) {
            if (order.getRestaurant().equals(restaurant) && order.getStatus().equals(OrderStatus.WAITING_FOR_PREPARATION)) {
                previousOrders.add(order);
            }
        }
        return previousOrders;
    }
    public void MarkOrderAsReady(UUID id){
        Optional<Order> optionalOrder = orderRepository.findById(id);

        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setStatus(OrderStatus.READY_FOR_DELIVERY);
            orderRepository.save(order,order.getId());
        }
    }

    public DeliveryRegistry getDeliveryRegistry() {
        return deliveryRegistry;
    }
}
