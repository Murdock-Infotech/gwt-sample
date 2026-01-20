package murdockinfotech.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import murdockinfotech.shared.service.UserService;
import murdockinfotech.shared.UserDto;

/**
 * Server-side implementation of UserService
 */
public class UserServiceImpl extends RemoteServiceServlet implements UserService {
  
  @Override
  public UserDto getUser(String name) {
    // Sample implementation
    return new UserDto(name, name.toLowerCase().replace(" ", ".") + "@example.com");
  }
  
  @Override
  public String greetUser(UserDto user) {
    return "Hello " + user.getName() + "! Your email is " + user.getEmail();
  }
}

