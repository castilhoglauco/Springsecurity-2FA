package com.baeldung.lss.web.controller;

import javax.validation.Valid;

import com.baeldung.lss.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.baeldung.lss.service.IUserService;
import com.baeldung.lss.validation.EmailExistsException;
import com.baeldung.lss.web.model.User;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Controller
class RegistrationController {

    private static String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";
    private static String APP_NAME = "SpringSecurity-2FA";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IUserService userService;

    @RequestMapping(value = "signup")
    public ModelAndView registrationForm() {
        return new ModelAndView("registrationPage", "user", new User());
    }

    @RequestMapping(value = "user/register")
    public ModelAndView registerUser(@Valid final User user, final BindingResult result) {
        if (result.hasErrors()) {
            return new ModelAndView("registrationPage", "user", user);
        }
        try {
            userService.registerNewUser(user);
        } catch (final EmailExistsException e) {
            result.addError(new FieldError("user", "email", e.getMessage()));
            return new ModelAndView("registrationPage", "user", user);
        }
        String url = getQRUrl(user.getEmail());
        return new ModelAndView("qrcode", "url", url);
    }



    public String getQRUrl(String username){
        String url;
        final User user = userRepository.findByEmail(username);
        if (user == null) {
            url = "";
        } else {
            try {
                url = generateQRUrl(user.getSecret(), user.getEmail());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                url = "";
            }
        }
        return url;
    }

    private String generateQRUrl(String secret, String username) throws UnsupportedEncodingException {
        return QR_PREFIX + URLEncoder.encode(String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s", APP_NAME, username, secret, APP_NAME), "UTF-8");
    }

}
