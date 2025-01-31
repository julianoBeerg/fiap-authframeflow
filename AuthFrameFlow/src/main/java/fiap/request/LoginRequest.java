package fiap.request;

public class LoginRequest {
    private String name;
    private String email;
    private String password;
    private String confirmationCode;

    public LoginRequest() {
    }

    public LoginRequest(String name, String email, String password, String confirmationCode) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.confirmationCode = confirmationCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }
}
