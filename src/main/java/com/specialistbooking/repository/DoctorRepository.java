package com.specialistbooking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.specialistbooking.entity.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Page<Doctor> findAll(Pageable pageable);
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);
    List<Doctor> findByLocation_Province(String province);
    List<Doctor> findByLocation_ProvinceIgnoreCase(String province);
    List<Doctor> findByLocation_DistrictIgnoreCase(String district);
    Optional<Doctor> findByUser_Id(Long userId);
    
    // Search by name (partial match)
    List<Doctor> findByNameContainingIgnoreCase(String name);

    // Search by hospital
    List<Doctor> findByHospitalNameContainingIgnoreCase(String hospitalName);

    // Advanced search: specialty, name, province, district (partial matches)
    @Query("SELECT d FROM Doctor d WHERE " +
           "(:specialty IS NULL OR LOWER(d.specialty) LIKE LOWER(CONCAT('%', CAST(:specialty AS string), '%'))) AND " +
           "(:name IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%', CAST(:name AS string), '%'))) AND " +
           "(:province IS NULL OR LOWER(d.location.province) LIKE LOWER(CONCAT('%', CAST(:province AS string), '%'))) AND " +
           "(:district IS NULL OR LOWER(d.location.district) LIKE LOWER(CONCAT('%', CAST(:district AS string), '%')))")
    List<Doctor> searchDoctors(@Param("specialty") String specialty,
                               @Param("name") String name,
                               @Param("province") String province,
                               @Param("district") String district);
}