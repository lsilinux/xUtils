package com.lidroid.xutils.db.table;

import android.database.Cursor;
import com.lidroid.xutils.db.sqlite.ColumnDbType;
import com.lidroid.xutils.db.sqlite.FinderLazyLoader;
import com.lidroid.xutils.exception.DbException;
import com.lidroid.xutils.util.LogUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 数据库表的主表关联列的描述
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-9-10
 * Time: 下午7:43
 * </pre>
 * 
 * @author wyouflf
 */
public class Finder extends Column {

    private final String valueColumnName;
    private final String targetColumnName;

    /* package */ Finder(Class<?> entityType, Field field) {
        super(entityType, field);

        com.lidroid.xutils.db.annotation.Finder finder =
                field.getAnnotation(com.lidroid.xutils.db.annotation.Finder.class);
        this.valueColumnName = finder.valueColumn();
        this.targetColumnName = finder.targetColumn();
    }

    /**
     * 获取该关联列对应的从表实体类类型
     * @return 实体类类型{@link java.lang.Class}
     */
    public Class<?> getTargetEntityType() {
        return ColumnUtils.getFinderTargetEntityType(this);
    }

    /**
     * 获取该关联列对应的从表列名
     * @return 关联列的从表列名
     */
    public String getTargetColumnName() {
        return targetColumnName;
    }

    @Override
    public void setValue2Entity(Object entity, Cursor cursor, int index) {
        Object value = null;
        Class<?> columnType = columnField.getType();
        Object finderValue = TableUtils.getColumnOrId(entity.getClass(), this.valueColumnName).getColumnValue(entity);
        if (columnType.equals(FinderLazyLoader.class)) {
            value = new FinderLazyLoader(this, finderValue);
        } else if (columnType.equals(List.class)) {
            try {
                value = new FinderLazyLoader(this, finderValue).getAllFromDb();
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
        } else {
            try {
                value = new FinderLazyLoader(this, finderValue).getFirstFromDb();
            } catch (DbException e) {
                LogUtils.e(e.getMessage(), e);
            }
        }

        if (setMethod != null) {
            try {
                setMethod.invoke(entity, value);
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        } else {
            try {
                this.columnField.setAccessible(true);
                this.columnField.set(entity, value);
            } catch (Throwable e) {
                LogUtils.e(e.getMessage(), e);
            }
        }
    }

    @Override
    public Object getColumnValue(Object entity) {
        return null;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    /**
     * 获取数据库列的默认值（永远返回null）
     * @return null（永远返回null）
     */
    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.TEXT;
    }
    
}
