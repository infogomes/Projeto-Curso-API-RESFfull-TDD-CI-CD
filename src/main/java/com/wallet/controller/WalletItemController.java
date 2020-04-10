package com.wallet.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.dto.WalletItemDTO;
import com.wallet.entity.UserWallet;
import com.wallet.entity.Wallet;
import com.wallet.entity.WalletItem;
import com.wallet.response.Response;
import com.wallet.service.UserWalletService;
import com.wallet.service.WalletItemService;
import com.wallet.util.Util;
import com.wallet.util.enums.TypeEnum;

@RestController
@RequestMapping("wallet-item")
public class WalletItemController {

	@Autowired
	private WalletItemService walletItemService;

	@Autowired
	private UserWalletService userWalletService;
	
	private static final Logger log = LoggerFactory.getLogger(WalletItemController.class);

	@PostMapping
	public ResponseEntity<Response<WalletItemDTO>> create(@Valid @RequestBody WalletItemDTO walletItemDTO,
			BindingResult result) {

		Response<WalletItemDTO> response = new Response<>();

		if (result.hasErrors()) {
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}

		WalletItem walletItem = walletItemService.save(this.convertDtoToEntity(walletItemDTO));

		response.setData(this.convertEntityToDto(walletItem));

		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping(value = "/{wallet}")
	public ResponseEntity<Response<Page<WalletItemDTO>>> findBetweenDates(@PathVariable("wallet") Long wallet,
			@RequestParam("startDate") @DateTimeFormat(pattern = "dd-MM-yyyy") Date startDate,
			@RequestParam("endDate") @DateTimeFormat(pattern = "dd-MM-yyyy") Date endDate,
			@RequestParam(name = "page", defaultValue = "0") int page) {

		Response<Page<WalletItemDTO>> response = new Response<Page<WalletItemDTO>>();

		Optional<UserWallet> uw = userWalletService.findByUsersIdAndWalletId(Util.getAuthenticatedUserId(), wallet);

		if (!uw.isPresent()) {
			response.getErrors().add("Você não tem acesso a essa carteira");
			return ResponseEntity.badRequest().body(response);
		}

		Page<WalletItem> items = walletItemService.findBetweenDates(wallet, startDate, endDate, page);
		Page<WalletItemDTO> dto = items.map(item -> this.convertEntityToDto(item));
		response.setData(dto);

		return ResponseEntity.ok().body(response);

	}

	@GetMapping(value = "/type/{wallet}")
	public ResponseEntity<Response<List<WalletItemDTO>>> findByWalletIdAndType(@PathVariable("wallet") Long wallet,
			@RequestParam("type") String type) {
		
		log.info("Buscando por carteira {} e tipo {}", wallet, type);

		Response<List<WalletItemDTO>> response = new Response<List<WalletItemDTO>>();
		List<WalletItem> items = walletItemService.findByWalletAndType(wallet, TypeEnum.getEnum(type));

		List<WalletItemDTO> dto = new ArrayList<WalletItemDTO>();
		items.forEach(item -> dto.add(this.convertEntityToDto(item)));
		response.setData(dto);

		return ResponseEntity.ok().body(response);

	}

	@GetMapping(value = "/total/{wallet}")
	public ResponseEntity<Response<BigDecimal>> findByWalletId(@PathVariable("wallet") Long wallet) {

		Response<BigDecimal> response = new Response<BigDecimal>();
		BigDecimal value = walletItemService.sumByWalletId(wallet);
		response.setData(value == null ? BigDecimal.ZERO : value);

		return ResponseEntity.ok().body(response);
	}

	@PutMapping
	public ResponseEntity<Response<WalletItemDTO>> update(@Valid @RequestBody WalletItemDTO walletItemDto,
			BindingResult result) {

		Response<WalletItemDTO> response = new Response<WalletItemDTO>();

		Optional<WalletItem> optionalWalletItem = walletItemService.findById(walletItemDto.getId());

		if (!optionalWalletItem.isPresent()) {
			result.addError(new ObjectError("WalletItem", "WalletItem não encontrado."));
		} else if (optionalWalletItem.get().getWallet().getId().compareTo(walletItemDto.getWallet()) != 0) {
			result.addError(new ObjectError("WalletItemChanged", "Você não pode alterar a carteira."));
		}

		if (result.hasErrors()) {
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		WalletItem walletItem = walletItemService.save(this.convertDtoToEntity(walletItemDto));

		response.setData(this.convertEntityToDto(walletItem));

		return ResponseEntity.ok().body(response);
	}

	@DeleteMapping(value = "/{wallet}")
	public ResponseEntity<Response<String>> delete(@PathVariable("wallet") Long wallet) {

		Response<String> response = new Response<String>();

		Optional<WalletItem> optionalWalletItem = walletItemService.findById(wallet);

		if (!optionalWalletItem.isPresent()) {
			response.getErrors().add("WalletItem de id " + wallet + " não encontrada.");
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
		}

		walletItemService.deleteById(wallet);
		response.setData("WalletItem de id " + wallet + " apagada com sucesso.");
		return ResponseEntity.ok().body(response);
	}

	private WalletItem convertDtoToEntity(WalletItemDTO walletItemDTO) {
		WalletItem walletItem = new WalletItem();
		walletItem.setId(walletItemDTO.getId());
		walletItem.setDate(walletItemDTO.getDate());
		walletItem.setDescription(walletItemDTO.getDescription());
		walletItem.setType(TypeEnum.getEnum(walletItemDTO.getType()));
		walletItem.setValue(walletItemDTO.getValue());
		Wallet wallet = new Wallet();
		wallet.setId(walletItemDTO.getWallet());
		walletItem.setWallet(wallet);

		return walletItem;
	}

	private WalletItemDTO convertEntityToDto(WalletItem walletItem) {
		WalletItemDTO walletItemDTO = new WalletItemDTO();
		walletItemDTO.setId(walletItem.getId());
		walletItemDTO.setDate(walletItem.getDate());
		walletItemDTO.setDescription(walletItem.getDescription());
		walletItemDTO.setType(walletItem.getType().getValue());
		walletItemDTO.setValue(walletItem.getValue());
		walletItemDTO.setWallet(walletItem.getWallet().getId());

		return walletItemDTO;
	}

}
