package com.luoyang.tourism.module.file.mapper;

import com.luoyang.tourism.module.file.pojo.entity.FileObject;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * FileMapper MyBatis 数据访问接口。
 * <p>SQL 只在本层维护，业务层不得拼接用户可控 SQL。</p>
 */
@Mapper
public interface FileMapper {
    FileObject selectById(@Param("id") Long id);
    FileObject selectByObjectKey(@Param("objectKey") String objectKey);
    List<FileObject> selectFiles(@Param("bizScene") String bizScene, @Param("status") Integer status,
                                 @Param("offset") long offset, @Param("limit") int limit);
    long countFiles(@Param("bizScene") String bizScene, @Param("status") Integer status);
    List<FileObject> selectByIds(@Param("ids") List<Long> ids);
    int insert(FileObject file);
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int softDelete(@Param("id") Long id);
    int countReferences(@Param("id") Long id);
}
