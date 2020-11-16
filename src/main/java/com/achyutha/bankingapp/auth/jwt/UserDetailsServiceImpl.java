package com.achyutha.bankingapp.auth.jwt;

import com.achyutha.bankingapp.domain.model.User;
import com.achyutha.bankingapp.domain.service.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * User Details Service Impl.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private String currentLoggedInUserName;

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        currentLoggedInUserName = user.getUsername();
        return UserDetailsImpl.build(user);
    }

    public String getCurrentLoggedInUser(){
        return currentLoggedInUserName;
    }
}
