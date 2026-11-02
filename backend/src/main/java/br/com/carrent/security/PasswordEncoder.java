package br.com.carrent.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder implements org.springframework.security.crypto.password.PasswordEncoder {

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(CharSequence senhaPura) {
        return delegate.encode(senhaPura);
    }

    @Override
    public boolean matches(CharSequence senhaPura, String senhaCodificada) {
        return delegate.matches(senhaPura, senhaCodificada);
    }

    @Override
    public boolean upgradeEncoding(String senhaCodificada) {
        return delegate.upgradeEncoding(senhaCodificada);
    }
}
