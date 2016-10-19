package io.swagger.api;

import feign.FeignException;
import io.swagger.TestUtils;
import io.swagger.configuration.ClientConfiguration;
import io.swagger.model.Order;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.threeten.bp.OffsetDateTime;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = StoreApiTest.Application.class)
public class StoreApiTest {


    @Autowired
    private StoreApiClient client;

    @Test
    public void testGetInventory() {
        Map<String, Integer> inventory = client.getInventory().getBody();
        assertTrue(inventory.keySet().size() > 0);
    }

    @Test
    public void testPlaceOrder() {
        Order order = createOrder();
        client.placeOrder(order);

        Order fetched = client.getOrderById(order.getId()).getBody();
        assertEquals(order.getId(), fetched.getId());
        assertEquals(order.getPetId(), fetched.getPetId());
        assertEquals(order.getQuantity(), fetched.getQuantity());
        assertEquals(order.getShipDate().toInstant(), fetched.getShipDate().toInstant());
    }

    @Test
    public void testDeleteOrder() {
        Order order = createOrder();
        client.placeOrder(order);

        Order fetched = client.getOrderById(order.getId()).getBody();
        assertEquals(fetched.getId(), order.getId());

        client.deleteOrder(String.valueOf(order.getId()));

        try {
            client.getOrderById(order.getId());
            fail("expected an error");
        } catch (FeignException e) {
            assertTrue(e.getMessage().startsWith("status 404 "));
        }
    }

    private Order createOrder() {
        return new Order()
                .id(TestUtils.nextId())
                .petId(200L)
                .quantity(13)
                //Ensure 3 fractional digits because of a bug in the petstore server
                .shipDate(OffsetDateTime.now().withNano(123000000))
                .status(Order.StatusEnum.PLACED)
                .complete(true);
    }

    @SpringBootApplication(scanBasePackages = "io.swagger", exclude = ClientConfiguration.class)
    @EnableFeignClients
    protected static class Application {
        public static void main(String[] args) {
            new SpringApplicationBuilder(StoreApiTest.Application.class).run(args);
        }
    }
}
