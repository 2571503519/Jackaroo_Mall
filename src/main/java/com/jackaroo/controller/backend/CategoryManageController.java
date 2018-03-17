package com.jackaroo.controller.backend;

import com.jackaroo.common.Const;
import com.jackaroo.common.ResponseCode;
import com.jackaroo.common.ServerResponse;
import com.jackaroo.pojo.Category;
import com.jackaroo.pojo.User;
import com.jackaroo.service.ICategoryService;
import com.jackaroo.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/manage/category")
public class CategoryManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 在指定分类下添加子分类，如果没有指定父分类则默认为一级分类
     * @param session
     * @param categoryName
     * @param parentId
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName
            , @RequestParam(value = "parentId", defaultValue = "0") int parentId) {
        // 检查用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
            , ResponseCode.NEED_LOGIN.getDesc());
        }
        // 检查用户是否是管理员
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.addCategory(categoryName, parentId);
        } else {
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    /**
     * 修改指定id的分类名
     * @param session
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, Integer categoryId
            , String categoryName) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.updateCategoryName(categoryId, categoryName);
        } else {
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    /**
     * 获取指定分类id 下面一级的子分类，默认获取所有一级分类
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse<List<Category>> getChildrenParallelCategory(HttpSession session
            , @RequestParam(value = "categoryId", defaultValue = "0") int categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.getChildrenParallelCategory(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }

    /**
     * 获取指定分类id 以及所有此分类下的子分类的id
     * @param session
     * @param categoryId
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session
            , @RequestParam(value = "categoryId", defaultValue = "0") int categoryId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            return iCategoryService.selectCategoryAndChildrenById(categoryId);
        } else {
            return ServerResponse.createByErrorMessage("无操作权限");
        }
    }



}
