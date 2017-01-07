package com.tadahtech.pub.snixeco.config;

import com.tadahtech.pub.snixeco.Snix;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public enum Lang
{

    CURRENT_AMOUNT("current-amount"),

    NOT_A_COMMAND("not-a-command"),
    NO_PERMISSION("no-permission"),

    NOT_VALID_NUMBER("not-a-valid-number"),

    FAIL_PLAYER_NOT_ONLINE("fail-player-not-online"),
    FAIL_GENERIC("fail-generic"),
    FAIL_NOT_EXIST("fail-not-exist"),

    GIVE_SUCCESS("give-success"),
    GIVE_SUCCESS_TARGET("give-success-target"),

    LOOKUP_SUCCESS("lookup-success"),

    PAY_NOT_ENOUGH_SNIX("pay-not-enough-snix"),
    PAY_SUCCESS_EXECUTOR("pay-success-executor"),
    PAY_SUCCESS_TARGET("pay-success-target"),

    RESET_SUCCESS_EXECUTOR("reset-success-executor"),
    RESET_SUCCESS_TARGET("reset-success-target"),

    TAKE_SUCCESS("take-success");

    public static final String PLAYER = "%player%";
    public static final String AMOUNT = "%amount%";
    public static final String VARIABLE = "%variable%";

    public static String PREFIX;

    private static LangConfig langConfig;

    static
    {
        if (langConfig == null)
        {
            langConfig = Snix.getInstance().getLangConfig();
        }

        if (PREFIX == null)
        {
            PREFIX = langConfig.getMessage("prefix");
            PREFIX = ChatColor.translateAlternateColorCodes('&', PREFIX);
        }
    }

    private String key;

    Lang(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public void sendMessage(CommandSender sender, Map<String, String> args)
    {

        String message = langConfig.getMessage(getKey());

        if (message == null)
        {
            sender.sendMessage(ChatColor.RED + "No message found for the key \"" + ChatColor.YELLOW + getKey() + ChatColor.RED + "\"!");
            return;
        }

        if (args == null || args.isEmpty())
        {
            sender.sendMessage(PREFIX + " " + ChatColor.translateAlternateColorCodes('&', message));
            return;
        }

        for (Entry<String, String> entry : args.entrySet())
        {
            if (message.contains(entry.getKey()))
            {
                message = message.replace(entry.getKey(), entry.getValue());
            }
        }

        message = ChatColor.translateAlternateColorCodes('&', message);

        sender.sendMessage(PREFIX + " " + message);
    }

}
