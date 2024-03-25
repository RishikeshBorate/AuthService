package dev.userservice.secutiry;

import dev.userservice.models.User;
import dev.userservice.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository ;

    public CustomUserDetailsService(UserRepository userRepository){
        this.userRepository = userRepository ;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = userRepository.findByEmail(username) ;

        if (userOptional.isEmpty()){
            throw  new UsernameNotFoundException("User with this username does not found") ;
        }

        User user = userOptional.get();

        return new CustomUserDetails(user);
    }
}
