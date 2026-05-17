package com.mikle.security;

import com.mikle.model.User;
import com.mikle.model.UserDetailsImpl;
import com.mikle.service.UserService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserService userService;

    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @Getter
    @Setter
    private static class AuthRequest {
        private String username;
        private String password;
    }

    @PostMapping
    public ResponseEntity<Void> register(@RequestBody AuthRequest request) {
        userService.register(request.getUsername(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<Void> login(@RequestBody AuthRequest request) {
        UserDetailsImpl user = userService.authenticate(request.getUsername(), request.getPassword());

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
            );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        String jwt = jwtUtils.generateJwtToken(auth);

        return ResponseEntity.ok().build();
    }


}
