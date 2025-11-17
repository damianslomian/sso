package de.websso;

import java.util.Map;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

//    @GetMapping("/me")
//    public Map<String, Object> me(@AuthenticationPrincipal OidcUser user) {
//        System.out.println("=== AUTHORITIES ===");
//        user.getAuthorities().forEach(a -> System.out.println(" - " + a.getAuthority()));
//
//        System.out.println("=== CLAIMS ===");
//        user.getClaims().forEach((k, v) -> System.out.println(k + " = " + v));
//
//        return user.getClaims();
//    }
}