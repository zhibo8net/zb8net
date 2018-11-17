package website2018.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springside.modules.web.MediaTypes;
import website2018.base.BaseEndPoint;
import website2018.dto.user.ReturnResponse;
import website2018.dto.user.UserDTO;
import website2018.service.user.UserLoginService;
import website2018.utils.SysConstants;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UserController extends BaseEndPoint {
    
    @Autowired
    UserLoginService userLoginService;
    
    @RequestMapping(value = "/api/user/login", produces = MediaTypes.JSON_UTF_8)
    public ReturnResponse userLogin(@RequestBody(required=false) UserDTO userDTO,HttpServletRequest request) {



        return userLoginService.checkUserLogin(request,userDTO);
    }
    @RequestMapping(value = "/api/user/register", produces = MediaTypes.JSON_UTF_8)
    public ReturnResponse userRegister(@RequestBody(required=false) UserDTO userDTO,HttpServletRequest request) {


        return userLoginService.doUserRegister(request, userDTO);
    }

    @RequestMapping(value = "/checkUser",produces = MediaTypes.JSON_UTF_8)
    public ReturnResponse checkUser(HttpServletRequest request) {
        UserDTO userDTO= (UserDTO) request.getSession().getAttribute(SysConstants.USER_LOGIN_FLAG);
        ReturnResponse response=new ReturnResponse();

        if(userDTO==null){
            response.code="0001";
            response.message="用户未登录";
            return response;
        }
        response.code="0000";
        response.message="用户已登录";
        userDTO.password="";
        userDTO.rePassword="";
        response.data=userDTO;
        return response;
    }
    }
