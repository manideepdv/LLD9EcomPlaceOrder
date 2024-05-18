package com.example.ecom.repositories;


import com.example.ecom.models.HighDemandProduct;
import com.example.ecom.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HighDemandProductRepository extends JpaRepository<HighDemandProduct, Integer> {
    Optional<HighDemandProduct> findByProduct(Product product);
}
