package com.tadahtech.pub.snixeco.config;

import com.tadahtech.pub.snixeco.Snix;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class LangConfig
{

    private final File file;
    private final FileConfiguration config;

    public LangConfig()
    {
        Snix plugin = Snix.getInstance();
        this.file = new File(plugin.getDataFolder(), "lang.yml");

        if (!file.exists())
        {
            plugin.saveResource(file.getName(), true);
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        Snix.getInstance().debug(String.valueOf(this.config.getKeys(true)));
    }

    public void setMessage(String key, String message)
    {
        this.config.set(key, message);
        try
        {
            this.config.save(file);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public String getMessage(String key)
    {
        return config.getString(key);
    }

}
