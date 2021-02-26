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

    private final boolean cacheEnabled;

    private volatile boolean listAllMethodInvoked = false;
    // 全局缓存
    private final Map<ID, Entity> entityCacheMap = new LinkedHashMap<>();
    // 局部缓存
    private final Map<ID, Entity> entityCacheMapTmp = new HashMap<>();
    private final ReentrantReadWriteLock entityCacheMapLock = new ReentrantReadWriteLock();

    public SQLite3Service(SQLite3JdbcTemplate jdbcTemplate) throws NullPointerException, SQLite3Exception {
        // 默认开启缓存
        this(jdbcTemplate, true);
    }

    public SQLite3Service(SQLite3JdbcTemplate jdbcTemplate, boolean cacheEnabled) throws NullPointerException, SQLite3Exception {
        this.cacheEnabled = cacheEnabled;
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

    private Map<ID, Entity> getEntityCacheMap() {
        if (listAllMethodInvoked) {
            return entityCacheMap;
        }
        return entityCacheMapTmp;
    }

    protected abstract Class<Entity> getEntityClass();

    protected void entityCacheReadLock(VoidFunc voidFunc) {
        entityCacheMapLock.readLock().lock();
        try {
            voidFunc.apply();
        } finally {
            entityCacheMapLock.readLock().unlock();
        }
    }

    protected void entityCacheWriteLock(VoidFunc voidFunc) {
        entityCacheMapLock.writeLock().lock();
        try {
            voidFunc.apply();
        } finally {
            entityCacheMapLock.writeLock().unlock();
        }
    }

    public ID getIdFieldValue(Entity entity) {
        if (entity == null) {
            throw new NullPointerException();
        }
        return (ID) SQLite3Utils.getClassTableModel(getEntityClass()).getIdFieldValue(entity);
    }

    protected String getListAllSQL() {
        return SQLite3Utils.getClassTableModel(getEntityClass()).getSelectAllSQL();
    }

    public List<Entity> listAllWithNoCache() throws NullPointerException, SQLite3MappingException {
        return getJdbcTemplate().queryForList(getListAllSQL(), getEntityClass());
    }

    public List<Entity> listAll() throws NullPointerException, SQLite3MappingException {
        if (!cacheEnabled) {
            return listAllWithNoCache();
        }
        AtomicReference<List<Entity>> ref = new AtomicReference<>();
        if (!listAllMethodInvoked) {
            entityCacheWriteLock(() -> {
                if (listAllMethodInvoked) {
                    return;
                }
                entityCacheMapTmp.clear();
                List<Entity> entityList = listAllWithNoCache();
                for (Entity entity: entityList) {
                    entityCacheMap.put(getIdFieldValue(entity), entity);
                }
                listAllMethodInvoked = true;
            });
        }
        entityCacheReadLock(() -> {
            ref.set(new ArrayList<>(entityCacheMap.values()));
        });
        return ref.get();
    }

    protected String getFindOneSQL() {
        return SQLite3Utils.getClassTableModel(getEntityClass()).getSelectSQL();
    }

    public Entity findWithNoCache(ID id) throws NullPointerException, SQLite3MappingException {
        if (id == null) {
            throw new NullPointerException();
        }
        return getJdbcTemplate().queryForObj(getFindOneSQL(), statement -> {
            castIDForStatement(statement, 1, id);
        }, getEntityClass());
    }

    public Entity find(ID id) throws NullPointerException, SQLite3MappingException {
        if (id == null) {
            throw new NullPointerException();
        }
        if (!cacheEnabled) {
            return findWithNoCache(id);
        }
        AtomicReference<Entity> ref = new AtomicReference<>();
        entityCacheReadLock(() -> {
            ref.set(getEntityCacheMap().get(id));
        });
        if (ref.get() == null) {
            entityCacheWriteLock(() -> {
                ref.set(getEntityCacheMap().get(id));
                if (ref.get() == null) {
                    Entity tmpEntity = findWithNoCache(id);
                    if (tmpEntity == null) {
                        tmpEntity = defaultEntity;
                    }
                    getEntityCacheMap().put(id, tmpEntity);
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

    public Entity insertWithNoCache(Entity entity) throws NullPointerException, SQLite3MappingException {
        if (entity == null) {
            throw new NullPointerException();
        }
        getJdbcTemplate().insert(entity);
        return entity;
    }

    public Entity insert(Entity entity) throws NullPointerException, SQLite3MappingException {
        if (!cacheEnabled) {
            return insertWithNoCache(entity);
        }
        entityCacheWriteLock(() -> {
            insertWithNoCache(entity);
            getEntityCacheMap().put(getIdFieldValue(entity), entity);
        });
        return entity;
    }

    public List<Entity> insertWithNoCache(List<Entity> entities) throws NullPointerException, SQLite3MappingException {
        if (entities == null) {
            throw new NullPointerException();
        }
        entities.forEach(entity -> {
            if (entity == null) {
                throw new NullPointerException();
            }
        });
        getJdbcTemplate().insert(entities);
        return entities;
    }

    public List<Entity> insert(List<Entity> entities) throws NullPointerException, SQLite3MappingException {
        if (!cacheEnabled) {
            return insertWithNoCache(entities);
        }
        entityCacheWriteLock(() -> {
            Map<ID, Entity> map = new HashMap<>();
            entities.forEach(entity -> {
                map.put(getIdFieldValue(entity), entity);
            });
            insertWithNoCache(entities);
            getEntityCacheMap().putAll(map);
        });
        return entities;
    }

    public Entity updateWithNoCache(Entity entity) throws NullPointerException, SQLite3MappingException {
        if (entity == null) {
            throw new NullPointerException();
        }
        getJdbcTemplate().update(entity);
        return entity;
    }

    public Entity update(Entity entity) throws NullPointerException, SQLite3MappingException {
        if (!cacheEnabled) {
            return updateWithNoCache(entity);
        }
        entityCacheWriteLock(() -> {
            ID id = getIdFieldValue(entity);
            updateWithNoCache(entity);
            getEntityCacheMap().put(id, entity);
        });
        return entity;
    }

    public List<Entity> updateWithNoCache(List<Entity> entities) throws NullPointerException, SQLite3MappingException {
        if (entities == null) {
            throw new NullPointerException();
        }
        entities.forEach(entity -> {
            if (entity == null) {
                throw new NullPointerException();
            }
        });
        getJdbcTemplate().update(entities);
        return entities;
    }

    public List<Entity> update(List<Entity> entities) throws NullPointerException, SQLite3MappingException {
        if (!cacheEnabled) {
            return updateWithNoCache(entities);
        }
        entityCacheWriteLock(() -> {
            Map<ID, Entity> map = new HashMap<>();
            entities.forEach(entity -> {
                map.put(getIdFieldValue(entity), entity);
            });
            updateWithNoCache(entities);
            getEntityCacheMap().putAll(map);
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

    public int deleteByIdWithNoCache(List<ID> idList) throws NullPointerException, SQLite3MappingException {
        if (idList == null) {
            throw new NullPointerException();
        }
        idList.forEach(entity -> {
            if (entity == null) {
                throw new NullPointerException();
            }
        });
        TableModel tableModel = SQLite3Utils.getClassTableModel(getEntityClass());
        return jdbcTemplate.batchUpdate(tableModel.getDeleteSQL(), idList.size(), (index, statement) -> {
            castIDForStatement(statement, 1, idList.get(index));
        });
    }

    public int deleteById(List<ID> idList) throws NullPointerException, SQLite3MappingException {
        if (!cacheEnabled) {
            return deleteByIdWithNoCache(idList);
        }
        AtomicReference<Integer> ref = new AtomicReference<>();
        entityCacheWriteLock(() -> {
            ref.set(deleteByIdWithNoCache(idList));
            idList.forEach(getEntityCacheMap()::remove);
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
