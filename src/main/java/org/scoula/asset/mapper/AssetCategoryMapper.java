package org.scoula.asset.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AssetCategoryMapper {

    Long findIdByNameAndGlobal(String name);
}
