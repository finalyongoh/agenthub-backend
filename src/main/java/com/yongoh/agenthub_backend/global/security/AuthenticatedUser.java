package com.yongoh.agenthub_backend.global.security;

import java.util.Collection;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class AuthenticatedUser implements UserDetails {
	private final UUID id;
	private final String email;
	private final String loginProvider;
	private final Collection<? extends GrantedAuthority> authorities;

	public AuthenticatedUser(UUID id, String email, Collection<? extends GrantedAuthority> authorities) {
		this(id, email, "EMAIL", authorities);
	}

	public AuthenticatedUser(UUID id, String email, String loginProvider, Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.email = email;
		this.loginProvider = loginProvider;
		this.authorities = authorities;
	}

	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getLoginProvider() {
		return loginProvider;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return "";
	}

	@Override
	public String getUsername() {
		return id.toString();
	}
}
