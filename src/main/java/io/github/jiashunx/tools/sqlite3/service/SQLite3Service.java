package io.github.jiashunx.tools.sqlite3.service;

import io.github.jiashunx.tools.sqlite3.SQLite3JdbcTemplate;
import io.github.jiashunx.tools.sqlite3.connection.SQLite3PreparedStatement;
import io.github.jiashunx.tools.sqlite3.exception.SQLite3Exception;
import io.github.jiashunx.tools.sqlite3.exception.SQLite3MappingException;
import io.github.jiashunx.tools.sqlite3.function.VoidFunc;
import io.github.jiashunx.tools.sqlite3.model.TableModel;
import io.github.jiashunx.tools.sqlite3.util.SQLite3Utils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jiashunx
 */
public abstract class SQLite3Service<Entity, ID> {

    private final SQLite3JdbcTemplate jdbcTemplate;

    private final Entity defaultEntity;

    private volatile boolean listAllMethodInvoked = false;
    // 全局缓存
    private final Map<ID, Entity> entityMap = new LinkedHashMap<>();
    // 临时缓存
    private final Map<ID, Entity> entityMap0 = new HashMap<>();
    private final ReentrantReadWriteLock entityMapLock = new ReentrantReadWriteLock();

    public SQLite3Service(SQLite3JdbcTemplate jdbcTemplate) throws NullPointerException, SQLite3Exception {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
        try {
            this.defaultEntity = getEntityClass().newInstance();
        } catch (Throwable throwable) {
            throw new SQLite3Exception(String.format("create entity [%s] instance failed", getEntityClass()), throwable);
        }
    }

    public SQLite3JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    private Map<ID, Entity> getEntityMap() {
        if (listAllMethodInvoked) {
            return entityMap;
        }
        return entityMap0;
    }

    protected abstract Class<Entity> getEntityClass();

    protected void entityCacheReadLock(VoidFunc voidFunc) {
        entityMapLock.readLock().lock();
        try {
            voidFunc.apply();
        } finally {
            entityMapLock.readLock().unlock();
        }
    }

    protected void entityCacheWriteLock(VoidFunc voidFunc) {
        entityMapLock.writeLock().lock();
        try {
            voidFunc.apply();
        } finally {
            entityMapLock.writeLock().unlock();
        }
    }

    public ID getIdFieldValue(Entity entity) {
        return (ID) SQLite3Utils.getClassTableModel(getEntityClass()).getIdFieldValue(entity);
    }

    protected String getListAllSQL() {
        return SQLite3Utils.getClassTableModel(getEntityClass()).getSelectAllSQL();
    }

    public List<Entity> listAll() throws NullPointerException, SQLite3MappingException {
        AtomicReference<List<Entity>> ref = new AtomicReference<>();
        if (!listAllMethodInvoked) {
            entityCacheWriteLock(() -> {
                if (listAllMethodInvoked) {
                    return;
                }
                entityMap0.clear();
                List<Entity> entityList = getJdbcTemplate().queryForList(getListAllSQL(), getEntityClass());
                for (Entity entity: entityList) {
                    entityMap.put(getIdFieldValue(entity), entity);
                }
                listAllMethodInvoked = true;
            });
        }
        entityCacheReadLock(() -> {
            ref.set(new ArrayList<>(entityMap.values()));
        });
        return ref.get();
    }

    protected String getFindOneSQL() {
        return SQLite3Utils.getClassTableModel(getEntityClass()).getSelectSQL();
    }

    public Entity find(ID id) throws NullPointerException, SQLite3MappingException {
        if (id == null) {
            throw new NullPointerException();
        }
        AtomicReference<Entity> ref = new AtomicReference<>();
        entityCacheReadLock(() -> {
            ref.set(getEntityMap().get(id));
        });
        if (ref.get() == null) {
            entityCacheWriteLock(() -> {
                ref.set(getEntityMap().get(id));
                if (ref.get() == null) {
                    Entity tmpEntity = getJdbcTemplate().queryForObj(getFindOneSQL(), statement -> {
                        castIDForStatement(statement, 1, id);
                    }, getEntityClass());
                    if (tmpEntity == null) {
                        tmpEntity = defaultEntity;
                    }
                    getEntityMap().put(id, tmpEntity);
                    ref.set(tmpEntity);
                }
            });
        }
        Entity entity = ref.get();
        if (entity == defaultEntity) {
            entity = null;
        }
        return entity;
    }

    public Entity insert(Entity entity) throws NullPointerException, SQLite3MappingException {
        if (entity == null) {
            throw new NullPointerException();
        }
        entityCacheWriteLock(() -> {
            jdbcTemplate.insert(entity);
            getEntityMap().put(getIdFieldValue(entity), entity);
        });
        return entity;
    }

    public List<Entity> insert(List<Entity> entities) throws NullPointerException, SQLite3MappingException {
        if (entities == null) {
            throw new NullPointerException();
        }
        entities.forEach(entity -> {
            if (entity == null) {
                throw new NullPointerException();
            }
        });
        entityCacheWriteLock(() -> {
            Map<ID, Entity> map = new HashMap<>();
            entities.forEach(entity -> {
                map.put(getIdFieldValue(entity), entity);
            });
            jdbcTemplate.insert(entities);
            getEntityMap().putAll(map);
        });
        return entities;
    }

    public Entity update(Entity entity) throws NullPointerException, SQLite3MappingException {
        if (entity == null) {
            throw new NullPointerException();
        }
        entityCacheWriteLock(() -> {
            ID id = getIdFieldValue(entity);
            jdbcTemplate.update(entity);
            getEntityMap().put(id, entity);
        });
        return entity;
    }

    public List<Entity> update(List<Entity> entities) throws NullPointerException, SQLite3MappingException {
        if (entities == null) {
            throw new NullPointerException();
        }
        entities.forEach(entity -> {
            if (entity == null) {
                throw new NullPointerException();
            }
        });
        entityCacheWriteLock(() -> {
            Map<ID, Entity> map = new HashMap<>();
            entities.forEach(entity -> {
                map.put(getIdFieldValue(entity), entity);
            });
            jdbcTemplate.update(entities);
            getEntityMap().putAll(map);
        });
        return entities;
    }

    public int delete(Entity entity) throws NullPointerException, SQLite3MappingException {
        return delete(Collections.singletonList(entity));
    }

    public int delete(List<Entity> entities) throws NullPointerException, SQLite3MappingException {
        if (entities == null) {
            throw new NullPointerException();
        }
        List<ID> idList = new ArrayList<>(entities.size());
        entities.forEach(entity -> {
            if (entity == null) {
                throw new NullPointerException();
            }
            idList.add(getIdFieldValue(entity));
        });
        return deleteById(idList);
    }

    public int deleteById(ID id) throws NullPointerException, SQLite3MappingException {
        return deleteById(Collections.singletonList(id));
    }

    public int deleteById(List<ID> idList) throws NullPointerException, SQLite3MappingException {
        if (idList == null) {
            throw new NullPointerException();
        }
        idList.forEach(entity -> {
            if (entity == null) {
                throw new NullPointerException();
            }
        });
        AtomicReference<Integer> ref = new AtomicReference<>();
        entityCacheWriteLock(() -> {
            TableModel tableModel = SQLite3Utils.getClassTableModel(getEntityClass());
            ref.set(jdbcTemplate.batchUpdate(tableModel.getDeleteSQL(), idList.size(), (index, statement) -> {
                castIDForStatement(statement, 1, idList.get(index));
            }));
            idList.forEach(getEntityMap()::remove);
        });
        return ref.get();
    }

    protected void castIDForStatement(SQLite3PreparedStatement statement, int parameterIndex, ID id) {
        Class<?> klass = id.getClass();
        if (klass == String.class) {
            statement.setString(parameterIndex, (String) id);
        } else if (klass == int.class || klass == Integer.class) {
            statement.setInt(parameterIndex, (Integer) id);
        } else if (klass == long.class || klass == Long.class) {
            statement.setLong(parameterIndex, (Long) id);
        } else {
            throw new UnsupportedOperationException();
        }
    }

}
