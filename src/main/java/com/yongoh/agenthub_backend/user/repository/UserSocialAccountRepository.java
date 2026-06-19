package com.yongoh.agenthub_backend.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yongoh.agenthub_backend.user.model.SocialProvider;
import com.yongoh.agenthub_backend.user.model.UserSocialAccount;

public interface UserSocialAccountRepository extends JpaRepository<UserSocialAccount, UUID> {
	Optional<UserSocialAccount> findByProviderAndProviderId(SocialProvider provider, String providerId);
}
