package fiap.repository;

import fiap.model.User;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Repository
public class UserRepository {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = System.getenv("BASE_URL");

    public User findByEmail(String email) {
        try {
            return restTemplate.getForObject(baseUrl + "email/" + email, User.class);
        } catch (Exception e) {
            return null;
        }
    }

    public User createUser(String username, String email, String password) {
        String encodedPassword = Base64.getEncoder().encodeToString(password.getBytes());

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(encodedPassword);

        try {
            return restTemplate.postForObject(baseUrl, user, User.class);
        } catch (Exception e) {
            return null;
        }
    }
}
