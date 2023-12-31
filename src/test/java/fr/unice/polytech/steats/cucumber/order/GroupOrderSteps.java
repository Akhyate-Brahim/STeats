package fr.unice.polytech.steats.cucumber.order;
import fr.unice.polytech.steats.cucumber.picosetup.FacadeContainer;
import fr.unice.polytech.steats.steatspico.components.CartHandler;
import fr.unice.polytech.steats.steatspico.entities.delivery.DeliveryLocation;
import fr.unice.polytech.steats.steatspico.exceptions.order.ClosedGroupOrderException;
import fr.unice.polytech.steats.steatspico.exceptions.order.EmptyCartException;
import fr.unice.polytech.steats.steatspico.exceptions.order.NonExistentGroupOrder;
import fr.unice.polytech.steats.steatspico.exceptions.order.PaymentException;
import fr.unice.polytech.steats.steatspico.exceptions.others.NoSuchElementException;
import fr.unice.polytech.steats.steatspico.exceptions.restaurant.DeliveryDateNotAvailable;
import fr.unice.polytech.steats.steatspico.exceptions.restaurant.InsufficientTimeSlotCapacity;
import fr.unice.polytech.steats.steatspico.exceptions.restaurant.NonExistentMenuException;
import fr.unice.polytech.steats.steatspico.entities.order.OrderDetails;
import fr.unice.polytech.steats.steatspico.entities.order.OrderDetailsBuilder;
import fr.unice.polytech.steats.steatspico.entities.order.SimpleOrder;
import fr.unice.polytech.steats.steatspico.entities.restaurant.Menu;
import fr.unice.polytech.steats.steatspico.entities.restaurant.Restaurant;
import fr.unice.polytech.steats.steatspico.interfaces.restaurant.RestaurantLocator;
import fr.unice.polytech.steats.steatspico.entities.order.GroupOrder;
import fr.unice.polytech.steats.steatspico.interfaces.order.GroupOrderFinder;
import fr.unice.polytech.steats.steatspico.interfaces.order.GroupOrderRegistration;
import fr.unice.polytech.steats.steatspico.interfaces.order.SubOrderManager;
import fr.unice.polytech.steats.steatspico.entities.users.CampusUser;
import fr.unice.polytech.steats.steatspico.interfaces.users.CampusUserFinder;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.Assert.*;

public class GroupOrderSteps {
    GroupOrder groupOrder;
    String groupOrderCode;
    CampusUser campusUser;
    Restaurant restaurant;
    final CampusUserFinder campusUserFinder;
    final RestaurantLocator restaurantLocator;
    final GroupOrderFinder groupOrderFinder;
    final GroupOrderRegistration groupOrderRegistration;
    final SubOrderManager subOrderManager;
    DeliveryLocation deliveryLocation;
    public GroupOrderSteps(FacadeContainer container){
        campusUserFinder = container.campusUserRegistry;
        restaurantLocator = container.restaurantLocator;
        groupOrderFinder = container.groupOrderFinder;
        groupOrderRegistration = container.groupOrderRegistration;
        subOrderManager = container.subOrderManager;
    }


    @And("a group order exists with the code {string} of user {string} with restaurant {string}")
    public void aGroupOrderExistsWithTheCodeOfUser(String groupOrderString, String campusUserName, String restaurantName) throws NoSuchElementException {
        groupOrderCode = groupOrderString;
        campusUser = campusUserFinder.findByName(campusUserName).orElseThrow(() -> new NoSuchElementException("Element not found"));
        restaurant = restaurantLocator.findByName(restaurantName).orElseThrow(() -> new NoSuchElementException("Element not found"));
    }

    @And("group order {string} is set with delivery time {string} and location {string}")
    public void groupOrderIsSetWithTimeslotAndLocation(String groupOrderCode, String dateTimeString, String locationString) {
        LocalDateTime deliveryDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        deliveryLocation = DeliveryLocation.getByName(locationString);
        OrderDetails orderDetails = new OrderDetailsBuilder()
                .orderOwner(campusUser)
                .deliveryTime(deliveryDateTime)
                .deliveryLocation(deliveryLocation)
                .build();
        groupOrder = groupOrderRegistration.register(orderDetails);
        groupOrder.setGroupOrderCode(groupOrderCode);
    }


    @When("{string} requests to create a group order")
    public void requestsToCreateAGroupOrder(String user) throws NoSuchElementException {
        campusUser = campusUserFinder.findByName(user).orElseThrow(() -> new NoSuchElementException("Element not found"));
    }

    @Then("a group order is created with a unique code")
    public void aGroupOrderIsCreatedWithAUniqueCode() {
        OrderDetails orderDetails = new OrderDetailsBuilder()
                .orderOwner(campusUser)
                .deliveryTime(LocalDateTime.now())
                .deliveryLocation(deliveryLocation)
                .build();
        groupOrderRegistration.register(orderDetails);
    }

    @And("the group order is in {string} status")
    public void theGroupOrderIsInStatus(String status) {
        boolean isOpen = status.equals("open");
        assertEquals(isOpen, groupOrder.isOpen());
    }

    @When("{string} joins the group order {string}")
    public void joinsTheGroupOrder(String userName, String groupOrderCode) throws NoSuchElementException {
        groupOrder = groupOrderFinder.findByCode(groupOrderCode).orElseThrow(() -> new NoSuchElementException("Element not found"));
        campusUser = campusUserFinder.findByName(userName).orElseThrow(() -> new NoSuchElementException("Element not found"));
    }

    @And("{string} orders and pays for {int} x {string}")
    public void ordersAndPaysForX(String userName, int quantity, String menuName) throws EmptyCartException, PaymentException, InsufficientTimeSlotCapacity, NonExistentGroupOrder, ClosedGroupOrderException, DeliveryDateNotAvailable, NoSuchElementException, NonExistentMenuException {
        campusUser = campusUserFinder.findByName(userName).orElseThrow(() -> new NoSuchElementException("Element not found"));
        Menu menu = restaurant.getMenufromName(menuName);
        CartHandler cartHandler = new CartHandler(campusUser.getCart());
        cartHandler.addItem(restaurant, menu, quantity);
        OrderDetails orderDetails = new OrderDetailsBuilder()
                .restaurant(restaurant)
                .orderOwner(campusUser)
                .deliveryTime(groupOrder.getDeliveryTime())
                .deliveryLocation(groupOrder.getDeliveryLocation())
                .build();
        subOrderManager.addSubOrder(groupOrderCode, orderDetails);
    }

    @And("{string}'s order should be set with delivery time {string} and location {string}")
    public void sOrderShouldBeSetWithTimeslotAndLocation(String username, String dateTimeString, String delivLocation) throws NoSuchElementException {
        campusUser = campusUserFinder.findByName(username).orElseThrow(() -> new NoSuchElementException("Element not found"));
        LocalDateTime timeslotDateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        DeliveryLocation deliveryLocation = DeliveryLocation.getByName(delivLocation);
        SimpleOrder order = groupOrder.getSubOrders().get(0);
        assertEquals(timeslotDateTime, order.getDeliveryTime());
        assertEquals(order.getDeliveryLocation(), deliveryLocation);
    }


    @And("group order {string} should have {int} order")
    public void groupOrderShouldHaveOneOrder(String groupOrderCode, int groupOrderSize) throws NoSuchElementException {
        groupOrder = groupOrderFinder.findByCode(groupOrderCode).orElseThrow(() -> new NoSuchElementException("Element not found"));
        assertEquals(groupOrderSize, groupOrder.getSize());
    }

    @Then("the price of {string}'s order is {double}")
    public void thePriceOfSOrderIs(String username, double price) throws NoSuchElementException {
        campusUser = campusUserFinder.findByName(username).orElseThrow(() -> new NoSuchElementException("Element not found"));
        SimpleOrder order = subOrderManager.locateSubOrder(groupOrder, campusUser).orElseThrow(() -> new NoSuchElementException("Element not found"));
        assertEquals(order.getPrice(), price, 0.1);

    }
    @When("{string} closes the group order")
    public void requestsToCloseTheGroupOrder(String username) throws NoSuchElementException {
        campusUser = campusUserFinder.findByName(username).orElseThrow(() -> new NoSuchElementException("Element not found"));
        groupOrder.closeGroupOrder();
    }
}