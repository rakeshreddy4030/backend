package com.foodapp.controller;

import com.foodapp.entity.FoodItem;
import com.foodapp.entity.Restaurant;
import com.foodapp.repository.FoodItemRepository;
import com.foodapp.repository.RestaurantRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/foods")
public class FoodController {

    public record FoodRequest(@NotBlank String name, String description, @Positive double price,
                              String category, String imageUrl, @NotNull Long restaurantId) {}

    private final FoodItemRepository foodRepo;
    private final RestaurantRepository restaurantRepo;

    public FoodController(FoodItemRepository foodRepo, RestaurantRepository restaurantRepo) {
        this.foodRepo = foodRepo;
        this.restaurantRepo = restaurantRepo;
    }

    private void fill(FoodItem f, FoodRequest r) {
        Restaurant restaurant = restaurantRepo.findById(r.restaurantId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Restaurant not found"));
        f.setName(r.name().trim());
        f.setDescription(r.description());
        f.setPrice(r.price());
        f.setCategory(r.category());
        f.setImageUrl(r.imageUrl());
        f.setRestaurant(restaurant);
    }

    @GetMapping
    public List<FoodItem> all() {
        return foodRepo.findAll();
    }

    @PostMapping
    public FoodItem create(@Valid @RequestBody FoodRequest req) {
        FoodItem f = new FoodItem();
        fill(f, req);
        return foodRepo.save(f);
    }

    @PutMapping("/{id}")
    public FoodItem update(@PathVariable Long id, @Valid @RequestBody FoodRequest req) {
        FoodItem f = foodRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found"));
        fill(f, req);
        return foodRepo.save(f);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        if (!foodRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Food item not found");
        }
        try {
            foodRepo.deleteById(id);
            foodRepo.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This item is part of existing orders and cannot be deleted");
        }
        return Map.of("message", "Deleted");
    }
}
