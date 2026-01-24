package murdockinfotech.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import murdockinfotech.server.domain.Profile;
import murdockinfotech.server.repository.ProfileRepository;
import murdockinfotech.shared.ProfileDto;
import murdockinfotech.shared.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Server-side implementation of ProfileService.
 *
 * Note: This servlet is created by the servlet container (not Spring), so we
 * explicitly trigger Spring autowiring in init().
 */
public class ProfileServiceImpl extends RemoteServiceServlet implements ProfileService {

    @Autowired
    private ProfileRepository profileRepository;

    @Override
    public void init() throws ServletException {
        super.init();
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, getServletContext());
    }

    @Override
    public List<ProfileDto> listProfiles() {
        List<Profile> profiles = profileRepository.findAll();
        List<ProfileDto> result = new ArrayList<ProfileDto>(profiles.size());
        for (Profile p : profiles) {
            result.add(toDto(p));
        }
        return result;
    }

    @Override
    public ProfileDto createProfile(String name) {
        String n = name == null ? null : name.trim();
        if (n == null || n.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        Profile p = new Profile();
        p.setName(n);
        Profile saved = profileRepository.save(p);
        return toDto(saved);
    }

    @Override
    public ProfileDto updateProfile(ProfileDto profile) {
        if (profile == null || profile.getId() == null) {
            throw new IllegalArgumentException("Profile id is required");
        }
        String n = profile.getName() == null ? null : profile.getName().trim();
        if (n == null || n.isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }

        Profile existing = profileRepository.findOne(profile.getId());
        if (existing == null) {
            throw new IllegalArgumentException("Profile not found: " + profile.getId());
        }
        existing.setName(n);
        Profile saved = profileRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void deleteProfile(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Profile id is required");
        }
        profileRepository.delete(id);
    }

    private static ProfileDto toDto(Profile p) {
        return new ProfileDto(p.getId(), p.getName());
    }
}

