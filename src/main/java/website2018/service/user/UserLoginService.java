package website2018.service.user;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springside.modules.utils.mapper.BeanMapper;
import website2018.domain.User;
import website2018.dto.user.ReturnResponse;
import website2018.dto.user.UserDTO;
import website2018.repository.UserDao;
import website2018.utils.MD5Util;
import website2018.utils.SysConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by Administrator on 2018/9/24.
 */
@Service
public class UserLoginService {


    @Autowired
    UserDao userDao;

    public User findByUserName(String userName){
     User user=   userDao.findByUserName(userName);
        return user;
    }

    public ReturnResponse checkUserLogin(HttpServletRequest request,UserDTO userDTO){
        ReturnResponse response=new ReturnResponse();

        if(userDTO==null|| StringUtils.isEmpty(userDTO.userName)||StringUtils.isEmpty(userDTO.password)){
            response.code="0002";
            response.message="请求参数错误";
            return response;
        }
        User user=   userDao.findByUserName(userDTO.userName);
        if(user==null){
            User  u = BeanMapper.map(userDTO, User.class);
            u.password=MD5Util.MD5(userDTO.password);
            userDao.save(u);
        }else{
            String password= MD5Util.MD5(userDTO.password);
            if(!password.equals(user.password)){
                response.code="0003";
                response.message="密码错误";
                return response;
            }
        }
        request.getSession().setAttribute(SysConstants.USER_LOGIN_FLAG,userDTO);
        response.code="0000";
        response.message="登录成功";
        return response;
    }

    public ReturnResponse doUserRegister(HttpServletRequest request,UserDTO userDTO){
        ReturnResponse response=new ReturnResponse();

        if(userDTO==null|| StringUtils.isEmpty(userDTO.userName)||StringUtils.isEmpty(userDTO.password)){
            response.code="0002";
            response.message="请求参数错误";
            return response;
        }

        if(userDTO.password.equals(userDTO.rePassword)){
            response.code="0005";
            response.message="2次输入密码不一致";
            return response;
        }
        User user=   userDao.findByUserName(userDTO.userName);
        if(user==null){
            User  u = BeanMapper.map(userDTO, User.class);
            u.password=MD5Util.MD5(userDTO.password);
            Date d=new Date();
            u.addTime=d;
            u.updateTime=d;
            userDao.save(u);
        }else{
            response.code="0004";
            response.message="用户名已存在";
            return response;
        }
        request.getSession().setAttribute(SysConstants.USER_LOGIN_FLAG,userDTO);
        response.code="0000";
        response.message="注册成功";
        return response;
    }
}
