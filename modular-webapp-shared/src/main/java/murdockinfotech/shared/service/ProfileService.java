package murdockinfotech.shared.service;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.List;
import murdockinfotech.shared.ProfileDto;

/**
 * GWT RPC service for Profile CRUD.
 */
@RemoteServiceRelativePath("profileService")
public interface ProfileService extends RemoteService {

    List<ProfileDto> listProfiles();

    ProfileDto createProfile(String name);

    ProfileDto updateProfile(ProfileDto profile);

    void deleteProfile(Long id);
}

