package fiap.service;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.*;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import fiap.response.LoginResponse;
import fiap.request.LoginRequest;
import fiap.utils.JwtUtil;
import java.io.IOException;
import java.util.Map;

public class AuthService {
    private final String userPoolId;
    private final String clientId;
    private final AWSSecretsManager secretsManager = AWSSecretsManagerClientBuilder.defaultClient();
    private final AWSCognitoIdentityProvider cognitoClient = AWSCognitoIdentityProviderClientBuilder.defaultClient();
    private final JwtUtil jwtUtil = new JwtUtil();

    public AuthService() {
        Map<String, String> secretValues = getSecretValues();
        this.userPoolId = secretValues.get("UserPoolId");
        this.clientId = secretValues.get("UserPoolClientId");
    }

    public LoginResponse handleAuth(LoginRequest request) {
        validateRequest(request);

        String email = request.getEmail();
        String name = request.getName();
        String password = request.getPassword();
        String confirmationCode = request.getConfirmationCode();
        if (!userExists(email)) {
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException("Nome é obrigatório para cadastro.");
            }
            signUpUser(name, email, password);
            return new LoginResponse("Cadastro realizado. Por favor, confirme seu e-mail.");
        }

        if (!isUserConfirmed(email)) {
            if (!confirmationCode.isEmpty()) {
                confirmUser(email, confirmationCode);
            }else{
            sendConfirmationCode(email);
            throw new RuntimeException("Usuário não confirmado. Um novo código de confirmação foi enviado.");
            }
        }

        authenticate(email, password);
        String token = jwtUtil.generateToken(email, name != null ? name : "Usuário");
        return new LoginResponse(token);
    }

    private void validateRequest(LoginRequest request) {
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório.");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Senha é obrigatória.");
        }
    }

    private boolean userExists(String email) {
        try {
            cognitoClient.adminGetUser(new AdminGetUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(email));
            return true;
        } catch (UserNotFoundException e) {
            return false;
        }
    }

    private boolean isUserConfirmed(String email) {
        try {
            AdminGetUserRequest request = new AdminGetUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(email);

            AdminGetUserResult response = cognitoClient.adminGetUser(request);

            return "CONFIRMED".equals(response.getUserStatus());
        } catch (Exception e) {
            return false;
        }
    }



    private void sendConfirmationCode(String email) {
        ResendConfirmationCodeRequest resendRequest = new ResendConfirmationCodeRequest()
                .withClientId(clientId)
                .withUsername(email);
        cognitoClient.resendConfirmationCode(resendRequest);
    }

    private void authenticate(String email, String password) {
        InitiateAuthRequest authRequest = new InitiateAuthRequest()
                .withAuthFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .withClientId(clientId)
                .withAuthParameters(Map.of(
                        "USERNAME", email,
                        "PASSWORD", password
                ));

        try {
            cognitoClient.initiateAuth(authRequest);
        } catch (NotAuthorizedException | UserNotFoundException e) {
            throw new RuntimeException("Credenciais inválidas.", e);
        }
    }

    private void signUpUser(String name, String email, String password) {
        SignUpRequest signUpRequest = new SignUpRequest()
                .withClientId(clientId)
                .withUsername(email)
                .withPassword(password)
                .withUserAttributes(
                        new AttributeType().withName("name").withValue(name),
                        new AttributeType().withName("email").withValue(email)
                );

        try {
            cognitoClient.signUp(signUpRequest);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao cadastrar usuário.", e);
        }
    }

    private Map<String, String> getSecretValues() {
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId("CognitoIds");
        GetSecretValueResult getSecretValueResult = secretsManager.getSecretValue(getSecretValueRequest);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(getSecretValueResult.getSecretString(), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao obter valores do Secrets Manager", e);
        }
    }

    public LoginResponse confirmUser(String email, String confirmationCode) {
        ConfirmSignUpRequest confirmRequest = new ConfirmSignUpRequest()
                .withClientId(clientId)
                .withUsername(email)
                .withConfirmationCode(confirmationCode);

        try {
            cognitoClient.confirmSignUp(confirmRequest);
            return new LoginResponse("Usuário confirmado com sucesso.");
        } catch (CodeMismatchException e) {
            throw new RuntimeException("Código de confirmação inválido.", e);
        } catch (ExpiredCodeException e) {
            throw new RuntimeException("Código expirado. Solicite um novo.", e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao confirmar usuário.", e);
        }
    }
}

