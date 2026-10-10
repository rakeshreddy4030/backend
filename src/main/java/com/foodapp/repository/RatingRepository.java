package com.foodapp.repository;

import com.foodapp.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByRestaurantIdAndUserId(Long restaurantId, Long userId);

    long countByRestaurantId(Long restaurantId);

    @Query("select avg(r.stars) from Rating r where r.restaurant.id = :rid")
    Double averageStars(@Param("rid") Long restaurantId);

    @Transactional
    void deleteByRestaurantId(Long restaurantId);
}
