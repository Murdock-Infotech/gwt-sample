package murdockinfotech.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sample DTO class that can be shared between client and server
 */
public class UserDto implements IsSerializable {
  
  private String name;
  private String email;
  
  public UserDto() {
    // Required for GWT serialization
  }
  
  public UserDto(String name, String email) {
    this.name = name;
    this.email = email;
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
}


