package net.sosuisen;

import java.util.regex.Pattern;

public class Constants {
    public static final Pattern PARAGRAPH_PATTERN = Pattern.compile("^\\{(.+?):(.+?):(.+?)}\\s+?(.+)$", Pattern.DOTALL);
    public static final Pattern SUMMARY_PATTERN = Pattern.compile("^\\{(.+?)}\\s+?(.+)$", Pattern.DOTALL);
    public static final Pattern QA_PATTERN = Pattern.compile("^\\[Q]\\s(.+?)\\n\\[A]\\s(.+?)$", Pattern.DOTALL);
}
