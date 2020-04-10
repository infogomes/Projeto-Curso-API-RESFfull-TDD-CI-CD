package com.wallet.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wallet.dto.WalletItemDTO;
import com.wallet.entity.User;
import com.wallet.entity.UserWallet;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.service.UserService;
import com.wallet.service.UserWalletService;
import com.wallet.service.WalletItemService;
import com.wallet.util.enums.TypeEnum;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class WalletItemControllerTest {

	@MockBean
	WalletItemService walletItemService;

	@MockBean
	UserWalletService userWalletService;

	@MockBean
	UserService userService;

	@Autowired
	MockMvc mockMvc;

	private static final Long ID = 1L;
	private static final Date DATE = new Date();
	private static final LocalDate TODAY = LocalDate.now();
	private static final TypeEnum TYPE = TypeEnum.EN;
	private static final String DESCRIPTION = "Conta de Luz";
	private static final BigDecimal VALUE = BigDecimal.valueOf(65);
	private static final String URL = "/wallet-item";

	@Test
	@WithMockUser
	void testSave() throws Exception {

		BDDMockito.given(walletItemService.save(Mockito.any(WalletItem.class))).willReturn(getMockWalletItem());

		mockMvc.perform(MockMvcRequestBuilders.post(URL).content(getJsonPayload())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(jsonPath("$.data.id").value(ID))
				.andExpect(jsonPath("$.data.date").value(TODAY.format(getDateFormatter())))
				.andExpect(jsonPath("$.data.description").value(DESCRIPTION))
				.andExpect(jsonPath("$.data.type").value(TYPE.getValue()))
				.andExpect(jsonPath("$.data.value").value(VALUE)).andExpect(jsonPath("$.data.wallet").value(ID));
	}

	@Test
	@WithMockUser
	public void testFindBetweenDates() throws JsonProcessingException, Exception {
		List<WalletItem> listWalletItem = new ArrayList<WalletItem>();
		listWalletItem.add(getMockWalletItem());
		Page<WalletItem> pageWalletItem = new PageImpl<>(listWalletItem);

		String startDate = TODAY.format(getDateFormatter());
		String endDate = TODAY.plusDays(5).format(getDateFormatter());

		User user = new User();
		user.setId(1L);

		BDDMockito.given(walletItemService.findBetweenDates(Mockito.anyLong(), Mockito.any(Date.class),
				Mockito.any(Date.class), Mockito.anyInt())).willReturn(pageWalletItem);

		BDDMockito.given(userService.findByEmail(Mockito.anyString())).willReturn(Optional.of(user));

		BDDMockito.given(userWalletService.findByUsersIdAndWalletId(Mockito.anyLong(), Mockito.anyLong()))
				.willReturn(Optional.of(new UserWallet()));

		mockMvc.perform(MockMvcRequestBuilders.get(URL + "/1?startDate=" + startDate + "&endDate=" + endDate)
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.content[0].id").value(ID))
				.andExpect(jsonPath("$.data.content[0].date").value(TODAY.format(getDateFormatter())))
				.andExpect(jsonPath("$.data.content[0].description").value(DESCRIPTION))
				.andExpect(jsonPath("$.data.content[0].type").value(TYPE.getValue()))
				.andExpect(jsonPath("$.data.content[0].value").value(VALUE))
				.andExpect(jsonPath("$.data.content[0].wallet").value(ID));

	}

	@Test
	@WithMockUser
	public void testFindByType() throws JsonProcessingException, Exception {
		List<WalletItem> listWalletItem = new ArrayList<>();
		listWalletItem.add(getMockWalletItem());

		BDDMockito.given(walletItemService.findByWalletAndType(Mockito.anyLong(), Mockito.any(TypeEnum.class)))
				.willReturn(listWalletItem);

		mockMvc.perform(MockMvcRequestBuilders.get(URL + "/type/1?type=ENTRADA").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.[0].id").value(ID))
				.andExpect(jsonPath("$.data.[0].date").value(TODAY.format(getDateFormatter())))
				.andExpect(jsonPath("$.data.[0].description").value(DESCRIPTION))
				.andExpect(jsonPath("$.data.[0].type").value(TYPE.getValue()))
				.andExpect(jsonPath("$.data.[0].value").value(VALUE))
				.andExpect(jsonPath("$.data.[0].wallet").value(ID));

	}

	@Test
	@WithMockUser
	public void testSumByWalet() throws JsonProcessingException, Exception {
		BigDecimal value = BigDecimal.valueOf(536.90);

		BDDMockito.given(walletItemService.sumByWalletId(Mockito.anyLong())).willReturn(value);

		mockMvc.perform(MockMvcRequestBuilders.get(URL + "/total/1").contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value("536.9"));

	}

	@Test
	@WithMockUser
	void testUpdate() throws Exception {

		String description = "Nova descriçaõ";
		Wallet wallet = new Wallet();
		wallet.setId(ID);

		BDDMockito.given(walletItemService.findById(Mockito.anyLong())).willReturn(Optional.of(getMockWalletItem()));

		BDDMockito.given(walletItemService.save(Mockito.any(WalletItem.class)))
				.willReturn(new WalletItem(1L, wallet, DATE, TypeEnum.SD, description, VALUE));

		mockMvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data.id").value(ID))
				.andExpect(jsonPath("$.data.date").value(TODAY.format(getDateFormatter())))
				.andExpect(jsonPath("$.data.description").value(description))
				.andExpect(jsonPath("$.data.type").value(TypeEnum.SD.getValue()))
				.andExpect(jsonPath("$.data.value").value(VALUE)).andExpect(jsonPath("$.data.wallet").value(ID));
	}

	@Test
	@WithMockUser
	void testUpdateWalletChange() throws Exception {

		Wallet wallet = new Wallet();
		wallet.setId(99L);

		WalletItem walletItem = new WalletItem(1L, wallet, DATE, TypeEnum.SD, DESCRIPTION, VALUE);

		BDDMockito.given(walletItemService.findById(Mockito.anyLong())).willReturn(Optional.of(walletItem));

		mockMvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors[0]").value("Você não pode alterar a carteira."));
	}

	@Test
	@WithMockUser
	void testUpdateInvalidId() throws Exception {

		BDDMockito.given(walletItemService.findById(Mockito.anyLong())).willReturn(Optional.empty());

		mockMvc.perform(MockMvcRequestBuilders.put(URL).content(getJsonPayload())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors[0]").value("WalletItem não encontrado."));
	}

	@Test
	@WithMockUser
	void testDelete() throws Exception {

		BDDMockito.given(walletItemService.findById(Mockito.anyLong())).willReturn(Optional.of(new WalletItem()));

		mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/1").content(getJsonPayload())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.data").value("WalletItem de id " + ID + " apagada com sucesso."));
	}

	@Test
	@WithMockUser
	void testDeleteInvalid() throws Exception {

		BDDMockito.given(walletItemService.findById(Mockito.anyLong())).willReturn(Optional.empty());

		mockMvc.perform(MockMvcRequestBuilders.delete(URL + "/99").content(getJsonPayload())
				.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.data").doesNotExist())
				.andExpect(jsonPath("$.errors[0]").value("WalletItem de id " + 99 + " não encontrada."));
	}

	private WalletItem getMockWalletItem() {
		Wallet wallet = new Wallet();
		wallet.setId(1L);

		WalletItem walletItem = new WalletItem(1l, wallet, DATE, TYPE, DESCRIPTION, VALUE);

		return walletItem;

	}

	public String getJsonPayload() throws JsonProcessingException {
		WalletItemDTO walletItemDTO = new WalletItemDTO();
		walletItemDTO.setId(ID);
		walletItemDTO.setDate(DATE);
		walletItemDTO.setDescription(DESCRIPTION);
		walletItemDTO.setType(TYPE.getValue());
		walletItemDTO.setValue(VALUE);
		walletItemDTO.setWallet(ID);

		ObjectMapper objectMapper = new ObjectMapper();

		return objectMapper.writeValueAsString(walletItemDTO);
	}

	private DateTimeFormatter getDateFormatter() {
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		return dateTimeFormatter;
	}

}
