package com.foodapp.controller;

import com.foodapp.entity.FoodItem;
import com.foodapp.entity.Order;
import com.foodapp.entity.OrderItem;
import com.foodapp.entity.Restaurant;
import com.foodapp.entity.User;
import com.foodapp.repository.FoodItemRepository;
import com.foodapp.repository.OrderRepository;
import com.foodapp.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    public record ItemRequest(Long foodId, int quantity) {}
    public record OrderRequest(List<ItemRequest> items) {}
    public record StatusRequest(String status) {}

    private static final List<String> STATUSES =
            List.of("PLACED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED");

    private final OrderRepository orderRepo;
    private final FoodItemRepository foodRepo;
    private final UserRepository userRepo;

    public OrderController(OrderRepository orderRepo, FoodItemRepository foodRepo, UserRepository userRepo) {
        this.orderRepo = orderRepo;
        this.foodRepo = foodRepo;
        this.userRepo = userRepo;
    }

    private User currentUser(Authentication auth) {
        return userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    @PostMapping
    public Order placeOrder(@RequestBody OrderRequest req, Authentication auth) {
        if (req.items() == null || req.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        Order order = new Order();
        order.setUser(currentUser(auth));
        order.setStatus("PLACED");
        order.setOrderDate(LocalDateTime.now());

        Restaurant restaurant = null;
        double total = 0;
        for (ItemRequest r : req.items()) {
            if (r.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be at least 1");
            }
            FoodItem food = foodRepo.findById(r.foodId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found: " + r.foodId()));
            if (food.getRestaurant() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, food.getName() + " is not linked to a restaurant");
            }
            if (restaurant == null) {
                restaurant = food.getRestaurant();
            } else if (!restaurant.getId().equals(food.getRestaurant().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "All items in one order must be from the same restaurant");
            }
            OrderItem oi = new OrderItem();
            oi.setOrder(order);
            oi.setFoodItem(food);
            oi.setQuantity(r.quantity());
            oi.setPrice(food.getPrice());
            order.getItems().add(oi);
            total += food.getPrice() * r.quantity();
        }
        order.setRestaurant(restaurant);
        order.setTotalAmount(total);
        return orderRepo.save(order);
    }

    @GetMapping("/my")
    public List<Order> myOrders(Authentication auth) {
        return orderRepo.findByUserIdOrderByOrderDateDesc(currentUser(auth).getId());
    }

    @GetMapping("/admin/all")
    public List<Order> allOrders() {
        return orderRepo.findAllByOrderByOrderDateDesc();
    }

    @PutMapping("/admin/{id}/status")
    public Order updateStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        if (req.status() == null || !STATUSES.contains(req.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        order.setStatus(req.status());
        return orderRepo.save(order);
    }
}
