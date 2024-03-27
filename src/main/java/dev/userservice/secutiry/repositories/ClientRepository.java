package dev.userservice.secutiry.repositories;

import dev.userservice.secutiry.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByClientId(String clientId);
}