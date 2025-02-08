package fiap.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class JwtUtil {
    private final Algorithm algorithm = Algorithm.HMAC256("jwtSecretKey");
    private final Date expirationDate = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 3); // 3 horas

    public String generateToken(int id, String email, String name) {
        return JWT.create()
                .withClaim("id", id)
                .withClaim("email", email)
                .withClaim("name", name)
                .withExpiresAt(expirationDate)
                .sign(algorithm);
    }
}
