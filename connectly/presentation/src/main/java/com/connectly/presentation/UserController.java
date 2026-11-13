package com.connectly.presentation;

import com.connectly.application.UseCases.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/users")
class UserController {

    record UpdateProfileRequest(@NotBlank @Size(max = 60) String firstName,
                                @NotBlank @Size(max = 60) String lastName,
                                @Size(max = 300) String bio) {}
    record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 72) String newPassword) {}
    record DeactivateRequest(@NotBlank String password) {}

    private final GetProfile profile;
    private final GetUserPosts userPosts;
    private final FollowUser follow;
    private final UserOps users;

    UserController(GetProfile profile, GetUserPosts userPosts, FollowUser follow, UserOps users) {
        this.profile = profile; this.userPosts = userPosts; this.follow = follow; this.users = users;
    }

    // ---- trenutni korisnik ----

    @GetMapping("/me")
    MeView me(@AuthenticationPrincipal Long userId) { return users.me(userId); }

    @PutMapping("/me")
    MeView updateMe(@AuthenticationPrincipal Long userId, @Valid @RequestBody UpdateProfileRequest r) {
        return users.updateProfile(userId, new UpdateProfileCommand(r.firstName(), r.lastName(), r.bio()));
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    MeView updatePhoto(@AuthenticationPrincipal Long userId, @RequestParam("photo") MultipartFile photo) throws IOException {
        return users.updatePhoto(userId, Uploads.one(photo));
    }

    @PutMapping("/me/password") @ResponseStatus(HttpStatus.NO_CONTENT)
    void changePassword(@AuthenticationPrincipal Long userId, @Valid @RequestBody ChangePasswordRequest r) {
        users.changePassword(userId, r.currentPassword(), r.newPassword());
    }

    @PostMapping("/me/deactivate") @ResponseStatus(HttpStatus.NO_CONTENT)
    void deactivate(@AuthenticationPrincipal Long userId, @Valid @RequestBody DeactivateRequest r) {
        users.deactivate(userId, r.password());
    }

    // ---- pretraga i predlozi ----

    @GetMapping("/search")
    List<UserSummary> search(@AuthenticationPrincipal Long userId, @RequestParam("q") String q,
                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return users.search(q, userId, page, size);
    }

    @GetMapping("/suggestions")
    List<UserSummary> suggestions(@AuthenticationPrincipal Long userId, @RequestParam(defaultValue = "5") int limit) {
        return users.suggestions(userId, limit);
    }

    // ---- javni profil ----

    @GetMapping("/{username}")
    ProfileView profile(@AuthenticationPrincipal Long viewerId, @PathVariable String username) {
        return profile.get(username, viewerId);
    }

    @GetMapping("/{username}/posts")
    List<PostView> posts(@AuthenticationPrincipal Long viewerId, @PathVariable String username,
                         @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return userPosts.byUser(username, viewerId, page, size);
    }

    @GetMapping("/{id}/followers")
    List<UserSummary> followers(@AuthenticationPrincipal Long viewerId, @PathVariable Long id,
                                @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size) {
        return users.followers(id, viewerId, page, size);
    }

    @GetMapping("/{id}/following")
    List<UserSummary> following(@AuthenticationPrincipal Long viewerId, @PathVariable Long id,
                                @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "30") int size) {
        return users.following(id, viewerId, page, size);
    }

    @PostMapping("/{id}/follow")
    FollowResult follow(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return follow.follow(userId, id); }

    @DeleteMapping("/{id}/follow")
    FollowResult unfollow(@AuthenticationPrincipal Long userId, @PathVariable Long id) { return follow.unfollow(userId, id); }
}
