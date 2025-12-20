package murdockinfotech.shared.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import murdockinfotech.shared.UserDto;

/**
 * Async version of UserService
 */
public interface UserServiceAsync {
  
  void getUser(String name, AsyncCallback<UserDto> callback);
  
  void greetUser(UserDto user, AsyncCallback<String> callback);
}
