package fr.unice.polytech.steats.cucumber;

import fr.unice.polytech.steats.cucumber.ordering.FacadeContainer;
import fr.unice.polytech.steats.delivery.DeliveryLocation;
import fr.unice.polytech.steats.order.*;
import fr.unice.polytech.steats.order.factory.SimpleOrderFactory;
import fr.unice.polytech.steats.restaurant.Menu;
import fr.unice.polytech.steats.restaurant.Restaurant;
import fr.unice.polytech.steats.users.CampusUser;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static fr.unice.polytech.steats.order.OrderStatus.PREPARING;
import static fr.unice.polytech.steats.order.OrderStatus.WAITING_FOR_PREPARATION;
import static fr.unice.polytech.steats.users.CampusUserStatus.STAFF;
import static org.junit.Assert.assertEquals;

public class GetOrdersWaitingForPreparationSteps {
    Restaurant restaurant;
    CampusUser staff;
    final OrderRepository orderRepository;
    final OrderLocator orderLocator;
    List<SimpleOrder> ordersWaitingForPreparation;

    public GetOrdersWaitingForPreparationSteps(FacadeContainer container) {
        orderLocator = container.orderLocator;
        orderRepository = container.orderRepository;
    }

    @Given("a restaurant staff {string} working at {string}")
    public void a_restaurant_staff_working_at(String staffName, String restaurantName) {
        restaurant = new Restaurant(restaurantName);
        staff = new CampusUser(staffName,STAFF);
    }
    @Given("the restaurant has {int} orders waiting for preparation")
    public void a_restaurant_with_orders_waiting_for_preparation(int ordersNumber) {
        OrderDetailsBuilder builder = new OrderDetailsBuilder()
                .restaurant(restaurant)
                .orderOwner(new CampusUser("lambda"))
                .deliveryTime(LocalDateTime.now())
                .deliveryLocation(DeliveryLocation.LIBRARY);
        OrderDetails orderDetails1 = builder.build();
        OrderDetails orderDetails2 = builder.orderOwner(new CampusUser("other")).build();
        for(int i = 0; i < ordersNumber; i++){

            SimpleOrder simpleOrder = new SimpleOrder(orderDetails1);
            simpleOrder.setStatus(WAITING_FOR_PREPARATION);
            orderRepository.save(simpleOrder, simpleOrder.getId());
        }
        for(int i = 0; i < 6; i++){
            LocalDateTime orderDate = LocalDateTime.now();
            SimpleOrder simpleOrder = new SimpleOrder(orderDetails2);
            simpleOrder.setStatus(PREPARING);
            orderRepository.save(simpleOrder, simpleOrder.getId());
        }
    }
    @When("the restaurant staff Karim clicks on get orders waiting for preparation")
    public void the_restaurant_staff_karim_clicks_on_get_orders_waiting_for_preparation() {
        ordersWaitingForPreparation = orderLocator.getOrdersByStatus(restaurant, WAITING_FOR_PREPARATION);
    }
    @Then("he should get a list of {int} orders waiting for preparation")
    public void he_should_get_a_list_of_orders_waiting_for_preparation(int ordersNumber) {
        assertEquals(ordersNumber, ordersWaitingForPreparation.size());
    }

}
