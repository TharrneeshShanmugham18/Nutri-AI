package com.nutriai.modules.auth.repository;

import com.nutriai.modules.auth.domain.Role;
import com.nutriai.modules.auth.domain.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(RoleName name);
}

