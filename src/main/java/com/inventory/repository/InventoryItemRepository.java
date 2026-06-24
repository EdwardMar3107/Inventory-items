package com.inventory.repository;

import com.inventory.entity.Category;
import com.inventory.entity.InventoryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    Optional<InventoryItem> findBySku(String sku);

    @Query("""
            SELECT i FROM InventoryItem i
            WHERE (:name IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :name, '%')))
              AND (:sku IS NULL OR LOWER(i.sku) LIKE LOWER(CONCAT('%', :sku, '%')))
              AND (:category IS NULL OR i.category = :category)
            """)
    Page<InventoryItem> findAllWithFilters(
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("category") Category category,
            Pageable pageable
    );

    @Query("""
            SELECT i.category AS category, COUNT(i) AS itemCount, SUM(i.quantity) AS totalQuantity
            FROM InventoryItem i
            WHERE i.createdAt BETWEEN :from AND :to
            GROUP BY i.category
            """)
    java.util.List<CategoryStatProjection> getStatsByDateRange(
            @Param("from") java.time.LocalDateTime from,
            @Param("to") java.time.LocalDateTime to
    );

    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.createdAt BETWEEN :from AND :to")
    Long countByDateRange(
            @Param("from") java.time.LocalDateTime from,
            @Param("to") java.time.LocalDateTime to
    );

    interface CategoryStatProjection {
        Category getCategory();
        Long getItemCount();
        Long getTotalQuantity();
    }
}
