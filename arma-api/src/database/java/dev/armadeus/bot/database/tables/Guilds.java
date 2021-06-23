/*
 * This file is generated by jOOQ.
 */
package dev.armadeus.bot.database.tables;


import dev.armadeus.bot.database.Keys;
import dev.armadeus.bot.database.Public;
import dev.armadeus.bot.database.tables.records.GuildsRecord;

import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Guilds extends TableImpl<GuildsRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>public.guilds</code>
     */
    public static final Guilds GUILDS = new Guilds();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<GuildsRecord> getRecordType() {
        return GuildsRecord.class;
    }

    /**
     * The column <code>public.guilds.id</code>.
     */
    public final TableField<GuildsRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false), this, "");

    /**
     * The column <code>public.guilds.config</code>.
     */
    public final TableField<GuildsRecord, String> CONFIG = createField(DSL.name("config"), SQLDataType.CLOB.nullable(false), this, "");

    private Guilds(Name alias, Table<GuildsRecord> aliased) {
        this(alias, aliased, null);
    }

    private Guilds(Name alias, Table<GuildsRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    /**
     * Create an aliased <code>public.guilds</code> table reference
     */
    public Guilds(String alias) {
        this(DSL.name(alias), GUILDS);
    }

    /**
     * Create an aliased <code>public.guilds</code> table reference
     */
    public Guilds(Name alias) {
        this(alias, GUILDS);
    }

    /**
     * Create a <code>public.guilds</code> table reference
     */
    public Guilds() {
        this(DSL.name("guilds"), null);
    }

    public <O extends Record> Guilds(Table<O> child, ForeignKey<O, GuildsRecord> key) {
        super(child, key, GUILDS);
    }

    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    @Override
    public UniqueKey<GuildsRecord> getPrimaryKey() {
        return Keys.GUILDS_PKEY;
    }

    @Override
    public List<UniqueKey<GuildsRecord>> getKeys() {
        return Arrays.<UniqueKey<GuildsRecord>>asList(Keys.GUILDS_PKEY);
    }

    @Override
    public Guilds as(String alias) {
        return new Guilds(DSL.name(alias), this);
    }

    @Override
    public Guilds as(Name alias) {
        return new Guilds(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Guilds rename(String name) {
        return new Guilds(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public Guilds rename(Name name) {
        return new Guilds(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Long, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}