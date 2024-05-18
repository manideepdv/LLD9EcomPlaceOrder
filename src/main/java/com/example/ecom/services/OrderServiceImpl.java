package com.example.ecom.services;

import com.example.ecom.exceptions.*;
import com.example.ecom.models.*;
import com.example.ecom.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final HighDemandProductRepository highDemandProductRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderServiceImpl(UserRepository userRepository, AddressRepository addressRepository, ProductRepository productRepository, HighDemandProductRepository highDemandProductRepository, InventoryRepository inventoryRepository, OrderDetailRepository orderDetailRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.highDemandProductRepository = highDemandProductRepository;
        this.inventoryRepository = inventoryRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public Order placeOrder(int userId, int addressId, List<Pair<Integer, Integer>> orderDetails) throws UserNotFoundException, InvalidAddressException, OutOfStockException, InvalidProductException, HighDemandProductException {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException("User with id " + userId + " not found");
        }
        Optional<Address> optionalAddress = addressRepository.findById(addressId);
        if (optionalAddress.isEmpty()) {
            throw new InvalidAddressException("Address with id " + addressId + " not found");
        }
        Address address = optionalAddress.get();
        User user = optionalUser.get();
        if(address.getUser().getId() != user.getId()) {
            throw new InvalidAddressException("User with id " + user.getId() + " is not the owner of the address");
        }

        synchronized (this) {
            List<OrderDetail> orderDetailsObjList = new ArrayList<>();

            for (Pair<Integer, Integer> pair : orderDetails) {
                int productId = pair.getFirst();
                int quantity = pair.getSecond();

                Optional<Product> optionalProduct = productRepository.findById(productId);
                if (optionalProduct.isEmpty()) {
                    throw new InvalidProductException("Product with id " + productId + " not found");
                }
                Product product = optionalProduct.get();
                Optional<HighDemandProduct> optionalHighDemandProduct = highDemandProductRepository.findByProduct(product);
                if (optionalHighDemandProduct.isPresent()) {
                    HighDemandProduct highDemandProduct = optionalHighDemandProduct.get();
                    if(quantity > highDemandProduct.getMaxQuantity()) {
                        throw new HighDemandProductException("High demand product exceeded maximum quantity");
                    }
                }
                Optional<Inventory> optionalInventory = inventoryRepository.findByProduct(product);
                if (optionalInventory.isEmpty()) {
                    throw new InvalidProductException("Product with id " + productId + " not found");
                }
                Inventory inventory = optionalInventory.get();
                if(quantity > inventory.getQuantity()) {
                    throw new OutOfStockException("Out of stock");
                }

                inventory.setQuantity(inventory.getQuantity() - quantity);
                inventoryRepository.save(inventory);

                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setProduct(product);
                orderDetail.setQuantity(quantity);
                orderDetailsObjList.add(orderDetail);
            }

            Order order = new Order();
            order.setUser(user);
            order.setDeliveryAddress(address);
            order.setOrderDetails(orderDetailsObjList);
            order.setOrderStatus(OrderStatus.PLACED);

            return orderRepository.save(order);
        }
    }
}
