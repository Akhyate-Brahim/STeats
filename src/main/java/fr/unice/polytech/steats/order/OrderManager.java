package fr.unice.polytech.steats.order;

import fr.unice.polytech.steats.delivery.DeliveryRegistry;
import fr.unice.polytech.steats.exceptions.order.EmptyCartException;
import fr.unice.polytech.steats.exceptions.restaurant.DeliveryDateNotAvailable;
import fr.unice.polytech.steats.notification.pickupTime.PickupTimePublisher;
import fr.unice.polytech.steats.order.factory.OrderFactory;
import fr.unice.polytech.steats.order.factory.SimpleOrderFactory;
import fr.unice.polytech.steats.payment.Payment;
import fr.unice.polytech.steats.delivery.DeliveryLocation;
import fr.unice.polytech.steats.exceptions.order.PaymentException;
import fr.unice.polytech.steats.restaurant.Menu;
import fr.unice.polytech.steats.restaurant.Restaurant;
import fr.unice.polytech.steats.restaurant.Schedule;
import fr.unice.polytech.steats.restaurant.TimeSlot;
import fr.unice.polytech.steats.users.CampusUser;
import java.time.LocalDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderManager implements OrderLocator, UserOrderHistory, OrderProcessing {

    private PickupTimePublisher pickupTimePublisher = new PickupTimePublisher();
    final Payment payment;
    final OrderRepository orderRepository;
    final DeliveryRegistry deliveryRegistry;
    SimpleOrderFactory simpleOrderFactory = new SimpleOrderFactory();

    public OrderManager(OrderRepository orderRepository, Payment payment, DeliveryRegistry deliveryRegistry) {
        this.orderRepository = orderRepository;
        this.payment = payment;
        this.deliveryRegistry = deliveryRegistry;
    }

    @Override
    public SimpleOrder process(OrderDetails orderDetails)
            throws EmptyCartException, PaymentException, DeliveryDateNotAvailable,NoSuchElementException {


        int menusNumber = orderDetails.menusOrdered().values().stream().mapToInt(Integer::intValue).sum();
        Optional<TimeSlot> optionalTimeSlot = calculateTimeslot(orderDetails.getRestaurant().getSchedule(), orderDetails.getDeliveryTime(), menusNumber);
        TimeSlot timeSlot = optionalTimeSlot.orElseThrow(() -> new NoSuchElementException("Element not found"));

        SimpleOrder order = simpleOrderFactory.createOrder(orderDetails);
        order.setStatus(OrderStatus.PREPARING);
        payment.completePayment(orderDetails.getOrderOwner());
        timeSlot.subtractCapacity(menusNumber);
        orderRepository.save(order, order.getId());
        deliveryRegistry.register(order);
        orderDetails.getOrderOwner().getCart().emptyCart();
        pickupTimePublisher.subscribe(order.getRestaurant());
        pickupTimePublisher.notifySubscribers(order);
        return order;
    }
    public Optional<TimeSlot> calculateTimeslot(Schedule schedule, LocalDateTime deliveryTime, int numberOfMenus)
            throws DeliveryDateNotAvailable, EmptyCartException {

        if (numberOfMenus == 0){
            throw new EmptyCartException();
        }

        LocalDateTime currentTimeSlotStart = deliveryTime.minusHours(2);
        while (currentTimeSlotStart.isAfter(LocalDateTime.of(deliveryTime.toLocalDate(), schedule.getOpeningTime()))) {
            Optional<TimeSlot> foundTimeslot = schedule.findTimeSlotByStartTime(currentTimeSlotStart);
            if (foundTimeslot.isPresent()) {
                TimeSlot timeslot = foundTimeslot.get();
                if (timeslot.getCapacity() >= numberOfMenus) {
                    return Optional.of(timeslot);
                }
            } else {
                TimeSlot newTimeslot = new TimeSlot(currentTimeSlotStart, schedule.getMaxCapacity() - numberOfMenus);
                schedule.getTimeSlots().add(newTimeslot);
                return Optional.of(newTimeslot);
            }
            currentTimeSlotStart = currentTimeSlotStart.minusMinutes(Schedule.SLOT_DURATION_IN_MINUTES);
        }
        throw new DeliveryDateNotAvailable(deliveryTime);
    }

    @Override
    public List<SimpleOrder> getPreviousOrders(CampusUser user) {
        List<SimpleOrder> previousOrders = new ArrayList<>();
        for (SimpleOrder order : orderRepository.findAll()) {
            if (order.getCustomer().equals(user)) {
                previousOrders.add(order);
            }
        }
        return previousOrders;
    }
    @Override
    public List<SimpleOrder> getOrdersByStatus(Restaurant restaurant, OrderStatus orderStatus) {
        List<SimpleOrder> previousOrders = new ArrayList<>();
        for (SimpleOrder order : orderRepository.findAll()) {
            if (order.getStatus()!=null && order.getRestaurant().equals(restaurant) && order.getStatus().equals(orderStatus)) {
                previousOrders.add(order);
            }
        }
        return previousOrders;
    }


    public DeliveryRegistry getDeliveryRegistry() {
        return deliveryRegistry;
    }
}