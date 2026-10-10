package com.foodapp.controller;

import com.foodapp.entity.FoodItem;
import com.foodapp.entity.Rating;
import com.foodapp.entity.Restaurant;
import com.foodapp.entity.User;
import com.foodapp.repository.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    /** Restaurant details plus its average star rating. */
    public record RestaurantView(Long id, String name, String description, String cuisine,
                                 String address, String imageUrl, double avgRating, long ratingCount) {}

    public record RateRequest(@Min(1) @Max(5) int stars) {}

    private final RestaurantRepository restaurantRepo;
    private final FoodItemRepository foodRepo;
    private final RatingRepository ratingRepo;
    private final OrderRepository orderRepo;
    private final UserRepository userRepo;

    public RestaurantController(RestaurantRepository restaurantRepo, FoodItemRepository foodRepo,
                                RatingRepository ratingRepo, OrderRepository orderRepo, UserRepository userRepo) {
        this.restaurantRepo = restaurantRepo;
        this.foodRepo = foodRepo;
        this.ratingRepo = ratingRepo;
        this.orderRepo = orderRepo;
        this.userRepo = userRepo;
    }

    private Restaurant find(Long id) {
        return restaurantRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
    }

    private RestaurantView view(Restaurant r) {
        Double avg = ratingRepo.averageStars(r.getId());
        double rounded = avg == null ? 0 : Math.round(avg * 10) / 10.0;
        return new RestaurantView(r.getId(), r.getName(), r.getDescription(), r.getCuisine(),
                r.getAddress(), r.getImageUrl(), rounded, ratingRepo.countByRestaurantId(r.getId()));
    }

    // ---------- public (anyone can view) ----------

    @GetMapping
    public List<RestaurantView> all() {
        return restaurantRepo.findAll().stream().map(this::view).toList();
    }

    @GetMapping("/{id}")
    public RestaurantView one(@PathVariable Long id) {
        return view(find(id));
    }

    @GetMapping("/{id}/foods")
    public List<FoodItem> foods(@PathVariable Long id) {
        find(id);
        return foodRepo.findByRestaurantId(id);
    }

    // ---------- customer: star rating ----------

    @PostMapping("/{id}/rate")
    public RestaurantView rate(@PathVariable Long id, @Valid @RequestBody RateRequest req, Authentication auth) {
        Restaurant restaurant = find(id);
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Rating rating = ratingRepo.findByRestaurantIdAndUserId(id, user.getId()).orElseGet(() -> {
            Rating x = new Rating();
            x.setRestaurant(restaurant);
            x.setUser(user);
            return x;
        });
        rating.setStars(req.stars());
        ratingRepo.save(rating);
        return view(restaurant);
    }

    @GetMapping("/{id}/my-rating")
    public Map<String, Integer> myRating(@PathVariable Long id, Authentication auth) {
        User user = userRepo.findByEmail(auth.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        int stars = ratingRepo.findByRestaurantIdAndUserId(id, user.getId()).map(Rating::getStars).orElse(0);
        return Map.of("stars", stars);
    }

    // ---------- admin: manage restaurants ----------

    @PostMapping
    public RestaurantView create(@Valid @RequestBody Restaurant body) {
        body.setId(null);
        return view(restaurantRepo.save(body));
    }

    @PutMapping("/{id}")
    public RestaurantView update(@PathVariable Long id, @Valid @RequestBody Restaurant body) {
        Restaurant r = find(id);
        r.setName(body.getName());
        r.setDescription(body.getDescription());
        r.setCuisine(body.getCuisine());
        r.setAddress(body.getAddress());
        r.setImageUrl(body.getImageUrl());
        return view(restaurantRepo.save(r));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        Restaurant r = find(id);
        if (foodRepo.existsByRestaurantId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delete this restaurant's food items first");
        }
        if (orderRepo.existsByRestaurantId(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This restaurant has orders and cannot be deleted");
        }
        ratingRepo.deleteByRestaurantId(id);
        restaurantRepo.delete(r);
        return Map.of("message", "Deleted");
    }
}
