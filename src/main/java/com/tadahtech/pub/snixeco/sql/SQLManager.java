package com.tadahtech.pub.snixeco.sql;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tadahtech.pub.snixeco.Snix;
import com.tadahtech.pub.snixeco.player.PlayerInfo;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SQLManager
{

    private static final String TABLE = "snix_players";
    private static final String ID = "player";
    private static final String AMOUNT = "snix";
    private static final String NAME = "last_known_name";

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `" + TABLE + "` (`" + ID + "` VARCHAR(64) PRIMARY KEY NOT NULL, `" + AMOUNT + "` INT, `" + NAME + "` VARCHAR(16));";

    private static final String GET_PLAYER = "SELECT * FROM `" + TABLE + "` WHERE `" + ID + "` = ?";
    private static final String SAVE_PLAYER = "INSERT INTO `" + TABLE + "` VALUES (?,?,?) ON DUPLICATE KEY UPDATE `" + NAME + "` = ?, `" + AMOUNT + "` = ?";
    private static final String UPDATE_NAME = "UPDATE `" + TABLE + "` SET `" + NAME + "` = ? WHERE `" + ID + "` = ?";

    private static final String GET_SNIX = "SELECT `" + AMOUNT + "` FROM `" + TABLE + "` WHERE `" + NAME + "` = ?";
    private static final String SET_SNIX = "UPDATE `" + TABLE + "` SET `" + AMOUNT + "` = ? WHERE `" + NAME + "` = ?";
    private static final String ADD_SNIX = "UPDATE `" + TABLE + "` SET `" + AMOUNT + "` = " + AMOUNT + " + ? WHERE `" + NAME + "` = ?";
    private static final String SUBTRACT_SNIX = "UPDATE `" + TABLE + "` SET `" + AMOUNT + "` = " + AMOUNT + " - ? WHERE `" + NAME + "` = ?";

    private final Snix plugin;
    private final HikariDataSource dataSource;

    private final Cache<String, Integer> SNIX_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).build();

    public SQLManager()
    {
        this.plugin = Snix.getInstance();
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection sql = config.getConfigurationSection("sql");

        String host = sql.getString("host");
        int port = sql.getInt("port", 3306);
        String database = sql.getString("database");
        String user = sql.getString("user");
        String password = sql.getString("password");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setDriverClassName("com.mysql.jdbc.Driver");
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(password);
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTimeout(10000);

        this.dataSource = new HikariDataSource(hikariConfig);

        runAsync(() ->
        {
            try (Connection connection = getConnection())
            {
                connection.prepareStatement(CREATE_TABLE).execute();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        });
    }

    /**
     * Fetches a player from the MySQL database on a separate thread
     *
     * @param uuid     The player's UUID
     * @param name     The player's current name
     * @param callback The callback which will supply the new player's info
     */
    public void getPlayerAsync(UUID uuid, String name, Callback<PlayerInfo> callback)
    {
        runAsync(() ->
        {
            SQLStatement statement = new SQLStatement(GET_PLAYER).set(uuid);

            getResultSet(statement, resultSet ->
            {
                try
                {
                    if (resultSet == null || !resultSet.next())
                    {
                        Snix.getInstance().debug("Error with the result set!");
                        runSync(() -> callback.call(new PlayerInfo(uuid, name, 0)));
                        return;
                    }

                    String lastKnown = resultSet.getString(NAME);

                    if (!name.equalsIgnoreCase(lastKnown))
                    {
                        plugin.debug("Last known name and Current name for player \"" + name + "\" do not match! (Last name was \"" + lastKnown + "\") Updating the DB to reflect this change");
                        lastKnown = name;

                        try (Connection connection = getConnection())
                        {
                            new SQLStatement(UPDATE_NAME).set(name).set(uuid).prepare(connection).execute();
                        }
                    }

                    int snix = resultSet.getInt(AMOUNT);

                    Snix.getInstance().debug("Loaded " + lastKnown + "'s data with " + snix + " snix!");

                    callback.call(new PlayerInfo(uuid, name, snix));

                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * Saves a player's info into MySQL and removes him from this servers memory
     *
     * @param info The Player's info
     */
    public void savePlayer(PlayerInfo info)
    {
        SQLStatement statement = new SQLStatement(SAVE_PLAYER)
          .set(info.getUuid())
          .set(info.getSnix())
          .set(info.getLastKnownName())
          .set(info.getLastKnownName())
          .set(info.getSnix());

        runAsync(() ->
        {
            try (Connection connection = getConnection())
            {
                Snix.getInstance().debug("Saved " + info.getLastKnownName() + " with " + info.getSnix() + " Snix!");
                statement.prepare(connection).execute();
                info.scrub();
            } catch (SQLException e)
            {
                e.printStackTrace();
            }
        });

    }

    /**
     * Retrieves a player's current snix count from the DB and inserts it into a cache
     *
     * @param name     The name of the player
     * @param callback The callback which will contain either the value of the player's DB/Cache stored currency, or -1 of am error occurred
     */
    public void getSnix(String name, Callback<Integer> callback)
    {
        if (SNIX_CACHE.getIfPresent(name) != null)
        {
            callback.call(SNIX_CACHE.getIfPresent(name));
            return;
        }

        SQLStatement statement = new SQLStatement(GET_SNIX).set(name);

        runAsync(() ->
        {
            getResultSet(statement, resultSet ->
            {
                try
                {
                    if (resultSet == null || !resultSet.next())
                    {
                        runSync(() -> callback.call(-1));
                        return;
                    }

                    int snix = resultSet.getInt(1);

                    runSync(() -> callback.call(snix));
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            });
        });
    }

    /**
     * Update a Player's Snix value in the DB
     * This will also insert the player into the cache or update his current value
     *
     * @param player     The player's name
     * @param delta      The change to be applied to the player's snix value
     * @param updateType The type of updating to execute
     * @param callback   The callback which will contain the result of the operation
     */
    public void updateSnix(String player, int delta, SnixUpdateType updateType, Callback<Integer> callback)
    {
        SQLStatement statement = null;

        switch (updateType)
        {
            case ADD:
                statement = new SQLStatement(ADD_SNIX).set(delta).set(player);
                break;
            case SET:
                statement = new SQLStatement(SET_SNIX).set(delta).set(player);
                break;
            case SUBTRACT:
                statement = new SQLStatement(SUBTRACT_SNIX).set(delta).set(player);
                break;
        }

        SQLStatement finalStatement = statement;

        runAsync(() ->
        {
            try (Connection connection = getConnection())
            {
                ResultSet res = new SQLStatement(GET_SNIX).set(player).prepare(connection).getResultSet();

                if (res == null || !res.next())
                {
                    callback.call(-2);
                    return;
                }

                finalStatement.prepare(connection).execute();

                ResultSet resultSet = new SQLStatement(GET_SNIX).set(player).prepare(connection).getResultSet();

                int snix = resultSet.getInt(1);

                runSync(() ->
                {
                    SNIX_CACHE.put(player, snix);
                    callback.call(snix);
                });

            } catch (SQLException e)
            {
                e.printStackTrace();
                callback.call(-1);
            }
        });
    }

    private Connection getConnection()
    {
        try
        {
            return dataSource.getConnection();
        } catch (SQLException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private void getResultSet(SQLStatement query, Callback<ResultSet> callback)
    {
        try (Connection connection = getConnection())
        {
            PreparedStatement preparedStatement = query.prepare(connection);
            preparedStatement.execute();
            callback.call(preparedStatement.getResultSet());
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void runAsync(Runnable runnable)
    {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private void runSync(Runnable runnable)
    {
        this.plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    public enum SnixUpdateType
    {
        ADD,
        SUBTRACT,
        SET
    }

}
