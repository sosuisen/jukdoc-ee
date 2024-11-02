package net.sosuisen.model;

import jakarta.enterprise.context.ApplicationScoped;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@ApplicationScoped
public class ChatCommand {
    public enum Command {
        PROCEED_CURRENT_TOPIC,
        REPEAT_ONLY_CURRENT_TOPIC
    }
    Map<String, Command> commandMap = new HashMap<>();
    public ChatCommand() {
        try (InputStream inputStream = ChatCommand.class.getResourceAsStream("/commands.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim().toLowerCase();
                    if(key.endsWith("?")){
                        key = key.substring(0, key.length() - 1);
                    }
                    String value = parts[1].trim();
                    switch (value) {
                        case "proceed_current_topic":
                            commandMap.put(key, Command.PROCEED_CURRENT_TOPIC);
                            break;
                        case "repeat_only_current_topic":
                            commandMap.put(key, Command.REPEAT_ONLY_CURRENT_TOPIC);
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Command getCommand(String key) {
        if (key.length() <= 1) return Command.PROCEED_CURRENT_TOPIC;

        List<String> removeChars = Arrays.asList(
                "。", ".", "、", ",", "？", "?", "！", "!", "ー", "-", "―", "…"
        );
        while (!key.isEmpty()) {
            String lastChar = key.substring(key.length() - 1);
            if (removeChars.contains(lastChar)) {
                key = key.substring(0, key.length() - 1);
            } else {
                break;
            }
        }
        return commandMap.get(key.toLowerCase());
    }

}
