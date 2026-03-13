package com.specialistbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.specialistbooking.entity.User;
import com.specialistbooking.enums.Role;

public interface UserRepository extends JpaRepository<User, Long> {

    // Satisfies existBy() requirement
    boolean existsByEmail(String email);

    // Find user by email (for authentication)
    Optional<User> findByEmail(String email);

    // Satisfies query by province requirement
    List<User> findByLocation_Province(String province);

    // Search by role (patients, doctors, admins)
    List<User> findByRole(Role role);

    // Search patients/users by name
    List<User> findByNameContainingIgnoreCase(String name);

    // Search by location (province)
    List<User> findByLocation_ProvinceContainingIgnoreCase(String province);

    // Search by location (district)
    List<User> findByLocation_DistrictContainingIgnoreCase(String district);

    // Complex search: name and location
    @Query("SELECT u FROM User u WHERE " +
           "(:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:province IS NULL OR LOWER(u.location.province) LIKE LOWER(CONCAT('%', CAST(:province AS string), '%'))) AND " +
           "(:district IS NULL OR LOWER(u.location.district) LIKE LOWER(CONCAT('%', CAST(:district AS string), '%')))")
    List<User> searchUsers(@Param("name") String name,
                          @Param("role") Role role,
                          @Param("province") String province,
                          @Param("district") String district);
}