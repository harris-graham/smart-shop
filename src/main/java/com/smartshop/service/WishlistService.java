package com.smartshop.service;

import com.smartshop.entity.Customer;
import com.smartshop.entity.Smartphone;
import com.smartshop.entity.Wishlist;
import com.smartshop.exception.ResourceNotFoundException;
import com.smartshop.repository.CustomerRepository;
import com.smartshop.repository.SmartphoneRepository;
import com.smartshop.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final CustomerRepository customerRepository;
    private final SmartphoneRepository smartphoneRepository;

    @Transactional(readOnly = true)
    public List<Wishlist> findByCustomer(Integer custid) {
        return wishlistRepository.findByCustomer_CustidOrderByCreatedAtDesc(custid);
    }

    @Transactional
    public void add(Integer custid, String smartid) {
        if (wishlistRepository.existsByCustomer_CustidAndSmartphone_Smartid(custid, smartid)) {
            return;
        }
        Customer customer = customerRepository.findById(custid)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", custid));
        Smartphone smartphone = smartphoneRepository.findById(smartid)
                .orElseThrow(() -> new ResourceNotFoundException("Smartphone", "id", smartid));
        wishlistRepository.save(new Wishlist(customer, smartphone));
    }

    @Transactional
    public void remove(Integer custid, String smartid) {
        wishlistRepository.deleteByCustomer_CustidAndSmartphone_Smartid(custid, smartid);
    }

    @Transactional(readOnly = true)
    public boolean exists(Integer custid, String smartid) {
        return wishlistRepository.existsByCustomer_CustidAndSmartphone_Smartid(custid, smartid);
    }
}
