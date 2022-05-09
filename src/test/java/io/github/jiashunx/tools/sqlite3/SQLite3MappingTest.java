package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Column;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Id;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Table;
import io.github.jiashunx.tools.sqlite3.service.SQLite3Service;
import io.github.jiashunx.tools.sqlite3.table.SQLPackage;
import io.github.jiashunx.tools.sqlite3.util.SQLite3SQLHelper;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * SQLite3映射测试
 * @author jiashunx
 */
public class SQLite3MappingTest {

    @Test
    public void test() throws Throwable {
        SQLPackage sqlPackage = SQLite3SQLHelper.loadSQLPackageFromClasspath("mapping-test.xml");
        assertNotNull(sqlPackage);
        SQLite3JdbcTemplate jdbcTemplate = new SQLite3JdbcTemplate("test/mapping-test.db");
        jdbcTemplate.initSQLPackage(sqlPackage);
        MappingTestService testService = new MappingTestService(jdbcTemplate, false);
        String id0 = "lllllll";
        MappingTestEntity entity0 = new MappingTestEntity();
        entity0.setFieldId(id0);
        entity0.setFieldVarchar("varchar-value");
        entity0.setFieldNVarchar("nvarchar-value");
        entity0.setFieldInteger(100001);
        entity0.setFieldText("this is a text message");
        entity0.setFieldFloat(100.323F);
        entity0.setFieldBlob("fuck".getBytes(StandardCharsets.UTF_8));
        entity0.setFieldBoolean(true);
        entity0.setFieldNumeric(new BigDecimal("1000.02"));
        entity0.setFieldDate(new Date());
        entity0.setFieldTime(new Date());
        entity0.setFieldTimestamp(new Date());
        testService.deleteById(id0);
        testService.insert(entity0);
        MappingTestEntity entity1 = testService.find(id0);
        assertNotNull(entity1);
        assertEquals(entity0.getFieldVarchar(), entity1.getFieldVarchar());
        assertEquals(entity0.getFieldNVarchar(), entity1.getFieldNVarchar());
        assertEquals(entity0.getFieldInteger(), entity1.getFieldInteger());
        assertEquals(entity0.getFieldText(), entity1.getFieldText());
        assertEquals(BigDecimal.valueOf(entity0.getFieldFloat()), BigDecimal.valueOf(entity1.getFieldFloat()));
        assertEquals("fuck", new String(entity1.getFieldBlob(), StandardCharsets.UTF_8));
        assertEquals(entity0.isFieldBoolean(), entity1.isFieldBoolean());
        assertEquals(entity0.getFieldNumeric(), entity1.getFieldNumeric());
        assertEquals(entity0.getFieldDate().getTime(), entity1.getFieldDate().getTime());
        assertEquals(entity0.getFieldTime().getTime(), entity1.getFieldTime().getTime());
        assertEquals(entity0.getFieldTimestamp().getTime(), entity1.getFieldTimestamp().getTime());
    }

    @SQLite3Table(tableName = "MAPPING_TEST_ENTITY")
    public static class MappingTestEntity implements Serializable {

        @SQLite3Id
        @SQLite3Column(columnName = "FIELD_ID")
        private String fieldId;
        @SQLite3Column(columnName = "FIELD_VARCHAR")
        private String fieldVarchar;
        @SQLite3Column(columnName = "FIELD_NVARCHAR")
        private String fieldNVarchar;
        @SQLite3Column(columnName = "FIELD_INTEGER")
        private int fieldInteger;
        @SQLite3Column(columnName = "FIELD_TEXT")
        private String fieldText;
        @SQLite3Column(columnName = "FIELD_FLOAT")
        private float fieldFloat;
        @SQLite3Column(columnName = "FIELD_BLOB")
        private byte[] fieldBlob;
        @SQLite3Column(columnName = "FIELD_BOOLEAN")
        private boolean fieldBoolean;
        @SQLite3Column(columnName = "FIELD_NUMERIC")
        private BigDecimal fieldNumeric;
        @SQLite3Column(columnName = "FIELD_DATE")
        private Date fieldDate;
        @SQLite3Column(columnName = "FIELD_TIME")
        private Date fieldTime;
        @SQLite3Column(columnName = "FIELD_TIMESTAMP")
        private Date fieldTimestamp;

        public String getFieldId() {
            return fieldId;
        }

        public void setFieldId(String fieldId) {
            this.fieldId = fieldId;
        }

        public String getFieldVarchar() {
            return fieldVarchar;
        }

        public void setFieldVarchar(String fieldVarchar) {
            this.fieldVarchar = fieldVarchar;
        }

        public String getFieldNVarchar() {
            return fieldNVarchar;
        }

        public void setFieldNVarchar(String fieldNVarchar) {
            this.fieldNVarchar = fieldNVarchar;
        }

        public int getFieldInteger() {
            return fieldInteger;
        }

        public void setFieldInteger(int fieldInteger) {
            this.fieldInteger = fieldInteger;
        }

        public String getFieldText() {
            return fieldText;
        }

        public void setFieldText(String fieldText) {
            this.fieldText = fieldText;
        }

        public float getFieldFloat() {
            return fieldFloat;
        }

        public void setFieldFloat(float fieldFloat) {
            this.fieldFloat = fieldFloat;
        }

        public byte[] getFieldBlob() {
            return fieldBlob;
        }

        public void setFieldBlob(byte[] fieldBlob) {
            this.fieldBlob = fieldBlob;
        }

        public boolean isFieldBoolean() {
            return fieldBoolean;
        }

        public void setFieldBoolean(boolean fieldBoolean) {
            this.fieldBoolean = fieldBoolean;
        }

        public BigDecimal getFieldNumeric() {
            return fieldNumeric;
        }

        public void setFieldNumeric(BigDecimal fieldNumeric) {
            this.fieldNumeric = fieldNumeric;
        }

        public Date getFieldDate() {
            return fieldDate;
        }

        public void setFieldDate(Date fieldDate) {
            this.fieldDate = fieldDate;
        }

        public Date getFieldTime() {
            return fieldTime;
        }

        public void setFieldTime(Date fieldTime) {
            this.fieldTime = fieldTime;
        }

        public Date getFieldTimestamp() {
            return fieldTimestamp;
        }

        public void setFieldTimestamp(Date fieldTimestamp) {
            this.fieldTimestamp = fieldTimestamp;
        }
    }

    public static class MappingTestService extends SQLite3Service<MappingTestEntity, String> {
        public MappingTestService(SQLite3JdbcTemplate sqLite3JdbcTemplate, boolean cacheEnabled) {
            super(sqLite3JdbcTemplate, cacheEnabled);
        }

        @Override
        protected Class<MappingTestEntity> getEntityClass() {
            return MappingTestEntity.class;
        }
    }
}
