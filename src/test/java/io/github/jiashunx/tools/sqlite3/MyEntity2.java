package io.github.jiashunx.tools.sqlite3;

import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Column;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Id;
import io.github.jiashunx.tools.sqlite3.mapping.SQLite3Table;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author jiashunx
 */
@SQLite3Table(tableName = "MY_TABLE2")
public class MyEntity2 {

    @SQLite3Id
    @SQLite3Column(columnName = "ID")
    private String id;

    @SQLite3Column(columnName = "VARCHAR")
    private String mVARCHAR;

    @SQLite3Column(columnName = "NVARCHAR")
    private String mNVARCHAR;

    @SQLite3Column(columnName = "INTEGER")
    private int mINTEGER;

    @SQLite3Column(columnName = "TEXT")
    private String mTEXT;

    @SQLite3Column(columnName = "FLOAT")
    private float mFLOAT;

    @SQLite3Column(columnName = "BLOB")
    private byte[] mBLOB;

    @SQLite3Column(columnName = "BOOLEAN")
    private boolean mBOOLEAN;

    @SQLite3Column(columnName = "NUMERIC")
    private BigDecimal mNUMERIC;

    @SQLite3Column(columnName = "DATE")
    private Date mDATE;

    @SQLite3Column(columnName = "TIME")
    private Date mTIME;

    @SQLite3Column(columnName = "TIMESTAMP")
    private Date mTIMESTAMP;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getmVARCHAR() {
        return mVARCHAR;
    }

    public void setmVARCHAR(String mVARCHAR) {
        this.mVARCHAR = mVARCHAR;
    }

    public String getmNVARCHAR() {
        return mNVARCHAR;
    }

    public void setmNVARCHAR(String mNVARCHAR) {
        this.mNVARCHAR = mNVARCHAR;
    }

    public int getmINTEGER() {
        return mINTEGER;
    }

    public void setmINTEGER(int mINTEGER) {
        this.mINTEGER = mINTEGER;
    }

    public String getmTEXT() {
        return mTEXT;
    }

    public void setmTEXT(String mTEXT) {
        this.mTEXT = mTEXT;
    }

    public float getmFLOAT() {
        return mFLOAT;
    }

    public void setmFLOAT(float mFLOAT) {
        this.mFLOAT = mFLOAT;
    }

    public byte[] getmBLOB() {
        return mBLOB;
    }

    public void setmBLOB(byte[] mBLOB) {
        this.mBLOB = mBLOB;
    }

    public boolean ismBOOLEAN() {
        return mBOOLEAN;
    }

    public void setmBOOLEAN(boolean mBOOLEAN) {
        this.mBOOLEAN = mBOOLEAN;
    }

    public BigDecimal getmNUMERIC() {
        return mNUMERIC;
    }

    public void setmNUMERIC(BigDecimal mNUMERIC) {
        this.mNUMERIC = mNUMERIC;
    }

    public Date getmDATE() {
        return mDATE;
    }

    public void setmDATE(Date mDATE) {
        this.mDATE = mDATE;
    }

    public Date getmTIME() {
        return mTIME;
    }

    public void setmTIME(Date mTIME) {
        this.mTIME = mTIME;
    }

    public Date getmTIMESTAMP() {
        return mTIMESTAMP;
    }

    public void setmTIMESTAMP(Date mTIMESTAMP) {
        this.mTIMESTAMP = mTIMESTAMP;
    }
}
