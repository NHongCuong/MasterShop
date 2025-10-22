package com.sportshop.filter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.sportshop.config.JwtTokenUtils;
import com.sportshop.security.MyUserDetails;
import com.sportshop.service.impl.MyUserDetailsService;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            final String token = authHeader.substring(7);

            if (token != null && !token.trim().isEmpty() && !"null".equalsIgnoreCase(token)) {
                try {
                    final String email = jwtTokenUtils.getUsernameFromToken(token);

                    if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        MyUserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        if (jwtTokenUtils.validateToken(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authToken);
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Lỗi khi xử lý JWT Token: {}", e.getMessage());
                }
            } else {
                logger.debug("JWT Token trống hoặc null");
            }
        } else {
            // Không log ra console nữa — chỉ log ở chế độ debug để tránh làm rác log
            logger.debug("Không có header Authorization hoặc không bắt đầu bằng Bearer");
        }

        filterChain.doFilter(request, response);
    }
}
