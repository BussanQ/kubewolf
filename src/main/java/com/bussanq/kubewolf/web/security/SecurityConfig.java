package com.bussanq.kubewolf.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.SecureRandom;
import java.util.*;

@Slf4j
@Configuration
public class SecurityConfig {
    @Bean
    UserDetailsService users(@Value("${auth.admin-password:}") String password,
                             @Value("${auth.password-file:${user.home}/.kubewolf/admin-password}") String file,
                             @Value("${auth.viewer-password:}") String viewerPassword) throws IOException {
        if (password.isBlank()) {
            Path path = Path.of(file);
            Files.createDirectories(path.toAbsolutePath().getParent());
            if (!Files.exists(path)) {
                byte[] random = new byte[24];
                new SecureRandom().nextBytes(random);
                String generated = Base64.getUrlEncoder().withoutPadding().encodeToString(random);
                try {
                    Files.createFile(path, PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-------")));
                    Files.writeString(path, generated + "\n");
                } catch (FileAlreadyExistsException ignored) {
                    // Another local instance has already initialized the credential.
                }
            }
            password = Files.readString(path).trim();
            log.info("Admin credential loaded from {}", path);
        }
        if (password.length() < 12) throw new IllegalArgumentException("管理员密码至少需要 12 个字符");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        List<UserDetails> users = new ArrayList<>();
        users.add(User.withUsername("admin").password("{bcrypt}" + encoder.encode(password)).roles("ADMIN", "VIEWER").build());
        if (!viewerPassword.isBlank()) {
            if (viewerPassword.length() < 12) throw new IllegalArgumentException("只读账号密码至少需要 12 个字符");
            users.add(User.withUsername("viewer").password("{bcrypt}" + encoder.encode(viewerPassword)).roles("VIEWER").build());
        }
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, ObjectMapper mapper) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/error", "/api/auth/csrf", "/component/**", "/admin/**", "/js/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/**").hasAnyRole("ADMIN", "VIEWER")
                .anyRequest().hasRole("ADMIN"));
        http.formLogin(form -> form.loginPage("/login").loginProcessingUrl("/login")
                .usernameParameter("userName").passwordParameter("passWord")
                .successHandler((request, response, auth) -> json(mapper, response, 200, "登录成功"))
                .failureHandler((request, response, error) -> json(mapper, response, 401, "账号或密码错误")));
        http.logout(logout -> logout.logoutUrl("/logout")
                .logoutSuccessHandler((request, response, auth) -> json(mapper, response, 200, "已退出登录")));
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, error) -> {
                    if (request.getRequestURI().startsWith("/api/")) json(mapper, response, 401, "请先登录");
                    else response.sendRedirect("/login");
                })
                .accessDeniedHandler((request, response, error) -> json(mapper, response, 403, "权限不足或会话校验失败，请刷新页面")));
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        http.addFilterAfter(new AuditFilter(), SecurityContextHolderFilter.class);
        return http.build();
    }

    private static void json(ObjectMapper mapper, HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(response.getWriter(), Map.of("code", status, "message", message));
    }
}
