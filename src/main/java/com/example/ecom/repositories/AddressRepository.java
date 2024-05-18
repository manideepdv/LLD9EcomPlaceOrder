package com.example.ecom.repositories;


import com.example.ecom.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
    @Override
    Optional<Address> findById(Integer integer);
}
