/*
 * This file is generated by jOOQ.
 */
package dev.armadeus.bot.database.tables.records;


import dev.armadeus.bot.database.tables.Guilds;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Row2;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class GuildsRecord extends UpdatableRecordImpl<GuildsRecord> implements Record2<Long, String> {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>public.guilds.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.guilds.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>public.guilds.config</code>.
     */
    public void setConfig(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.guilds.config</code>.
     */
    public String getConfig() {
        return (String) get(1);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row2<Long, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }

    @Override
    public Row2<Long, String> valuesRow() {
        return (Row2) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return Guilds.GUILDS.ID;
    }

    @Override
    public Field<String> field2() {
        return Guilds.GUILDS.CONFIG;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public String component2() {
        return getConfig();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public String value2() {
        return getConfig();
    }

    @Override
    public GuildsRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public GuildsRecord value2(String value) {
        setConfig(value);
        return this;
    }

    @Override
    public GuildsRecord values(Long value1, String value2) {
        value1(value1);
        value2(value2);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached GuildsRecord
     */
    public GuildsRecord() {
        super(Guilds.GUILDS);
    }

    /**
     * Create a detached, initialised GuildsRecord
     */
    public GuildsRecord(Long id, String config) {
        super(Guilds.GUILDS);

        setId(id);
        setConfig(config);
    }
}
