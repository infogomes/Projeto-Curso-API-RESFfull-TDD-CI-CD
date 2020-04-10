package com.wallet.dto;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@SuppressWarnings("deprecation")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

	private Long id;

	@NotNull
	@Length(min = 6, message = "A senha deve ter um mínimo de 6 caracteres")
	private String password;

	@Length(min = 3, max = 50, message = "O nome deve ter entre 3 e 50 caracteres")
	private String name;

	@Email(message = "E-mail inválido")
	private String email;

}
