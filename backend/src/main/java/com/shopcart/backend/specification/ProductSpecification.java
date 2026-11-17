package com.shopcart.backend.specification;

import com.shopcart.backend.dto.ProductFilterCriteriaDto;
import com.shopcart.backend.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> withFilters(ProductFilterCriteriaDto criteria) {
        return (root, query, criteriaBuilder) -> {
            if (criteria == null) {
                return criteriaBuilder.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();
            // 1. Keyword search (Name or Description
            if (criteria.getKeyword() != null && !criteria.getKeyword().trim().isEmpty()) {
                String pattern = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
                Predicate nameMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        pattern
                );

                Predicate descMatch = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        pattern
                );
                predicates.add(criteriaBuilder.or(
                        nameMatch,
                        descMatch
                ));
            }

            // 2. Category filter
            if (criteria.getCategoryId() != null) {
                predicates.add(
                        criteriaBuilder.equal(
                                root.get("category").get("id"),
                                criteria.getCategoryId()
                        )
                );
            }

            // 3. Minimum price
            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.<BigDecimal>get("price"),
                        criteria.getMinPrice()
                ));
            }

            // 4. Maximum price
            if(criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.<BigDecimal>get("price"),
                        criteria.getMaxPrice()
                ));
            }

            // 5. In-stock check
            if(Boolean.TRUE.equals(criteria.getInStock())) {
                predicates.add(criteriaBuilder.greaterThan(
                        root.get("stockQuantity"),
                        0
                ));
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
