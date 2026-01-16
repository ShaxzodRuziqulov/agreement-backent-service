package com.example.agreement.repository;

import com.example.agreement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByPinfl(String pinfl);

    Optional<User> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<User> findByPinfl(String pinfl);

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.status = 'ACTIVE'")
    Optional<User> findActiveUserByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}
