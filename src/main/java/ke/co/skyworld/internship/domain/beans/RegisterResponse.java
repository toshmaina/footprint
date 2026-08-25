package ke.co.skyworld.internship.domain.beans;

public class RegisterResponse {

    private long userAccountId;
    private String message;

    public RegisterResponse() {
    }

    public RegisterResponse(long userAccountId, String message) {
        this.userAccountId = userAccountId;
        this.message = message;
    }

    public long getUserAccountId() {
        return userAccountId;
    }

    public void setUserAccountId(long userAccountId) {
        this.userAccountId = userAccountId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}