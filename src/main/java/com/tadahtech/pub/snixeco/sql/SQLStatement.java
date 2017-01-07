package com.tadahtech.pub.snixeco.sql;

import com.google.common.collect.Lists;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * @author Timothy Andis (TadahTech) on 3/27/2016.
 */
public class SQLStatement
{

    /**
     * The initial string literal statement
     */
    private String base;

    /**
     * The list for inserting objects into a prepared statement
     */
    private List<Object> objects;

    /**
     * Create a new instance of an SQLStatement
     *
     * @param base The literal string MySQL query ('?' are allowed)
     */
    public SQLStatement(String base)
    {
        this.base = base;
        this.objects = Lists.newArrayList();
    }

    /**
     * Set an object to a value for inserting upon completion
     *
     * @param object The object to insert into the statement for the given position
     * @return The local instance with an updated map
     */
    public SQLStatement set(Object object)
    {
        this.objects.add(object);
        return this;
    }

    /**
     * Prepare a fully built statement for running
     *
     * @param connection The connection used to establish the statement
     * @return The fully built PreparedStatement
     * @throws SQLException
     */
    public PreparedStatement prepare(Connection connection) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement(base);

        for (int slot = 0; slot < objects.size(); slot++)
        {
            Object object = objects.get(slot);

            if (object instanceof UUID)
            {
                statement.setObject(slot + 1, object.toString());
                continue;
            }

            statement.setObject(slot + 1, object);
        }

        this.objects.clear();

        return statement;
    }

}
