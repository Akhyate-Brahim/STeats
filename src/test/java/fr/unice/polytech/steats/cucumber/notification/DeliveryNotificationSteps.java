package fr.unice.polytech.steats.cucumber.notification;

import fr.unice.polytech.steats.cucumber.picosetup.FacadeContainer;
import fr.unice.polytech.steats.steatspico.exceptions.order.EmptyCartException;
import fr.unice.polytech.steats.steatspico.exceptions.order.PaymentException;
import fr.unice.polytech.steats.steatspico.exceptions.restaurant.DeliveryDateNotAvailable;
import fr.unice.polytech.steats.steatspico.entities.order.OrderDetails;
import fr.unice.polytech.steats.steatspico.entities.order.OrderDetailsBuilder;
import fr.unice.polytech.steats.steatspico.components.OrderManager;
import fr.unice.polytech.steats.steatspico.entities.order.SimpleOrder;
import fr.unice.polytech.steats.steatspico.components.orderprocessingstrategy.SimpleOrderProcessingStrategy;
import fr.unice.polytech.steats.steatspico.entities.restaurant.Restaurant;
import fr.unice.polytech.steats.steatspico.entities.delivery.Delivery;
import fr.unice.polytech.steats.steatspico.components.DeliveryRegistry;
import fr.unice.polytech.steats.steatspico.entities.delivery.DeliveryStatus;
import fr.unice.polytech.steats.steatspico.entities.users.CampusUser;
import fr.unice.polytech.steats.steatspico.entities.users.DeliveryPerson;
import io.cucumber.java.en.*;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static fr.unice.polytech.steats.steatspico.entities.delivery.DeliveryLocation.LIBRARY;
import static fr.unice.polytech.steats.steatspico.entities.delivery.DeliveryStatus.IN_PROGRESS;
import static org.junit.jupiter.api.Assertions.*;
public class DeliveryNotificationSteps {

    Delivery delivery;
    CampusUser campusUser;
    DeliveryPerson deliveryPerson;

    OrderManager orderManager;

    SimpleOrder order;

    DeliveryRegistry deliveryRegistry;
    SimpleOrderProcessingStrategy simpleOrderProcessingStrategy;


    public DeliveryNotificationSteps(FacadeContainer container) {
        orderManager = container.orderManager;
        deliveryRegistry = orderManager.getDeliveryRegistry();
        simpleOrderProcessingStrategy = container.simpleOrderProcessingStrategy;
    }

    @Given("a user named {string}")
    public void a_user(String name) {
        campusUser = new CampusUser(name);
    }

    @Given("{string} a delivery person")
    public void a_delivery_person(String name) {
        deliveryPerson = new DeliveryPerson(name);
        deliveryPerson.setPhoneNumber("789456123");

    }
    @Given("a delivery with the status WAITING")
    public void a_delivery_with_the_status_waiting() throws EmptyCartException, PaymentException, DeliveryDateNotAvailable {
        orderManager.setOrderProcessingStrategy(simpleOrderProcessingStrategy);
        OrderDetails orderDetails = new OrderDetailsBuilder()
                .restaurant(new Restaurant("R1"))
                .orderOwner(campusUser)
                .deliveryTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(12, 0)))
                .deliveryLocation(LIBRARY)
                .build();
        order = (SimpleOrder)orderManager.process(orderDetails);
        delivery = new Delivery(order);
        deliveryRegistry.getDeliveryRepository().save(delivery, delivery.getId());
    }


    @When("the delivery status is set as IN_PROGRESS")
    public void the_delivery_status_is_set_as_in_progress() {
        delivery.setDeliveryPerson(deliveryPerson);
        delivery.setStatus(IN_PROGRESS);
    }

    @Then("a notification is sent to the delivery person and the campus user")
    public void a_notification_is_sent_to_the_delivery_person_and_the_campus_user() {
        assertEquals(delivery.getStatus(), DeliveryStatus.IN_PROGRESS);
        assertEquals(delivery.getDeliveryPublisher().getObservers().size(),1 );
        assertTrue(delivery.getDeliveryPublisher().getObservers().contains(deliveryPerson));
    }
}
