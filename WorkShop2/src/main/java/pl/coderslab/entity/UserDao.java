package pl.coderslab.entity;


import org.mindrot.jbcrypt.BCrypt;
import pl.coderslab.DbUtil;

import java.sql.*;
import java.util.Arrays;

public class UserDao {
    private static final String CREATE_USER_QUERY = "INSERT INTO users(username,email,password) VALUES(?, ?, ?)";
    private static final String READ_ID_QUERY = "SELECT * FROM users where id= ?";

    private static User createUser(ResultSet resultSet) throws SQLException {
        long userId = resultSet.getLong(Column.ID.name());
        String username = resultSet.getString(Column.USERNAME.name());
        String email = resultSet.getString(Column.EMAIL.name());
        String password = resultSet.getString(Column.PASSWORD.name());
        return new User(userId, username, email, password);
    }
    public User create(User user) {
        try (Connection conn = DbUtil.connection();
             PreparedStatement preparedStatement = conn.prepareStatement(CREATE_USER_QUERY, PreparedStatement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, hashPassword(user.getPassword()));
            preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    long generatedId = resultSet.getLong(1);
                    return new User(generatedId, User.getUserName(), User.getEmail(), User.getPassword());
                } else {
                    throw new RuntimeException("Fail - You dont create new user");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }
    public User readId(long userId) {
        try (Connection conn = DbUtil.connection();
             PreparedStatement stmt = conn.prepareStatement(READ_ID_QUERY)) {
            stmt.setLong(1, userId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return createUser(resultSet);
                } else {
                    throw new UserNotFoundException(userId);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
    private static final String UPDATE_USER_QUERY = "UPDATE users SET username=?, email=?, password= ? where id=?";
    public void updateUser(User user) {
        try (Connection conn = DbUtil.connection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_USER_QUERY)) {
            stmt.setString(1, User.getUserName());
            stmt.setString(2, User.getEmail());
            stmt.setString(3, hashPassword(user.getPassword()));
            stmt.setLong(4, User.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private static final String DELETE_USER_QUERY="DELETE FROM users WHERE id= ? ";
    public void deleteUser(int userId){
        try(Connection conn = DbUtil.connection();
            PreparedStatement stmt = conn.prepareStatement(DELETE_USER_QUERY)){
            stmt.setInt(1, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public static final String FIND_ALL_QUERY="SELECT id,username,email,password FROM users";
    public User[] findAll(){
        User[] usersArr = new User[0];
        try( Connection conn = DbUtil.connection();
             Statement stmt = conn.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(FIND_ALL_QUERY);
            while (resultSet.next()){
                User user1 = new User();
                User.setId( resultSet.getInt("id"));
                User.setUserName(resultSet.getString("username"));
                User.setEmail(resultSet.getString("email"));
                User.setPassword(resultSet.getString("password"));
                usersArr= addToArray(user1,usersArr);
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return usersArr;
    }
    private User[] addToArray(User user1, User[] usersArr){
        User[] newArr = Arrays.copyOf(usersArr,usersArr.length+1);
        newArr[newArr.length-1]=user1;
        usersArr=newArr;
        return usersArr;

    }



    enum Column {
        ID, USERNAME, EMAIL, PASSWORD
    }


}
