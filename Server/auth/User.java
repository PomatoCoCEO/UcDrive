package auth;

public class User implements Comparable {
    private String username;
    private String passwordHash;
    private String lastDir;
    public User(String username, String passwordHash, String lastDir) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.lastDir = lastDir;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    public String getLastDir() {
        return lastDir;
    }
    public void setLastDir(String lastDir) {
        this.lastDir = lastDir;
    }
    @Override
    public String toString() {
        return username + " "+passwordHash + " "+lastDir;
    }
    @Override
    public int compareTo(Object o) {
        // TODO Auto-generated method stub
        if (! (o instanceof User)) throw new ClassCastException("Invalid object cast");
        User u = (User) o;
        return this.username.compareTo(u.username);
    }
    
}
