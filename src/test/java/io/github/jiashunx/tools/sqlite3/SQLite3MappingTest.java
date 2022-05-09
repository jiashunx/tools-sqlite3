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
    public void testTrigger() throws Throwable {
        SQLPackage sqlPackage = SQLite3SQLHelper.loadSQLPackageFromClasspath("mapping-test.xml");
        SQLite3JdbcTemplate jdbcTemplate = new SQLite3JdbcTemplate("test/mapping-test.db");
        jdbcTemplate.initSQLPackage(sqlPackage);
        // sqlite没有truncate命令, 使用delete删除表记录
        jdbcTemplate.executeUpdate("DELETE FROM AUDIT");
        jdbcTemplate.executeUpdate("DELETE FROM COMPANY");
        jdbcTemplate.executeUpdate("INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
                "VALUES (1, 'Paul', 32, 'California', 20000.00 )");
        assertEquals(1, jdbcTemplate.queryForInt("SELECT COUNT(1) FROM AUDIT"));
    }

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
        entity0.setFieldMediumInt(10002);
        entity0.setFieldInt(10003);
        entity0.setFieldInt4(10004);
        entity0.setFieldText("this is a text message");
        entity0.setFieldFloat(100.323F);
        entity0.setFieldBlob("fuck".getBytes(StandardCharsets.UTF_8));
        entity0.setFieldBoolean(true);
        entity0.setFieldBit(false);
        entity0.setFieldNumeric(new BigDecimal("1000.02"));
        entity0.setFieldDecimal(new BigDecimal("2001.03"));
        entity0.setFieldDate(new Date());
        entity0.setFieldTime(new Date());
        entity0.setFieldTimestamp(new Date());
        entity0.setFieldInt1((byte) 7);
        entity0.setFieldTinyint((byte) 6);
        entity0.setFieldInt2((short) 127);
        entity0.setFieldSmallint((short) 126);
        entity0.setFieldInt8(888888888888L);
        entity0.setFieldBigint(99999999999999999L);
        entity0.setFieldReal(9.0988D);
        entity0.setFieldDouble(8.0991D);
        entity0.setFieldChar("A");
        entity0.setFieldLongvarchar("B");
        entity0.setFieldClob("C");
        entity0.setFieldTinytext("D");
        entity0.setFieldMediumtext("E");
        entity0.setFieldLongtext("F");
        entity0.setFieldNchar("G");
        entity0.setFieldLongnvarchar("I");
        entity0.setFieldNclob("J");
        entity0.setFieldBinary("fuckX".getBytes(StandardCharsets.UTF_8));
        entity0.setFieldVarbinary("fuckY".getBytes(StandardCharsets.UTF_8));
        entity0.setFieldLongvarbinary("fuckZ".getBytes(StandardCharsets.UTF_8));
        testService.deleteById(id0);
        testService.insert(entity0);
        MappingTestEntity entity1 = testService.find(id0);
        assertNotNull(entity1);
        assertEquals(entity0.getFieldVarchar(), entity1.getFieldVarchar());
        assertEquals(entity0.getFieldNVarchar(), entity1.getFieldNVarchar());
        assertEquals(entity0.getFieldInteger(), entity1.getFieldInteger());
        assertEquals(entity0.getFieldMediumInt(), entity1.getFieldMediumInt());
        assertEquals(entity0.getFieldInt(), entity1.getFieldInt());
        assertEquals(entity0.getFieldInt4(), entity1.getFieldInt4());
        assertEquals(entity0.getFieldText(), entity1.getFieldText());
        assertEquals(BigDecimal.valueOf(entity0.getFieldFloat()), BigDecimal.valueOf(entity1.getFieldFloat()));
        assertEquals("fuck", new String(entity1.getFieldBlob(), StandardCharsets.UTF_8));
        assertEquals(entity0.isFieldBoolean(), entity1.isFieldBoolean());
        assertEquals(entity0.isFieldBit(), entity1.isFieldBit());
        assertEquals(entity0.getFieldNumeric(), entity1.getFieldNumeric());
        assertEquals(entity0.getFieldDecimal(), entity1.getFieldDecimal());
        assertEquals(entity0.getFieldDate().getTime(), entity1.getFieldDate().getTime());
        assertEquals(entity0.getFieldTime().getTime(), entity1.getFieldTime().getTime());
        assertEquals(entity0.getFieldTimestamp().getTime(), entity1.getFieldTimestamp().getTime());
        assertEquals(entity0.getFieldInt1(), entity1.getFieldInt1());
        assertEquals(entity0.getFieldTinyint(), entity1.getFieldTinyint());
        assertEquals(entity0.getFieldInt2(), entity1.getFieldInt2());
        assertEquals(entity0.getFieldSmallint(), entity1.getFieldSmallint());
        assertEquals(entity0.getFieldInt8(), entity1.getFieldInt8());
        assertEquals(entity0.getFieldBigint(), entity1.getFieldBigint());
        assertEquals(BigDecimal.valueOf(entity0.getFieldReal()), BigDecimal.valueOf(entity1.getFieldReal()));
        assertEquals(BigDecimal.valueOf(entity0.getFieldDouble()), BigDecimal.valueOf(entity1.getFieldDouble()));
        assertEquals(entity0.getFieldChar(), entity1.getFieldChar());
        assertEquals(entity0.getFieldLongvarchar(), entity1.getFieldLongvarchar());
        assertEquals(entity0.getFieldClob(), entity1.getFieldClob());
        assertEquals(entity0.getFieldTinytext(), entity1.getFieldTinytext());
        assertEquals(entity0.getFieldMediumtext(), entity1.getFieldMediumtext());
        assertEquals(entity0.getFieldLongtext(), entity1.getFieldLongtext());
        assertEquals(entity0.getFieldNchar(), entity1.getFieldNchar());
        assertEquals(entity0.getFieldLongnvarchar(), entity1.getFieldLongnvarchar());
        assertEquals(entity0.getFieldNclob(), entity1.getFieldNclob());
        assertEquals("fuckX", new String(entity1.getFieldBinary(), StandardCharsets.UTF_8));
        assertEquals("fuckY", new String(entity1.getFieldVarbinary(), StandardCharsets.UTF_8));
        assertEquals("fuckZ", new String(entity1.getFieldLongvarbinary(), StandardCharsets.UTF_8));
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
        @SQLite3Column(columnName = "FIELD_MEDIUMINT")
        private int fieldMediumInt;
        @SQLite3Column(columnName = "FIELD_INT")
        private int fieldInt;
        @SQLite3Column(columnName = "FIELD_INT4")
        private int fieldInt4;
        @SQLite3Column(columnName = "FIELD_TEXT")
        private String fieldText;
        @SQLite3Column(columnName = "FIELD_FLOAT")
        private float fieldFloat;
        @SQLite3Column(columnName = "FIELD_BLOB")
        private byte[] fieldBlob;
        @SQLite3Column(columnName = "FIELD_BOOLEAN")
        private boolean fieldBoolean;
        @SQLite3Column(columnName = "FIELD_BIT")
        private boolean fieldBit;
        @SQLite3Column(columnName = "FIELD_NUMERIC")
        private BigDecimal fieldNumeric;
        @SQLite3Column(columnName = "FIELD_DECIMAL")
        private BigDecimal fieldDecimal;
        @SQLite3Column(columnName = "FIELD_DATE")
        private Date fieldDate;
        @SQLite3Column(columnName = "FIELD_TIME")
        private Date fieldTime;
        @SQLite3Column(columnName = "FIELD_TIMESTAMP")
        private Date fieldTimestamp;
        @SQLite3Column(columnName = "FIELD_INT1")
        private byte fieldInt1;
        @SQLite3Column(columnName = "FIELD_TINYINT")
        private byte fieldTinyint;
        @SQLite3Column(columnName = "FIELD_INT2")
        private short fieldInt2;
        @SQLite3Column(columnName = "FIELD_SMALLINT")
        private short fieldSmallint;
        @SQLite3Column(columnName = "FIELD_INT8")
        private long fieldInt8;
        @SQLite3Column(columnName = "FIELD_BIGINT")
        private long fieldBigint;
        @SQLite3Column(columnName = "FIELD_REAL")
        private double fieldReal;
        @SQLite3Column(columnName = "FIELD_DOUBLE")
        private double fieldDouble;
        @SQLite3Column(columnName = "FIELD_CHAR")
        private String fieldChar;
        @SQLite3Column(columnName = "FIELD_LONGVARCHAR")
        private String fieldLongvarchar;
        @SQLite3Column(columnName = "FIELD_CLOB")
        private String fieldClob;
        @SQLite3Column(columnName = "FIELD_TINYTEXT")
        private String fieldTinytext;
        @SQLite3Column(columnName = "FIELD_MEDIUMTEXT")
        private String fieldMediumtext;
        @SQLite3Column(columnName = "FIELD_LONGTEXT")
        private String fieldLongtext;
        @SQLite3Column(columnName = "FIELD_NCHAR")
        private String fieldNchar;
        @SQLite3Column(columnName = "FIELD_LONGNVARCHAR")
        private String fieldLongnvarchar;
        @SQLite3Column(columnName = "FIELD_NCLOB")
        private String fieldNclob;
        @SQLite3Column(columnName = "FIELD_BINARY")
        private byte[] fieldBinary;
        @SQLite3Column(columnName = "FIELD_VARBINARY")
        private byte[] fieldVarbinary;
        @SQLite3Column(columnName = "FIELD_LONGVARBINARY")
        private byte[] fieldLongvarbinary;

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

        public int getFieldMediumInt() {
            return fieldMediumInt;
        }

        public void setFieldMediumInt(int fieldMediumInt) {
            this.fieldMediumInt = fieldMediumInt;
        }

        public int getFieldInt() {
            return fieldInt;
        }

        public void setFieldInt(int fieldInt) {
            this.fieldInt = fieldInt;
        }

        public int getFieldInt4() {
            return fieldInt4;
        }

        public void setFieldInt4(int fieldInt4) {
            this.fieldInt4 = fieldInt4;
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

        public boolean isFieldBit() {
            return fieldBit;
        }

        public void setFieldBit(boolean fieldBit) {
            this.fieldBit = fieldBit;
        }

        public BigDecimal getFieldNumeric() {
            return fieldNumeric;
        }

        public void setFieldNumeric(BigDecimal fieldNumeric) {
            this.fieldNumeric = fieldNumeric;
        }

        public BigDecimal getFieldDecimal() {
            return fieldDecimal;
        }

        public void setFieldDecimal(BigDecimal fieldDecimal) {
            this.fieldDecimal = fieldDecimal;
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

        public byte getFieldInt1() {
            return fieldInt1;
        }

        public void setFieldInt1(byte fieldInt1) {
            this.fieldInt1 = fieldInt1;
        }

        public byte getFieldTinyint() {
            return fieldTinyint;
        }

        public void setFieldTinyint(byte fieldTinyint) {
            this.fieldTinyint = fieldTinyint;
        }

        public short getFieldInt2() {
            return fieldInt2;
        }

        public void setFieldInt2(short fieldInt2) {
            this.fieldInt2 = fieldInt2;
        }

        public short getFieldSmallint() {
            return fieldSmallint;
        }

        public void setFieldSmallint(short fieldSmallint) {
            this.fieldSmallint = fieldSmallint;
        }

        public long getFieldInt8() {
            return fieldInt8;
        }

        public void setFieldInt8(long fieldInt8) {
            this.fieldInt8 = fieldInt8;
        }

        public long getFieldBigint() {
            return fieldBigint;
        }

        public void setFieldBigint(long fieldBigint) {
            this.fieldBigint = fieldBigint;
        }

        public double getFieldReal() {
            return fieldReal;
        }

        public void setFieldReal(double fieldReal) {
            this.fieldReal = fieldReal;
        }

        public double getFieldDouble() {
            return fieldDouble;
        }

        public void setFieldDouble(double fieldDouble) {
            this.fieldDouble = fieldDouble;
        }

        public String getFieldChar() {
            return fieldChar;
        }

        public void setFieldChar(String fieldChar) {
            this.fieldChar = fieldChar;
        }

        public String getFieldLongvarchar() {
            return fieldLongvarchar;
        }

        public void setFieldLongvarchar(String fieldLongvarchar) {
            this.fieldLongvarchar = fieldLongvarchar;
        }

        public String getFieldClob() {
            return fieldClob;
        }

        public void setFieldClob(String fieldClob) {
            this.fieldClob = fieldClob;
        }

        public String getFieldTinytext() {
            return fieldTinytext;
        }

        public void setFieldTinytext(String fieldTinytext) {
            this.fieldTinytext = fieldTinytext;
        }

        public String getFieldMediumtext() {
            return fieldMediumtext;
        }

        public void setFieldMediumtext(String fieldMediumtext) {
            this.fieldMediumtext = fieldMediumtext;
        }

        public String getFieldLongtext() {
            return fieldLongtext;
        }

        public void setFieldLongtext(String fieldLongtext) {
            this.fieldLongtext = fieldLongtext;
        }

        public String getFieldNchar() {
            return fieldNchar;
        }

        public void setFieldNchar(String fieldNchar) {
            this.fieldNchar = fieldNchar;
        }

        public String getFieldLongnvarchar() {
            return fieldLongnvarchar;
        }

        public void setFieldLongnvarchar(String fieldLongnvarchar) {
            this.fieldLongnvarchar = fieldLongnvarchar;
        }

        public String getFieldNclob() {
            return fieldNclob;
        }

        public void setFieldNclob(String fieldNclob) {
            this.fieldNclob = fieldNclob;
        }

        public byte[] getFieldBinary() {
            return fieldBinary;
        }

        public void setFieldBinary(byte[] fieldBinary) {
            this.fieldBinary = fieldBinary;
        }

        public byte[] getFieldVarbinary() {
            return fieldVarbinary;
        }

        public void setFieldVarbinary(byte[] fieldVarbinary) {
            this.fieldVarbinary = fieldVarbinary;
        }

        public byte[] getFieldLongvarbinary() {
            return fieldLongvarbinary;
        }

        public void setFieldLongvarbinary(byte[] fieldLongvarbinary) {
            this.fieldLongvarbinary = fieldLongvarbinary;
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
