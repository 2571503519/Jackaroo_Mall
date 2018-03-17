package com.jackaroo.service.impl;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.jackaroo.common.Const;
import com.jackaroo.common.ResponseCode;
import com.jackaroo.common.ServerResponse;
import com.jackaroo.dao.CartMapper;
import com.jackaroo.dao.ProductMapper;
import com.jackaroo.pojo.Cart;
import com.jackaroo.pojo.Product;
import com.jackaroo.service.ICartService;
import com.jackaroo.util.BigDecimalUtil;
import com.jackaroo.util.PropertiesUtil;
import com.jackaroo.vo.CartProductVo;
import com.jackaroo.vo.CartVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取用户购物车中所有的商品
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = this.getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
    }

    /**
     * 向购物车中添加商品，如果该商品以存在，则增加商品的数量
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse<CartVo> add(Integer userId,Integer productId,Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode()
                    ,ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        // 判断该商品是否存在
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("该商品已经下架或者不存在");
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if (cart == null){
            //这个产品不在这个购物车里,需要新增一个这个产品的记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);
        } else {
            //这个产品已经在购物车里了.
            //如果产品已存在,数量相加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    /**
     * 更新商品的购买数量
     * @param userId
     * @param productId
     * @param count
     * @return
     */
    @Override
    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);
        if (cart != null) {
            cart.setQuantity(count);
        }
        cartMapper.updateByPrimaryKey(cart);
        return this.list(userId);
    }

    /**
     * 删除购物车中的指定商品，可以批量删除，productId之间使用"," 隔开
     * @param userId
     * @param productIds
     * @return
     */
    @Override
    public ServerResponse<CartVo> deleteProduct(Integer userId,String productIds){
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){
            return ServerResponse.createByErrorMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productList);
        return this.list(userId);
    }

    /**
     * 全选或者全不选
     * @param userId
     * @param productId
     * @param checked
     * @return
     */
    @Override
    public ServerResponse<CartVo> selectOrUnSelect (Integer userId,Integer productId,Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
    }

    /**
     * 获取购物车中所有商品的数量
     * @param userId
     * @return
     */
    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){
            return ServerResponse.createBySuccess(ResponseCode.SUCCESS.getCode());
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));
    }

    /**
     * 获取指定id 的用户的CartVo对象。
     * 获取购物车中的记录 --> 根据productId 获取商品信息 ——> 判断库存 --> 更新购物车中的记录 --> 计算价格
     * @param userId
     * @return
     */
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        // 获取用户购物车中的记录
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        List<CartProductVo> cartProductVoList = Lists.newArrayList();
        // 购物车中选中商品的总价
        BigDecimal cartTotalPrice = new BigDecimal("0");

        if (CollectionUtils.isNotEmpty(cartList)) {
            // 装配cartProductVoList对象
            for (Cart cartItem : cartList) {
                // 购物车中的商品对象
                CartProductVo cartProductVo = new CartProductVo();
                cartProductVo.setId(cartItem.getId());
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());
                // 根据商品的id查询商品
                Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                // 将商品的信息装配到CartProductVo对象
                if (product != null) {
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());
                    // 判断库存
                    int buyLimitCount = 0;
                    if (product.getStock() >= cartItem.getQuantity()) {
                        // 库存足够
                        buyLimitCount = cartItem.getQuantity();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);
                    } else {
                        // 库存不足时，最多只能购买库存中剩余数量的商品
                        buyLimitCount = product.getStock();
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    // 设置商品数量
                    cartProductVo.setQuantity(buyLimitCount);
                    //计算这件商品的总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue()
                            ,cartProductVo.getQuantity()));
                    // 设置是否选中
                    cartProductVo.setProductChecked(cartItem.getChecked());
                }
                if(cartItem.getChecked() == Const.Cart.CHECKED){
                    //如果已经勾选,增加到整个的购物车总价中
                    // 计算购物车中所有已选中的商品的总价
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());
                }
                cartProductVoList.add(cartProductVo);
            }
        }
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        // 判断是否购物车中的所有商品都被选中
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        // 获取用户没有选中的商品数量，如果等于零，则代表全选
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;
    }

}
