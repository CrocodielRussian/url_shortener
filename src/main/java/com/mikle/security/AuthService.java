package com.mikle.security;

import com.mikle.dto.AuthRequest;
import com.mikle.dto.AuthResponse;
import com.mikle.dto.RegisterRequest;
import com.mikle.model.User;
import com.mikle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthResponse register(@RequestBody RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("User is already exist");
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String jwt = jwtUtils.generateToken(userDetails);

        return new AuthResponse(jwt, user.getId(), user.getUsername());
    }

    public AuthResponse login(@RequestBody AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetailsImpl user  = (UserDetailsImpl) auth.getPrincipal();
        String jwt = jwtUtils.generateToken(user);

        return new AuthResponse(jwt, user.getId(), user.getUsername());
    }


}
