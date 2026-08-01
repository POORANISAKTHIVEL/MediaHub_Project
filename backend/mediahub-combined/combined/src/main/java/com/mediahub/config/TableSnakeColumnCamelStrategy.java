package com.mediahub.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class TableSnakeColumnCamelStrategy
        extends PhysicalNamingStrategyStandardImpl {

    @Override
    public Identifier toPhysicalTableName(Identifier logicalName,
            JdbcEnvironment context) {
        return logicalName;
    }

    @Override
    public Identifier toPhysicalColumnName(Identifier logicalName,
            JdbcEnvironment context) {
        return logicalName;
    }
}
