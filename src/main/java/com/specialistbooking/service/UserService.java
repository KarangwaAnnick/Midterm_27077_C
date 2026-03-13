package com.specialistbooking.service;

import com.specialistbooking.dto.request.UserRequest;
import com.specialistbooking.entity.User;
import java.util.List;

public interface UserService {
    User registerUser(UserRequest request);
    List<User> getAllUsers();
    List<User> getUsersByProvince(String province);
    User getUserById(Long id);
    User updateUser(Long id, UserRequest request);
    void deleteUser(Long id);
    List<User> searchUsers(String name, String role, String province, String district);
}