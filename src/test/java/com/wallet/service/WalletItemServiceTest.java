package com.wallet.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.repository.WalletItemRepository;
import com.wallet.util.enums.TypeEnum;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class WalletItemServiceTest {

	@MockBean
	WalletItemRepository walletItemRepository;

	@Autowired
	WalletItemService walletItemService;

	private static final Date DATE = new Date();
	private static final TypeEnum TYPE = TypeEnum.EN;
	private static final String DESCRIPTION = "Conta de Luz";
	private static final BigDecimal VALUE = BigDecimal.valueOf(65);

	@Test
	public void testSave() {
		BDDMockito.given(walletItemRepository.save(Mockito.any(WalletItem.class))).willReturn(getMockWalletItem());

		WalletItem walletSaved = walletItemService.save(new WalletItem());

		assertNotNull(walletSaved);
		assertEquals(walletSaved.getDescription(), DESCRIPTION);
		assertEquals(walletSaved.getValue().compareTo(VALUE), 0);
	}

	@Test
	public void testFindBetweenDates() {
		List<WalletItem> listWalletItem = new ArrayList<WalletItem>();
		listWalletItem.add(getMockWalletItem());
		Page<WalletItem> pageWalletItem = new PageImpl<>(listWalletItem);

		BDDMockito.given(walletItemRepository.findAllByWalletIdAndDateGreaterThanEqualAndDateLessThanEqual(
				Mockito.anyLong(), Mockito.any(Date.class), Mockito.any(Date.class), Mockito.any(PageRequest.class)))
				.willReturn(pageWalletItem);

		Page<WalletItem> pageWalletItemReturn = walletItemService.findBetweenDates(1L, new Date(), new Date(), 0);

		assertNotNull(pageWalletItemReturn);
		assertEquals(pageWalletItemReturn.getContent().size(), 1);
		assertEquals(pageWalletItemReturn.getContent().get(0).getDescription(), DESCRIPTION);
	}

	@Test
	public void testFindByType() {
		List<WalletItem> listWalletItem = new ArrayList<>();
		listWalletItem.add(getMockWalletItem());

		BDDMockito.given(walletItemRepository.findByWalletIdAndType(Mockito.anyLong(), Mockito.any(TypeEnum.class)))
				.willReturn(listWalletItem);
		
		List<WalletItem> listWalletItemReturn = walletItemService.findByWalletAndType(1L,TypeEnum.EN);
		
		assertNotNull(listWalletItemReturn);
		assertEquals(listWalletItemReturn.get(0).getType(), TYPE);

	}
	
	@Test
	public void testSumByWalet() {
		BigDecimal value = BigDecimal.valueOf(45);
		
		BDDMockito.given(walletItemRepository.sumByWalletId(Mockito.anyLong())).willReturn(value);
		
		BigDecimal sumWallet = walletItemService.sumByWalletId(1L);
		
		assertEquals(sumWallet.compareTo(value), 0);
	}

	private WalletItem getMockWalletItem() {
		Wallet wallet = new Wallet();
		wallet.setId(1L);

		WalletItem walletItem = new WalletItem(1l, wallet, DATE, TYPE, DESCRIPTION, VALUE);

		return walletItem;

	}

}
