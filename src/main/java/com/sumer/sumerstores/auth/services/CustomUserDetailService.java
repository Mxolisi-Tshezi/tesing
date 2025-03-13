package com.sumer.sumerstores.auth.services;

import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.auth.repositories.UserDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserDetailRepository userDetailRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOpt = userDetailRepository.findByEmail(username);
        User user = userOpt.orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return user;
    }
}
