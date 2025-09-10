package com.ruoyi.backend.service;

import com.ruoyi.backend.entity.User;
import com.ruoyi.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务类
 */
@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * 创建用户
     */
    public User createUser(String name) {
        if (userRepository.existsByName(name)) {
            throw new RuntimeException("用户名已存在: " + name);
        }
        
        User user = new User();
        user.setName(name);
        return userRepository.save(user);
    }
    
    /**
     * 获取所有用户
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * 根据ID获取用户
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * 根据名称获取用户
     */
    public Optional<User> getUserByName(String name) {
        return userRepository.findByName(name);
    }
    
    /**
     * 更新用户
     */
    public User updateUser(Long id, String name) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
        
        if (!user.getName().equals(name) && userRepository.existsByName(name)) {
            throw new RuntimeException("用户名已存在: " + name);
        }
        
        user.setName(name);
        return userRepository.save(user);
    }
    
    /**
     * 删除用户
     */
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在: " + id);
        }
        userRepository.deleteById(id);
    }
}
