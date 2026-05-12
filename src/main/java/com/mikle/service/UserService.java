package com.mikle.service;

import com.mikle.model.User;
import com.mikle.repository.UserRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Getter
@Setter
@Service
public class UserService {
    @Autowired
    private final UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository repo) {
        this.userRepository = repo;
    }

    @Transactional
    public User register(String username, String password) {
        userRepository.findByUsername(username).orElseThrow(
                () -> new IllegalArgumentException("User with this name already exist")
        );
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(password));

        return newUser;
    }
}
