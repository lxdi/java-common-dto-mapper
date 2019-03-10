package com.sogoodlabs.sql_mapper;

import com.sogoodlabs.TestEntity;
import com.sogoodlabs.common_mapper.SQLmapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;


public class SQLmapperTest {


    SQLmapper sqLmapper;

    @Before
    public void init(){
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setName("test_embedded_db")
                .build();
        initTable(dataSource);
        sqLmapper = new SQLmapper(dataSource);
    }

    @Test
    public void mapToDtoTest(){
        Map<String, Object> result = new HashMap<>();
        sqLmapper.mapToDto(1, TestEntity.class, result);
        assertTrue(result.size()>0);
    }

    private void initTable(DataSource dataSource){
        try {
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            statement.execute("create table testentity (id bigint, title character varying(255), booleanval boolean)");
            statement.execute("insert into testentity values(1, 'test title', true)");

            statement.execute("select * from testentity");
            ResultSet rs = statement.getResultSet();
            while((rs!=null) && (rs.next())){
                System.out.println(rs.getLong(1) + " : " + rs.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
