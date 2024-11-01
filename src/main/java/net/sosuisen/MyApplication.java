package net.sosuisen;

import jakarta.mvc.security.Csrf;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.HashMap;
import java.util.Map;

@ApplicationPath("/jukdoc")
public class MyApplication extends Application {
    @Override
    public Map<String, Object> getProperties() {
        final Map<String, Object> map = new HashMap<>();
        map.put(Csrf.CSRF_PROTECTION, Csrf.CsrfOptions.IMPLICIT);
        return map;
    }
}
