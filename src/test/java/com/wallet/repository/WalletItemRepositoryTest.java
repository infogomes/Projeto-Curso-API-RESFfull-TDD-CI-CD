package com.wallet.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.ConstraintViolationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.util.enums.TypeEnum;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class WalletItemRepositoryTest {

	@Autowired
	WalletItemRepository walletItemRepository;

	@Autowired
	WalletRepository walletRepository;

	private Long savedWalletItemId = null;
	private Long savedWalletId = null;

	private static final Date DATE = new Date();
	private static final TypeEnum TYPE = TypeEnum.EN;
	private static final String DESCRIPTION = "Conta de Luz";
	private static final BigDecimal VALUE = BigDecimal.valueOf(65);

	@Before
	public void setUp() {
		Wallet wallet = new Wallet();
		wallet.setName("Carteira Teste");
		wallet.setValue(BigDecimal.valueOf(250));
		walletRepository.save(wallet);

		WalletItem walletItem = new WalletItem(null, wallet, DATE, TYPE, DESCRIPTION, VALUE);
		walletItemRepository.save(walletItem);

		savedWalletItemId = walletItem.getId();
		savedWalletId = wallet.getId();
	}

	@After
	public void tearDown() {
		walletItemRepository.deleteAll();
		walletRepository.deleteAll();
	}

	@Test
	public void testSave() {
		Wallet wallet = new Wallet();
		wallet.setName("Carteira 1");
		wallet.setValue(BigDecimal.valueOf(500));

		walletRepository.save(wallet);

		WalletItem walletItem = new WalletItem(1L, wallet, DATE, TYPE, DESCRIPTION, VALUE);

		WalletItem walletItemBD = walletItemRepository.save(walletItem);

		assertNotNull(walletItemBD);
		assertEquals(walletItemBD.getDescription(), DESCRIPTION);
		assertEquals(walletItemBD.getType(), TYPE);
		assertEquals(walletItemBD.getValue(), VALUE);
		assertEquals(walletItemBD.getWallet().getId(), wallet.getId());

	}

	@Test(expected = ConstraintViolationException.class)
	public void testSaveInvalidWalletItem() {
		WalletItem walletItem = new WalletItem(null, null, DATE, null, DESCRIPTION, null);
		walletItemRepository.save(walletItem);
	}

	@Test
	public void testUpdate() {
		Optional<WalletItem> optionalWalletItem = walletItemRepository.findById(savedWalletItemId);

		String description = "Descrição Alterada";

		WalletItem changedWalletItem = optionalWalletItem.get();
		changedWalletItem.setDescription(description);

		walletItemRepository.save(changedWalletItem);

		Optional<WalletItem> optionalWalletItemDB = walletItemRepository.findById(savedWalletItemId);

		assertEquals(description, optionalWalletItemDB.get().getDescription());
	}

	@Test
	public void deleteWalletItem() {
		Optional<Wallet> optionalWallet = walletRepository.findById(savedWalletId);
		WalletItem walletItem = new WalletItem(null, optionalWallet.get(), DATE, TYPE, DESCRIPTION, VALUE);

		walletItemRepository.save(walletItem);

		walletItemRepository.deleteById(walletItem.getId());

		Optional<WalletItem> optionalWalletItem = walletItemRepository.findById(walletItem.getId());

		assertFalse(optionalWalletItem.isPresent());
	}

	@Test
	public void testFindBetweenDates() {
		Optional<Wallet> optionalWallet = walletRepository.findById(savedWalletId);

		LocalDateTime localDateTime = DATE.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

		Date currentDatePlusFiveDays = Date.from(localDateTime.plusDays(5).atZone(ZoneId.systemDefault()).toInstant());
		Date currentDatePlusSevenDays = Date.from(localDateTime.plusDays(7).atZone(ZoneId.systemDefault()).toInstant());

		walletItemRepository
				.save(new WalletItem(null, optionalWallet.get(), currentDatePlusFiveDays, TYPE, DESCRIPTION, VALUE));
		walletItemRepository
				.save(new WalletItem(null, optionalWallet.get(), currentDatePlusSevenDays, TYPE, DESCRIPTION, VALUE));

		PageRequest pageRequest = PageRequest.of(0, 10);
		Page<WalletItem> pageWalletItemResponse = walletItemRepository
				.findAllByWalletIdAndDateGreaterThanEqualAndDateLessThanEqual(savedWalletId, DATE,
						currentDatePlusFiveDays, pageRequest);

		assertEquals(pageWalletItemResponse.getContent().size(), 2);
		assertEquals(pageWalletItemResponse.getTotalElements(), 2);
		assertEquals(pageWalletItemResponse.getContent().get(0).getWallet().getId(), savedWalletId);
	}

	@Test
	public void testFindByType() {
		List<WalletItem> listWalletItem = walletItemRepository.findByWalletIdAndType(savedWalletId, TYPE);

		assertEquals(listWalletItem.size(), 1);
		assertEquals(listWalletItem.get(0).getType(), TYPE);
	}

	@Test
	public void testFindByTypeSd() {
		Optional<Wallet> optionalWallet = walletRepository.findById(savedWalletId);

		walletItemRepository.save(new WalletItem(null, optionalWallet.get(), DATE, TypeEnum.SD, DESCRIPTION, VALUE));

		List<WalletItem> listWalletItem = walletItemRepository.findByWalletIdAndType(savedWalletId, TypeEnum.SD);

		assertEquals(listWalletItem.size(), 1);
		assertEquals(listWalletItem.get(0).getType(), TypeEnum.SD);
	}
	
	@Test
	public void testSumByWallet() {
		Optional<Wallet> optionalWallet = walletRepository.findById(savedWalletId);
		
		walletItemRepository.save(new WalletItem(null, optionalWallet.get(), DATE, TYPE, DESCRIPTION, BigDecimal.valueOf(150.80)));
		
		BigDecimal sumWallet = walletItemRepository.sumByWalletId(savedWalletId);
		
		assertEquals(sumWallet.compareTo(BigDecimal.valueOf(215.8)), 0);
	}
	
}
