package io.github.jiashunx.tools.sqlite3.util;

import io.github.jiashunx.tools.sqlite3.exception.SQLite3SQLException;
import io.github.jiashunx.tools.sqlite3.table.*;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jiashunx
 */
public class SQLite3SQLHelper {

    private static final Logger logger = LoggerFactory.getLogger(SQLite3SQLHelper.class);

    private SQLite3SQLHelper() {}

    public static SQLPackage loadSQLPackageFromClasspath(String filePath) throws SQLite3SQLException {
        return loadSQLPackageFromClasspath(filePath, SQLite3SQLHelper.class.getClassLoader());
    }

    public static SQLPackage loadSQLPackageFromClasspath(String filePath, ClassLoader classLoader) throws SQLite3SQLException {
        try {
            return loadSQLPackage(classLoader.getResourceAsStream(filePath));
        } catch (Throwable throwable) {
            throw new SQLite3SQLException(String.format("load SQLPackage from classpath: %s failed, classLoader: %s", filePath, String.valueOf(classLoader)));
        }
    }

    public static SQLPackage loadSQLPackageFromDisk(String filePath) throws SQLite3SQLException {
        try {
            return loadSQLPackage(new FileInputStream(filePath));
        } catch (Throwable throwable) {
            throw new SQLite3SQLException(String.format("load SQLPackage from disk: %s failed", filePath));
        }
    }

    public static SQLPackage loadSQLPackage(InputStream inputStream) throws SQLite3SQLException {
        SQLPackage sqlPackage = null;
        try {
            Element rootElement = new SAXReader().read(inputStream).getRootElement();
            sqlPackage = new SQLPackage();
            sqlPackage.setGroupId(rootElement.attributeValue("id"));
            sqlPackage.setGroupName(rootElement.attributeValue("name"));
            AtomicReference<SQLPackage> sqlPackageRef = new AtomicReference<>(sqlPackage);
            Optional.ofNullable(rootElement.elements("dql")).ifPresent(dqlElements -> {
                dqlElements.forEach(dqlElement -> {
                    Optional.ofNullable(dqlElement.elements("sql")).ifPresent(sqlElements -> {
                        sqlElements.forEach(sqlElement -> {
                            SQL sql = new SQL();
                            sql.setId(sqlElement.attributeValue("id"));
                            sql.setDesc(sqlElement.attributeValue("desc"));
                            sql.setClassName(sqlElement.attributeValue("class"));
                            sql.setContent(sqlElement.getText().replace("    ", "").replace("\n", " "));
                            sqlPackageRef.get().addDQL(sql);
                        });
                    });
                });
            });
            Optional.ofNullable(rootElement.elements("dml")).ifPresent(dmlElements -> {
                dmlElements.forEach(dmlElement -> {
                    Optional.ofNullable(dmlElement.elements("sql")).ifPresent(sqlElements -> {
                        sqlElements.forEach(sqlElement -> {
                            SQL sql = new SQL();
                            sql.setId(sqlElement.attributeValue("id"));
                            sql.setDesc(sqlElement.attributeValue("desc"));
                            sql.setContent(sqlElement.getText().replace("    ", "").replace("\n", " "));
                            sqlPackageRef.get().addDML(sql);
                        });
                    });
                });
            });
            Optional.ofNullable(rootElement.elements("ddl")).ifPresent(ddlElements -> {
                ddlElements.forEach(ddlElement -> {
                    Optional.ofNullable(ddlElement.elements("table")).ifPresent(tableElements -> {
                        tableElements.forEach(tableElement -> {
                            String tableName = tableElement.attributeValue("name");
                            String tableDesc = tableElement.attributeValue("desc");
                            Optional.ofNullable(tableElement.elements("column")).ifPresent(columnElements -> {
                                columnElements.forEach(columnElement -> {
                                    Column column =  new Column();
                                    column.setColumnName(columnElement.attributeValue("name"));
                                    column.setColumnType(columnElement.attributeValue("type"));
                                    column.setPrimary("true".equals(columnElement.attributeValue("primary")));
                                    column.setColumnComment(columnElement.attributeValue("comment"));
                                    column.setTableName(tableName);
                                    column.setTableDesc(tableDesc);
                                    sqlPackageRef.get().addColumnDDL(column);
                                });
                            });
                        });
                    });
                    Optional.ofNullable(ddlElement.elements("view")).ifPresent(viewElements -> {
                        viewElements.forEach(viewElement -> {
                            View view = new View();
                            view.setViewName(viewElement.attributeValue("name"));
                            view.setViewDesc(viewElement.attributeValue("desc"));
                            view.setTemporary("true".equals(viewElement.attributeValue("temporary")));
                            view.setContent(viewElement.getText().replace("    ", "").replace("\n", " "));
                            sqlPackageRef.get().addViewDDL(view);
                        });
                    });
                    Optional.ofNullable(ddlElement.elements("index")).ifPresent(indexElements -> {
                        indexElements.forEach(indexElement -> {
                            Index index = new Index();
                            index.setIndexName(indexElement.attributeValue("name"));
                            index.setTableName(indexElement.attributeValue("table"));
                            index.setUnique("true".equals(indexElement.attributeValue("unique")));
                            AtomicReference<List<String>> columnNamesRef = new AtomicReference<List<String>>(new ArrayList<>());
                            Optional.ofNullable(indexElement.elements("column")).ifPresent(columnElements -> {
                                columnElements.forEach(columnElement -> {
                                    String columnName = columnElement.attributeValue("name");
                                    columnNamesRef.get().add(columnName);
                                });
                            });
                            index.setColumnNames(columnNamesRef.get());
                            sqlPackageRef.get().addIndexDDL(index);
                        });
                    });
                });
            });
        } catch (Throwable throwable) {
            throw new SQLite3SQLException("load SQLPackage from InputStream failed.", throwable);
        }
        return sqlPackage;
    }

}
