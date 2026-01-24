package murdockinfotech.shared.service;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;
import murdockinfotech.shared.ProfileDto;

/**
 * Async version of ProfileService.
 */
public interface ProfileServiceAsync {

    void listProfiles(AsyncCallback<List<ProfileDto>> callback);

    void createProfile(String name, AsyncCallback<ProfileDto> callback);

    void updateProfile(ProfileDto profile, AsyncCallback<ProfileDto> callback);

    void deleteProfile(Long id, AsyncCallback<Void> callback);
}

