package com.tadahtech.pub.snixeco;

import com.tadahtech.pub.snixeco.command.CommandHandler;
import com.tadahtech.pub.snixeco.config.LangConfig;
import com.tadahtech.pub.snixeco.listener.PlayerListener;
import com.tadahtech.pub.snixeco.sql.SQLManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 */
public class Snix extends JavaPlugin
{

    private static Snix instance;
    private boolean debug;
    private CommandHandler commandHandler;
    private SQLManager sqlManager;
    private LangConfig langConfig;

    public static Snix getInstance()
    {
        return instance;
    }

    @Override
    public void onEnable()
    {
        instance = this;
        saveDefaultConfig();
        this.debug = getConfig().getBoolean("debug");

        this.sqlManager = new SQLManager();
        this.commandHandler = new CommandHandler();
        this.langConfig = new LangConfig();

        getCommand("snix").setExecutor(this.commandHandler);
        getServer().getPluginManager().registerEvents(new PlayerListener(this.sqlManager), this);
    }

    public void debug(String message)
    {
        if (isDebug())
        {
            getLogger().info(message);
        }
    }

    public boolean isDebug()
    {
        return debug;
    }

    public CommandHandler getCommandHandler()
    {
        return commandHandler;
    }

    public SQLManager getSqlManager()
    {
        return sqlManager;
    }

    public LangConfig getLangConfig()
    {
        return langConfig;
    }
}
