package com.foodapp;

import com.foodapp.entity.FoodItem;
import com.foodapp.entity.Restaurant;
import com.foodapp.entity.User;
import com.foodapp.repository.FoodItemRepository;
import com.foodapp.repository.RestaurantRepository;
import com.foodapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/** Creates a default admin, 3 sample restaurants and their food items on first run. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepo;
    private final FoodItemRepository foodRepo;
    private final RestaurantRepository restaurantRepo;
    private final PasswordEncoder encoder;

    public DataSeeder(UserRepository userRepo, FoodItemRepository foodRepo,
                      RestaurantRepository restaurantRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.foodRepo = foodRepo;
        this.restaurantRepo = restaurantRepo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepo.existsByEmail("admin@food.com")) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@food.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepo.save(admin);
            System.out.println(">>> Default admin created: admin@food.com / admin123");
        }

        if (restaurantRepo.count() == 0) {
            Restaurant paradise = restaurant("Paradise Biryani House", "Famous Hyderabadi dum biryani and kebabs", "Biryani", "Secunderabad, Hyderabad");
            Restaurant pizza = restaurant("Pizza Corner", "Fresh oven-baked pizzas, burgers and shakes", "Italian / Fast Food", "Banjara Hills, Hyderabad");
            Restaurant udupi = restaurant("Udupi Tiffins", "South Indian breakfast and snacks", "South Indian", "Madhapur, Hyderabad");

            food(paradise, "Chicken Dum Biryani", "Hyderabadi style dum biryani", 299, "Biryani");
            food(paradise, "Mutton Biryani", "Tender mutton cooked with basmati rice", 399, "Biryani");
            food(paradise, "Veg Biryani", "Aromatic rice with mixed vegetables", 229, "Biryani");
            food(paradise, "Chicken 65", "Spicy fried chicken starter", 180, "Starters");
            food(paradise, "Double Ka Meetha", "Hyderabadi bread pudding dessert", 90, "Dessert");

            food(pizza, "Margherita Pizza", "Classic cheese and tomato pizza", 249, "Pizza");
            food(pizza, "Farmhouse Pizza", "Loaded with fresh veggies", 329, "Pizza");
            food(pizza, "Veg Burger", "Crispy veg patty with fresh veggies", 129, "Burger");
            food(pizza, "Garlic Bread", "Toasted bread with garlic butter", 119, "Snacks");
            food(pizza, "Chocolate Shake", "Thick cold chocolate shake", 119, "Drinks");

            food(udupi, "Masala Dosa", "Crispy dosa with potato filling", 80, "Tiffin");
            food(udupi, "Idli Sambar", "Soft idlis with hot sambar", 60, "Tiffin");
            food(udupi, "Medu Vada", "Crispy lentil donuts", 70, "Tiffin");
            food(udupi, "Pongal", "Rice and moong dal with ghee", 75, "Tiffin");
            food(udupi, "Filter Coffee", "Traditional South Indian coffee", 30, "Drinks");
        }

        // Safety net: attach any old food items that have no restaurant to the first restaurant
        List<Restaurant> all = restaurantRepo.findAll();
        if (!all.isEmpty()) {
            for (FoodItem f : foodRepo.findAll()) {
                if (f.getRestaurant() == null) {
                    f.setRestaurant(all.get(0));
                    foodRepo.save(f);
                }
            }
        }
    }

    private Restaurant restaurant(String name, String desc, String cuisine, String address) {
        Restaurant r = new Restaurant();
        r.setName(name);
        r.setDescription(desc);
        r.setCuisine(cuisine);
        r.setAddress(address);
        return restaurantRepo.save(r);
    }

    private void food(Restaurant restaurant, String name, String desc, double price, String category) {
        FoodItem f = new FoodItem();
        f.setName(name);
        f.setDescription(desc);
        f.setPrice(price);
        f.setCategory(category);
        f.setRestaurant(restaurant);
        foodRepo.save(f);
    }
}
