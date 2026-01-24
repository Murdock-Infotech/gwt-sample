package murdockinfotech.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DTO for Profile CRUD over GWT RPC.
 */
public class ProfileDto implements IsSerializable {

    private Long id;
    private String name;

    public ProfileDto() {
        // Required for GWT serialization
    }

    public ProfileDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

