package taskmanagement.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
public class AccountsController {

    private final AppUserDetailsServiceImpl userService;

    @Autowired
    public AccountsController(AppUserDetailsServiceImpl userService) {
        this.userService = userService;
    }

    @PostMapping("api/accounts")
    public ResponseEntity<Void> register(@RequestBody RegistrationRequest request) {
        // check for the request
        if (request == null || request.email() == null || request.email().isBlank() || request.password() == null || request.password().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String email = request.email().trim().toLowerCase();
        String password = request.password().trim();

        // check if the email is valid
        if (!isValidEmail(email)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // password shall be at least 8 char long
        if (password.length() < 6){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // Check whether a user with username exists
        if (userService.findByUsernameIgnoreCase(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // create new user
        userService.registerUser(email, password);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    private boolean isValidEmail(String email) {
        final String EMAIL_REGEX =
                "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        final Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

}
