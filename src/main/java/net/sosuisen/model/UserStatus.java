package net.sosuisen.model;

import jakarta.enterprise.context.SessionScoped;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;

@SessionScoped
@Getter
@Setter
public class UserStatus implements Serializable {
    private ArrayList<HistoryDocument> history = new ArrayList<>();
    private String currentPositionTag = "";
    private String userName = "";
}
