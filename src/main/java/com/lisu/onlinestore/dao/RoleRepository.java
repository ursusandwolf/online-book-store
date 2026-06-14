package com.lisu.onlinestore.dao;

import java.util.Optional;
import com.lisu.onlinestore.model.Role;
import com.lisu.onlinestore.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName roleName);
}
