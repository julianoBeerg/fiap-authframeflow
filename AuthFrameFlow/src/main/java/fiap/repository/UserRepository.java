package fiap.repository;

import fiap.model.UserModel;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

@Repository
public class UserRepository {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String baseUrl = System.getenv("BASE_URL");

    public UserModel findByEmail(String email) {
        try {
            return restTemplate.getForObject(baseUrl+ "email/"+ email, UserModel.class);
        } catch (Exception e) {
            return null;
        }
    }

    public UserModel createUser(String username, String email, String password) {

        String json = String.format(
                "{\n" +
                        "  \"username\": \"%s\",\n" +
                        "  \"email\": \"%s\",\n" +
                        "  \"password\": \"%s\",\n" +
                        "  \"authorities\": [],\n" +
                        "  \"enabled\": true,\n" +
                        "  \"accountNonLocked\": true,\n" +
                        "  \"credentialsNonExpired\": true,\n" +
                        "  \"accountNonExpired\": true\n" +
                        "}",
                username, email, password
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(json, headers);

        try {
            return restTemplate.postForObject(baseUrl, requestEntity, UserModel.class);
        } catch (Exception e) {
            return null;
        }
    }

}
