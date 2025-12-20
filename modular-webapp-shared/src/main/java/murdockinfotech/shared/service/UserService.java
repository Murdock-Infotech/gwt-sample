package murdockinfotech.shared.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import murdockinfotech.shared.UserDto;

/**
 * The client-side stub for the RPC service
 */
@RemoteServiceRelativePath("userService")
public interface UserService extends RemoteService {
  
  UserDto getUser(String name);
  
  String greetUser(UserDto user);
}
