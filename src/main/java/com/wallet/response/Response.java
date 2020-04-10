package com.wallet.response;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Response<T> {

	private T data;
	private List<String> errors;

	public List<String> getErrors() {

		this.errors = Optional.ofNullable(errors).orElse(new ArrayList<String>());

		return this.errors;
	}

}
