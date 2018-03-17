package com.jackaroo.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.jackaroo.common.Const;
import com.jackaroo.common.ResponseCode;
import com.jackaroo.common.ServerResponse;
import com.jackaroo.dao.CategoryMapper;
import com.jackaroo.dao.ProductMapper;
import com.jackaroo.pojo.Category;
import com.jackaroo.pojo.Product;
import com.jackaroo.service.ICategoryService;
import com.jackaroo.service.IProductService;
import com.jackaroo.util.DateTimeUtil;
import com.jackaroo.util.PropertiesUtil;
import com.jackaroo.vo.ProductDetailVo;
import com.jackaroo.vo.ProductListVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 依据product对象的id属性是否有值，保存或者更新商品
     * @param product
     * @return
     */
    @Override
    public ServerResponse saveOrUpdateProduct(Product product) {
        if (product != null) {
            // 检查product属性
            ServerResponse serverResponse = checkProduct(product);
            if (!serverResponse.isSuccess())
                return serverResponse;
            // 将商品的第一个子图作为商品的主图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImages = product.getSubImages().split(",");
                if (subImages.length > 0)
                    product.setMainImage(subImages[0]);
            }
            // 根据是否存在id，判断是更新还是新增操作
            if (product.getId() != null) {
                // 更新
                int rowCount = productMapper.updateByPrimaryKey(product);
                if (rowCount > 0)
                    return ServerResponse.createBySuccess("更新商品信息成功");
                else
                    return ServerResponse.createByErrorMessage("更新商品信息失败");
            } else {
                // 新增
                int rowCount = productMapper.insert(product);
                if (rowCount > 0)
                    return ServerResponse.createBySuccess("新增商品信息成功");
                else
                    return ServerResponse.createByErrorMessage("新增商品信息失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增商品的参数错误");
    }

    /**
     *  设置指定id 的商品的状态
     * @param productId
     * @param status
     * @return
     */
    @Override
    public ServerResponse setSaleStatus(Integer productId, Integer status) {
        if (productId == null || status == null)
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode()
                    , ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        // 有选择性的更新，即只更新属性值不为null的属性
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if (rowCount > 0)
            return ServerResponse.createBySuccess("更新商品状态成功");
        else
            return ServerResponse.createByErrorMessage("更新商品状态失败");
    }

    /**
     * 获取指定id 的商品详情
     * @param productId
     * @return
     */
    @Override
    public ServerResponse manageProductDetail(Integer productId) {
        if (productId != null) {
            Product product = productMapper.selectByPrimaryKey(productId);
            if (product == null) {
                return ServerResponse.createByErrorMessage("商品不存在或者已下架");
            } else {
                // 由于pojo对象中的很多属性并不能传给前端，所有将pojo对象的部分值赋值给vo对象
                ProductDetailVo productDetailVo = assembleProductDetailVo(product);
                return ServerResponse.createBySuccess(productDetailVo);
            }
        } else {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode()
                    , ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
    }

    /**
     * 获取商品列表，分页查询
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse getProductList(int pageNum, int pageSize) {
        // 分页插件利用AOP的方式，实现分页查询
        // 设置页码、每页大小
        PageHelper.startPage(pageNum, pageSize);
        List<Product> productList = productMapper.selectList();
        // 装配vo
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 根据商品名称、id 查询商品，分页查询
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @Override
    public ServerResponse searchProduct(String productName, Integer productId
            , int pageNum, int pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        // 判断是否传入商品名称，传入则配置模糊查询条件
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%")
                    .toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);
        // 装配vo
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageResult = new PageInfo(productList);
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    /**
     * 获取指定id 商品的详情
     * @param productId
     * @return
     */
    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {

        if (productId == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode()
                    , ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 查询指定id的商品，不设置状态条件的限制
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("产品已经下架或者删除");
        }
        // 只返回在线商品的信息
        if (product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()) {
            return ServerResponse.createByErrorMessage("产品已经下架或者删除");
        }
        // 装配vo
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    /**
     * 通过关键字和分类id，获取商品列表，分页查询
     * @param keyword
     * @param categoryId
     * @param pageNum
     * @param pageSize
     * @param orderBy
     * @return
     */
    @Override
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword
            ,Integer categoryId,int pageNum,int pageSize,String orderBy) {
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode()
                    , ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<>();

        if (categoryId != null) {
            // 查询指定id的分类，如果存在该分类，则平级调用CategoryService查询该分类以及该分类下的所有子分类
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if (category == null && StringUtils.isBlank(keyword)) {
                // 该分类不存在，并且没有关键字，返回一个空的结果集
                PageHelper.startPage(pageNum, pageSize);
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();
        }
        // 设置商品名称模糊查询的条件
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        PageHelper.startPage(pageNum, pageSize);
        // 设置返回商品信息的排序方式
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)) {
                String[] orderByArray = orderBy.split("_");
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        // 根据商品名称和商品分类查询商品
        String productName = StringUtils.isBlank(keyword) ? null : keyword;
        List<Integer> categoryIds = categoryIdList.size() == 0 ? null : categoryIdList;
        List<Product> productList = productMapper.selectByNameAndCategoryIds(productName, categoryIds);
        // 装配vo
        List<ProductListVo> productListVoList = Lists.newArrayList();
        for (Product productItem : productList) {
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }


    /**
     * 通过Product对象装配ProductListVo对象
     * @param product
     * @return
     */
    private ProductListVo assembleProductListVo(Product product) {
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    /**
     * 通过Product对象装配ProductDetailVo对象
     * @param product
     * @return
     */
    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());
        // 将配置文件中的图片服务器的主机地址赋值给商品的imageHost属性
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        // 根据商品的分类id，获取该分类的上一级分类id，并赋值给商品的parentId属性
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);//默认根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }
        // 将Date对象所表示的时间，转化为指定时间格式的字符串形式
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }

    /**
     * 检查在插入或者商品的时候，必需的属性是否赋值
     * @param product
     * @return
     */
    private ServerResponse checkProduct(Product product) {
        boolean flag = true;
        if (product.getName() == null) flag = false;
        if (product.getCategoryId() == null) flag = false;
        if (product.getPrice() == null) flag = false;
        if (product.getStock() == null) flag = false;
        if (product.getStatus() == null) flag = false;
        if (flag)
            return ServerResponse.createBySuccess();
        else
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode()
                    , ResponseCode.ILLEGAL_ARGUMENT.getDesc());
    }


}
