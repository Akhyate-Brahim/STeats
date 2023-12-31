package fr.unice.polytech.steats.unit.OrderTest;

import fr.unice.polytech.steats.steatspico.entities.order.Order;
import fr.unice.polytech.steats.steatspico.entities.order.OrderDetails;
import fr.unice.polytech.steats.steatspico.entities.order.OrderStatus;
import fr.unice.polytech.steats.steatspico.entities.order.SimpleOrder;
import fr.unice.polytech.steats.steatspico.entities.users.CampusUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class OrderTest {

    @Mock
    private OrderDetails orderDetails;

    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(orderDetails.getOrderOwner()).thenReturn(new CampusUser());
        when(orderDetails.getDeliveryTime()).thenReturn(LocalDateTime.now());

        order = new SimpleOrder(orderDetails);
    }

    @Test
    void testOrderInitialization() {
        assertNotNull(order.getId());
        assertEquals(OrderStatus.WAITING_FOR_PREPARATION, order.getStatus());
        assertNotNull(order.getOrderDetails());
        assertNotNull(order.getOrderDetails().getOrderOwner());
        assertNotNull(order.getOrderPublisher());
        assertEquals(1, order.getOrderPublisher().getObservers().size());
        assertEquals(orderDetails.getOrderOwner(), order.getOrderPublisher().getObservers().get(0));
    }

    @Test
    void testGetDeliveryTime() {
        assertEquals(orderDetails.getDeliveryTime(), order.getDeliveryTime());
    }

    @Test
    void testGetStatus() {
        assertEquals(OrderStatus.WAITING_FOR_PREPARATION, order.getStatus());
    }

    @Test
    void testSetStatus() {
        order.setStatus(OrderStatus.PREPARING);
        assertEquals(OrderStatus.PREPARING, order.getStatus());
    }

    @Test
    void testGetId() {
        assertNotNull(order.getId());
    }

    @Test
    void testGetCustomer() {
        assertEquals(orderDetails.getOrderOwner(), order.getCustomer());
    }


}
