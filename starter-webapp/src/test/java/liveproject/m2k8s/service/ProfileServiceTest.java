package liveproject.m2k8s.service;

import liveproject.m2k8s.Profile;
import liveproject.m2k8s.data.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


public class ProfileServiceTest {
    private ProfileRepository profileRepository = Mockito.mock(ProfileRepository.class);
    private ProfileService profileService;

    @BeforeEach
    public void setup() {
        profileService = new ProfileService(profileRepository);
        Profile zasu = Profile.builder()
                .id(1L)
                .firstName("Zasu")
                .lastName("Pitts")
                .email("zasu@hollywood.com")
                .username("zasupitts")
                .password("changeme")
                .build();
        when(profileRepository.findByUsername("zasupitts")).thenReturn(zasu);
    }

    @Test
    public void test_getProfile() {
        Profile zasupitts = profileService.getProfile("zasupitts");
        assertEquals("Zasu", zasupitts.getFirstName());
    }
}
