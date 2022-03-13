import java.io.Serializable;


public class Reply implements Serializable{

    private String message;

    public Reply(String message) {
        this.message= message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    

    @Override
    public String toString() {
        return "Auth [message=" + message + "]";
    }
    
}