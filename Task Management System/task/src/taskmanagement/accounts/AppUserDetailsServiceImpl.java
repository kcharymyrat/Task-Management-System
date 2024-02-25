package taskmanagement.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import taskmanagement.dtos.RegistrationRequest;

import java.util.Optional;

@Service
public class AppUserDetailsServiceImpl implements UserDetailsService {
    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AppUserDetailsServiceImpl(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = repository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Not Found"));

        return new AppUserDetailsImpl(user);
    }
    
    public Optional<AppUser> findByUsernameIgnoreCase(String username) {
        return repository.findByUsernameIgnoreCase(username);
    }

    public void registerUser(String email, String password) {
        AppUser user = new AppUser();
        user.setUsername(email.trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(password.trim().toLowerCase()));
        user.setAuthority("ROLE_USER");
        repository.save(user);
    }
}
