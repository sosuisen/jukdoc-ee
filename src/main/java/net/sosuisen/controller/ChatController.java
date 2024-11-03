package net.sosuisen.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.sosuisen.model.ReadingRecordDAO;
import net.sosuisen.model.UserStatus;

import java.util.UUID;

@Controller
@RequestScoped
@NoArgsConstructor(force = true)
@RequiredArgsConstructor(onConstructor_ = @Inject)
@Path("/")
public class ChatController {
    private final Models models;
    private final UserStatus userStatus;
    private final ReadingRecordDAO readingRecordDAO;

    @GET
    public String home() {
        double readingRate = 0.0;
        if (userStatus.getUserName() == null || userStatus.getUserName().isEmpty()) {
            var useName = UUID.randomUUID().toString();
            userStatus.setUserName(useName);
        }
        models.put("userName", userStatus.getUserName().substring(0, 8));
        return "index.jsp";
    }
}
