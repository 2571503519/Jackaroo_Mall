package com.jackaroo.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jackaroo.common.ServerResponse;
import com.jackaroo.dao.CategoryMapper;
import com.jackaroo.pojo.Category;
import com.jackaroo.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service("iCategoryService")
public class CategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 为指定分类下添加子分类
     * @param categoryName
     * @param parentId
     * @return
     */
    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        // 检查父分类是否为空
        if (parentId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("添加分类参数错误");
        }
        // 构建分类对象
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        category.setStatus(true); // 设置状态为正常
        int resultCount = categoryMapper.insert(category);
        if (resultCount > 0) {
            return ServerResponse.createBySuccess("添加分类成功");
        }
        return ServerResponse.createByErrorMessage("添加分类失败");
    }

    /**
     * 通过id更新分类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    @Override
    public ServerResponse updateCategoryName(Integer categoryId, String categoryName) {
        if (categoryId == null || StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("更新分类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int rowCount = categoryMapper.updateByPrimaryKeySelective(category);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("更新分类名称成功");
        }
        return ServerResponse.createByErrorMessage("更新分类名称失败");
    }

    /**
     * 获取指定id 下面一级的子分类
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(categoryId);
        if (CollectionUtils.isEmpty(categoryList)) {
            logger.info("未找到分类id为" + categoryId + "下的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 获取指定id下面所有的子分类id（包括自身）
     * @param categoryId
     * @return
     */
    @Override
    public ServerResponse<List<Integer>> selectCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet, categoryId);
        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null) {
            for (Category categoryItem : categorySet) {
                categoryIdList.add(categoryItem.getId());
            }
        }
        return ServerResponse.createBySuccess(categoryIdList);
    }

    /**
     * 通过递归查询指定分类下的所有子分类
     * @param categorySet
     * @param categoryId
     * @return
     */
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
        // 递归的结束条件为指定的id在数据库中没有相应的记录
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if (category != null) {
            categorySet.add(category);
        } else {
            return categorySet;
        }
        List<Category> categoryList = categoryMapper.selectCategoryChildrenByParentId(category.getId());
        for (Category categoryItem : categoryList) {
            findChildCategory(categorySet, categoryItem.getId());
        }
        return categorySet;
    }
}
