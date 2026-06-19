package com.yongoh.agenthub_backend.global.security;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
	private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
	private final RestClient restClient = RestClient.create();

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauthUser = delegate.loadUser(userRequest);
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		if (!"github".equals(registrationId) || StringUtils.hasText((String)oauthUser.getAttributes().get("email"))) {
			return oauthUser;
		}

		Map<String, Object> attributes = new HashMap<>(oauthUser.getAttributes());
		String email = fetchPrimaryGithubEmail(userRequest.getAccessToken().getTokenValue());
		if (StringUtils.hasText(email)) {
			attributes.put("email", email);
		}

		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();
		List<GrantedAuthority> authorities = new ArrayList<>(oauthUser.getAuthorities());
		return new DefaultOAuth2User(authorities, attributes, userNameAttributeName);
	}

	private String fetchPrimaryGithubEmail(String accessToken) {
		List<Map<String, Object>> emails = restClient.get()
			.uri("https://api.github.com/user/emails")
			.header("Authorization", "Bearer " + accessToken)
			.header("Accept", "application/vnd.github+json")
			.retrieve()
			.body(new ParameterizedTypeReference<>() {
			});

		if (emails == null) {
			return null;
		}

		return emails.stream()
			.filter(email -> Boolean.TRUE.equals(email.get("primary")) && Boolean.TRUE.equals(email.get("verified")))
			.map(email -> (String)email.get("email"))
			.filter(StringUtils::hasText)
			.findFirst()
			.orElse(null);
	}
}
