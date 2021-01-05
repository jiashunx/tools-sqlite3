package io.github.jiashunx.tools.sqlite3.util;

import io.github.jiashunx.tools.sqlite3.exception.SQLite3SQLException;
import io.github.jiashunx.tools.sqlite3.table.Column;
import io.github.jiashunx.tools.sqlite3.table.SQL;
import io.github.jiashunx.tools.sqlite3.table.SQLPackage;
import io.github.jiashunx.tools.sqlite3.table.View;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
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
            String groupId = rootElement.attributeValue("id");
            sqlPackage = new SQLPackage(groupId);
            AtomicReference<SQLPackage> sqlPackageRef = new AtomicReference<>(sqlPackage);
            Optional.ofNullable(rootElement.elements("dql")).ifPresent(dqlElements -> {
                dqlElements.forEach(dqlElement -> {
                    Optional.ofNullable(dqlElement.elements("sql")).ifPresent(sqlElements -> {
                        sqlElements.forEach(sqlElement -> {
                            String id = sqlElement.attributeValue("id");
                            String desc = sqlElement.attributeValue("attr");
                            String className = sqlElement.attributeValue("class");
                            String content = sqlElement.getText().replace("    ", "").replace("\n", " ");
                            sqlPackageRef.get().addDQL(new SQL(id, content, desc, className));
                        });
                    });
                });
            });
            Optional.ofNullable(rootElement.elements("dml")).ifPresent(dmlElements -> {
                dmlElements.forEach(dmlElement -> {
                    Optional.ofNullable(dmlElement.elements("sql")).ifPresent(sqlElements -> {
                        sqlElements.forEach(sqlElement -> {
                            String id = sqlElement.attributeValue("id");
                            String desc = sqlElement.attributeValue("attr");
                            String content = sqlElement.getText().replace("    ", "").replace("\n", " ");
                            sqlPackageRef.get().addDML(new SQL(id, content, desc));
                        });
                    });
                });
            });
            Optional.ofNullable(rootElement.elements("ddl")).ifPresent(ddlElements -> {
                ddlElements.forEach(ddlElement -> {
                    Optional.ofNullable(ddlElement.elements("table")).ifPresent(tableElements -> {
                        tableElements.forEach(tableElement -> {
                            String tableName = tableElement.attributeValue("name");
                            Optional.ofNullable(tableElement.elements("column")).ifPresent(columnElements -> {
                                columnElements.forEach(columnElement -> {
                                    String columnName = columnElement.attributeValue("name");
                                    String columnType = columnElement.attributeValue("type");
                                    String primaryVal = columnElement.attributeValue("primary");
                                    boolean primary = "true".equals(primaryVal);
                                    sqlPackageRef.get().addColumnDDL(new Column(tableName, columnName, columnType, primary));
                                });
                            });
                        });
                    });
                    Optional.ofNullable(ddlElement.elements("view")).ifPresent(viewElements -> {
                        viewElements.forEach(viewElement -> {
                            String viewName = viewElement.attributeValue("name");
                            String temporaryVal = viewElement.attributeValue("temporary");
                            boolean temporary = "true".equals(temporaryVal);
                            String content = viewElement.getText().replace("    ", "").replace("\n", " ");
                            sqlPackageRef.get().addViewDDL(new View(viewName, temporary, content));
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
