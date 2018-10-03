package com.baeldung.lss.security;

import com.baeldung.lss.persistence.UserRepository;
import com.baeldung.lss.web.model.User;
import org.jboss.aerogear.security.otp.Totp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private UserRepository userRepository;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        String verificationCode = ((CustomWebAuthenticationDetails) authentication.getDetails()).getVerificationCode();

        User user = userRepository.findByEmail(username);
        if ((user == null ) || !user.getPassword().equals(password)) {
            throw new BadCredentialsException("Invalid username or password");
        }

        final Totp totp = new Totp(user.getSecret());
        try {
            if (!totp.verify(verificationCode)) {
                throw new BadCredentialsException("Invalid verfication code");
            }
        } catch (final Exception e) {
            throw new BadCredentialsException("Invalid verfication code");
        }


        return new UsernamePasswordAuthenticationToken(user, password, Arrays.asList(new SimpleGrantedAuthority("ROLE_USER")));

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class );
    }
}
