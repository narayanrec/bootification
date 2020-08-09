package liveproject.m2k8s.web;

import liveproject.m2k8s.Profile;
import liveproject.m2k8s.service.ProfileService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Slf4j
@RequestMapping("/profile")
public class ProfileController {

    private ProfileService profileService;

    @Value("${images.directory:/tmp}")
    private String uploadFolder;

    @Value("classpath:ghost.jpg")
    private Resource defaultImage;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity processRegistration(
            @Valid @RequestBody Profile profile,
            Errors errors) {
        if (errors.hasErrors()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        profileService.save(profile);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @GetMapping(value = "/{username}")
    public ResponseEntity<Profile> showProfile(@PathVariable String username) {
        log.debug("Reading model for: "+username);
        Profile profile = profileService.getProfile(username);
        if(profile == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok(profile);
    }

    @GetMapping(value = "/{username}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] showProfileImage(@PathVariable String username) throws IOException {
        log.debug("Reading model for: "+username);
        InputStream in = null;

        try {
            Profile profile = profileService.getProfile(username);
            if(profile == null || StringUtils.isEmpty(profile.getImageFileName()))
                in = defaultImage.getInputStream();
            else
                in = new FileInputStream(profile.getImageFileName());

            return IOUtils.toByteArray(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(in != null) in.close();
        }
        return null;
    }

    @PutMapping(value = "/{username}")
    @Transactional
    public ResponseEntity updateProfile(@PathVariable String username, @RequestBody @Valid Profile profile, Errors errors) {
        log.debug("Updating model for: "+username);
        if (errors.hasErrors()) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        Profile dbProfile = profileService.getProfile(username);
        boolean dirty = false;
        if (!StringUtils.isEmpty(profile.getEmail())
                && !profile.getEmail().equals(dbProfile.getEmail())) {
            dbProfile.setEmail(profile.getEmail());
            dirty = true;
        }
        if (!StringUtils.isEmpty(profile.getFirstName())
                && !profile.getFirstName().equals(dbProfile.getFirstName())) {
            dbProfile.setFirstName(profile.getFirstName());
            dirty = true;
        }
        if (!StringUtils.isEmpty(profile.getLastName())
                && !profile.getLastName().equals(dbProfile.getLastName())) {
            dbProfile.setLastName(profile.getLastName());
            dirty = true;
        }
        if (dirty) {
            profileService.save(dbProfile);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{username}/image")
    @Transactional
    public ResponseEntity uploadImage(@PathVariable String username, @RequestParam("file") MultipartFile file) {
        log.debug("Updating image for: "+username);
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        String fileName = file.getOriginalFilename();
        if (!(fileName.endsWith("jpg") || fileName.endsWith("JPG"))) {
            return ResponseEntity.badRequest().build();
        }
        try {
            final String contentType = file.getContentType();
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(uploadFolder, username+".jpg");
            Files.write(path, bytes);
            Profile profile = profileService.getProfile(username);
            profile.setImageFileName(path.toString());
            profile.setImageFileContentType(contentType);
            profileService.save(profile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(null);
    }

}
