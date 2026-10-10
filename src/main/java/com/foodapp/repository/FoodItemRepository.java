package com.foodapp.repository;

import com.foodapp.entity.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
    List<FoodItem> findByRestaurantId(Long restaurantId);
    boolean existsByRestaurantId(Long restaurantId);
}
