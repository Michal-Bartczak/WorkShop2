package pl.coderslab.entity;

public class UserNotFoundException extends RuntimeException{
    private static final String MSG = "USER for given id %s not found";

    public UserNotFoundException(long id) {
        super(String.format(MSG, id));
    }
}
