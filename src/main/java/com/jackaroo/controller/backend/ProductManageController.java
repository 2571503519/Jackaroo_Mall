package com.jackaroo.controller.backend;

import com.google.common.collect.Maps;
import com.jackaroo.common.Const;
import com.jackaroo.common.ResponseCode;
import com.jackaroo.common.ServerResponse;
import com.jackaroo.pojo.Product;
import com.jackaroo.pojo.User;
import com.jackaroo.service.IFileService;
import com.jackaroo.service.IProductService;
import com.jackaroo.service.IUserService;
import com.jackaroo.util.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;


    /**
     * 保存商品
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        // 权限检查
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            if (iUserService.checkAdminRole(user).isSuccess()) {
                return iProductService.saveOrUpdateProduct(product);
            } else {
                return ServerResponse.createByErrorMessage("没有此权限");
            }
        } else {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
    }

    /**
     * 设置商品的状态
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            if (iUserService.checkAdminRole(user).isSuccess()) {
                return iProductService.setSaleStatus(productId, status);
            } else {
                return ServerResponse.createByErrorMessage("没有此权限");
            }
        } else {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
    }

    /**
     * 获取商品的详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            if (iUserService.checkAdminRole(user).isSuccess()) {
                return iProductService.manageProductDetail(productId);
            } else {
                return ServerResponse.createByErrorMessage("没有此权限");
            }
        } else {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
    }

    /**
     * 获取商品列表，分页查询
     * @param session
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum", defaultValue = "1") int pageNum
                                  , @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            if (iUserService.checkAdminRole(user).isSuccess()) {
                return iProductService.getProductList(pageNum, pageSize);
            } else {
                return ServerResponse.createByErrorMessage("没有此权限");
            }
        } else {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
    }

    /**
     * 根据商品的名称、id 查询商品，分页查询
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse getList(HttpSession session, String productName, Integer productId
            , @RequestParam(value = "pageNum", defaultValue = "1") int pageNum
            , @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            if (iUserService.checkAdminRole(user).isSuccess()) {
                return iProductService.searchProduct(productName, productId, pageNum, pageSize);
            } else {
                return ServerResponse.createByErrorMessage("没有此权限");
            }
        } else {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
    }

    /**
     * 文件上传，成功则返回文件的名称和文件的url
     * @param session
     * @param file
     * @param request
     * @return
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse upload(HttpSession session
            , @RequestParam(value = "upload_file",required = false) MultipartFile file
            , HttpServletRequest request) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user != null) {
            if (iUserService.checkAdminRole(user).isSuccess()) {
                String path = request.getSession().getServletContext().getRealPath("upload");
                String targetFileName = iFileService.upload(file, path);
                String url = PropertiesUtil.getProperty("ftp.server.http.prefix")
                        + targetFileName;
                Map fileMap = Maps.newHashMap();
                fileMap.put("uri", targetFileName);
                fileMap.put("url", url);
                return ServerResponse.createBySuccess(fileMap);
            } else {
                return ServerResponse.createByErrorMessage("没有此权限");
            }
        } else {
            return ServerResponse.createByErrorMessage(ResponseCode.NEED_LOGIN.getCode()
                    , ResponseCode.NEED_LOGIN.getDesc());
        }
    }

    /**
     * 富文本中的图片上传，根据Simditor中的要求的json格式返回json数据
     * @param session
     * @param file
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session
            , @RequestParam(value = "upload_file",required = false) MultipartFile file
            , HttpServletRequest request
            , HttpServletResponse response) {

        Map resultMap = Maps.newHashMap();
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "需要登录管理员账户操作");
            return resultMap;
        }
        if (iUserService.checkAdminRole(user).isSuccess()) {
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        } else {
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }












}
