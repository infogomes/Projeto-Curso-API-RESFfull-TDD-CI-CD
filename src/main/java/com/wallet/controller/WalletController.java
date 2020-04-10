package com.wallet.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wallet.dto.WalletDTO;
import com.wallet.entity.Wallet;
import com.wallet.response.Response;
import com.wallet.service.WalletService;

@RestController
@RequestMapping("wallet")
public class WalletController {

	@Autowired
	private WalletService walletService;

	@PostMapping
	public ResponseEntity<Response<WalletDTO>> create(@Valid @RequestBody WalletDTO walletDTO, BindingResult result) {

		Response<WalletDTO> response = new Response<WalletDTO>();

		if (result.hasErrors()) {
			result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
			return ResponseEntity.badRequest().body(response);
		}

		Wallet wallet = walletService.save(this.convertDtoToEntity(walletDTO));

		response.setData(this.convertEntityToDto(wallet));

		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

	private Wallet convertDtoToEntity(WalletDTO walletDTO) {
		Wallet wallet = new Wallet();
		wallet.setId(walletDTO.getId());
		wallet.setName(walletDTO.getName());
		wallet.setValue(walletDTO.getValue());

		return wallet;
	}

	private WalletDTO convertEntityToDto(Wallet wallet) {
		WalletDTO walletDTO = new WalletDTO();
		walletDTO.setId(wallet.getId());
		walletDTO.setName(wallet.getName());
		walletDTO.setValue(wallet.getValue());

		return walletDTO;
	}

}
