package com.luoyang.tourism.module.file.mapper;

import com.luoyang.tourism.module.file.entity.FileObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
