package com.nnk.springboot;

import com.nnk.springboot.domain.BidList;

import com.nnk.springboot.repositories.BidListRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class BidTests {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	public void printEncodedPassword() {
		System.out.println("Encoded 'admin123': " + passwordEncoder.encode("admin123"));
		System.out.println("Encoded 'user123': " + passwordEncoder.encode("user123"));
	}

	@Test
	public void verifyAdminPassword() {
		String rawPassword = "admin123"; // Le mot de passe que vous essayez
		String storedHash = "$2a$10$4MH6YUArhKqL6H1YpjEI7uHo2JhWW00ZET0I.qMAiPqdJwheIH3bG";

		boolean matches = passwordEncoder.matches(rawPassword, storedHash);
		assertTrue(matches);
		System.out.println("Password matches: " + matches);
	}

	@Autowired
	private BidListRepository bidListRepository;

	@Test
	public void bidListTest() {
		BidList bid = new BidList("Account Test", "Type Test", 10d);

		// Save
		bid = bidListRepository.save(bid);
		assertNotNull(bid.getBidListId());
		assertEquals(bid.getBidQuantity(), 10d, 10d);

		// Update
		bid.setBidQuantity(20d);
		bid = bidListRepository.save(bid);
		assertEquals(bid.getBidQuantity(), 20d, 20d);

		// Find
		List<BidList> listResult = bidListRepository.findAll();
		assertTrue(listResult.size() > 0);

		// Delete
		Integer id = bid.getBidListId();
		bidListRepository.delete(bid);
		Optional<BidList> bidList = bidListRepository.findById(id);
		assertFalse(bidList.isPresent());
	}
}
