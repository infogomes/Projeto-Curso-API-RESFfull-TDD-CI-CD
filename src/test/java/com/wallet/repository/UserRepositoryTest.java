package com.wallet.repository;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.wallet.entity.User;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {

	private static final String EMAIL = "email@teste.com";

	@Autowired
	UserRepository userRepository;

	@Before
	public void setUp() {
		User user = new User();
		user.setName("Set up User");
		user.setPassword("senha123");
		user.setEmail(EMAIL);

		userRepository.save(user);
	}

	@After
	public void tearDown() {
		userRepository.deleteAll();
	}

	@Test
	public void testSave() {
		User user = new User();
		user.setName("Teste");
		user.setPassword("123456");
		user.setEmail("teste@teste.com.br");

		User userResponse = userRepository.save(user);

		assertNotNull(userResponse);
	}
	
	@Test
	public void testFindByEmail() {
		Optional<User> userResponse = userRepository.findByEmailEquals(EMAIL);

		assertTrue(userResponse.isPresent());
		assertEquals(userResponse.get().getEmail(), EMAIL);
	}

}
