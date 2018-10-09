package com.newframe.services.user.impl;

import com.newframe.common.cache.CachePrefix;
import com.newframe.configuration.rabbitmq.QueueConstants;
import com.newframe.dto.LoginInfo;
import com.newframe.dto.OperationResult;
import com.newframe.dto.mq.AliVcode;
import com.newframe.entity.user.User;
import com.newframe.enums.BizErrorCode;
import com.newframe.enums.CodeStatus;
import com.newframe.enums.SystemCode;
import com.newframe.enums.User.ApplyStatusEnum;
import com.newframe.enums.User.UserRoleEnum;
import com.newframe.enums.User.UserStatusEnum;
import com.newframe.enums.sms.AliyunSMSTemplateEnum;
import com.newframe.enums.sms.McodeTypeEnum;
import com.newframe.provider.MessageProvider;
import com.newframe.services.sms.CodeService;
import com.newframe.services.user.UserManageService;
import com.newframe.services.user.UserService;
import com.newframe.utils.log.GwsLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author:wangdong
 * @description:用户管理类的Service
 */
@Service
public class UserManageServiceImpl implements UserManageService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CodeService codeService;

    @Autowired
    private UserService userService;

    @Value("${aliyun.user.avatar}")
    private String aliyunUserAvatar;

    @Autowired
    private MessageProvider messageProvider;

    /**
     * 根据手机号获取验证码
     *
     * @param mobile
     * @param mcodeType
     * @return
     */
    @Override
    public OperationResult<String> getMcode(String mobile, Integer mcodeType) {
        if (StringUtils.isEmpty(mobile) || null == mcodeType) {
            return new OperationResult<>(BizErrorCode.PARM_ERROR);
        }
        OperationResult<String> result = new OperationResult<>();
        if (McodeTypeEnum.REGISTER_OR_LOGIN.getCode().equals(mcodeType)){
            //将实体信息写入消息队列
            AliVcode aliVcode = new AliVcode(mobile,AliyunSMSTemplateEnum.REGISTER_OR_LOGIN.getTemplateCode(),randomVcode(6));
            //用消息队列去发短信
            result  = sendMcode(aliVcode, McodeTypeEnum.REGISTER_OR_LOGIN);
        }else {
            return new OperationResult<>(BizErrorCode.MCODE_TYPE_ERROR);
        }
        if (result.getSucc()){
            return new OperationResult<>(result.getEntity());
        }

        return new OperationResult<>(result.getErrorCode());
    }

    @Override
    public OperationResult<String> sendMcode(AliVcode aliVcode, McodeTypeEnum mcodeTypeEnum) {

        /**1分钟限制调用1次**/
        String key = CachePrefix.MCODE_LOCK + aliVcode.getMobile();
        if (redisTemplate.opsForValue().setIfAbsent(key, true)) {
            redisTemplate.expire(key, 60, TimeUnit.SECONDS);
        } else {
            return new OperationResult<>(BizErrorCode.MCODE_ONE_MINIT_LIMIT);
        }

        String mcodeKey = new StringBuilder(CachePrefix.MCODE).
                append(mcodeTypeEnum.getCode()).append("_").append(aliVcode.getMobile()).toString();
        //判读这个验证码，是否被消费过，如果消费过就不再发了
        Boolean result = redisTemplate.hasKey(mcodeKey);
        if (!result) {
            //将实体信息写入消息队列
            messageProvider.sendMessage(aliVcode, QueueConstants.MESSAGE_ALIEXCHANGE, QueueConstants.MESSAGE_ROUTE_SENDCODE);
            redisTemplate.opsForValue().set(mcodeKey.toString(), aliVcode.getCode(), 10, TimeUnit.MINUTES);
            GwsLogger.info("给{}发送{}验证码【{}】", aliVcode.getMobile(), mcodeTypeEnum.getMessage(), aliVcode.getCode());
        }

        return new OperationResult<>(aliVcode.getCode());
    }

    /**
     * 【获取指定位数的数字验证码】
     * @param bit 位数
     * @return
     */
    private String randomVcode(int bit) {
        StringBuilder vcode = new StringBuilder();
        for(int i=0; i<bit; i++) {
            vcode.append(new Random().nextInt(9));
        }
        return vcode.toString();
    }

    /**
     * 用户的注册或者登陆
     *
     * @param mobile
     * @param mcode
     * @param roleType
     * @return
     */
    @Override
    public OperationResult<LoginInfo> mobileRegisterOrLogin(String mobile, String mcode, Integer roleType) {

        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(mcode)) {
            return new OperationResult<>(BizErrorCode.PARM_ERROR);
        }
        LoginInfo loginInfo = new LoginInfo();
        //查看这个手机号是否已经注册过
        User user = userService.getUser(mobile);
        //没有注册,验证验证码是否正确，走注册，再走登陆流程
        if (null == user){
            CodeStatus codeStatus = validateMcode(mobile, McodeTypeEnum.REGISTER_OR_LOGIN, mcode);
            if (!SystemCode.SUCCESS.equals(codeStatus)) {
                return new OperationResult<>(codeStatus);
            }
            User u = saveUserByMobile(mobile, roleType);
            loginInfo.setUid(u.getUid());
            //操作token,将token放到redis，并存到数据库
            String token = UUID.randomUUID().toString();
            loginInfo.setToken(token);
            setUserToken(loginInfo);
            return new OperationResult<>(loginInfo);
        }else {

            //如果注册了，校验验证码是否正确，直接走登录流程
            CodeStatus codeStatus = validateMcode(mobile, McodeTypeEnum.REGISTER_OR_LOGIN, mcode);
            if (!SystemCode.SUCCESS.equals(codeStatus)) {
                return new OperationResult<>(codeStatus);
            }
            //操作token,将token放到redis，并存到数据库
            String token = UUID.randomUUID().toString();
            loginInfo.setToken(token);
            setUserToken(loginInfo);
            return new OperationResult<>(loginInfo);
        }
    }

    /**
     * 将用户的Token放到redis中
     * @param loginInfo
     */
    private void setUserToken(LoginInfo loginInfo) {
        if (null == loginInfo){
            return;
        }
        Long uid = loginInfo.getUid();
        String key = CachePrefix.U_TOKEN+uid;
        //判断redis中是否有这个token
        Boolean result = redisTemplate.hasKey(key);
        //如果存在则删除
        if (true == result){
            redisTemplate.delete(key);
        }
        //重新设置token的redis
        redisTemplate.opsForValue().set(key,loginInfo.getToken());
        //目前为了测试方便，暂时设置过期时间为2小时
        redisTemplate.expire(key,2, TimeUnit.HOURS);
    }

    private User saveUserByMobile(String mobile, Integer roleType) {
        if (StringUtils.isEmpty(mobile)){
            return null;
        }

        User user = new User();
        user.setAvatar(aliyunUserAvatar);
        user.setMobile(mobile);
        user.setApplyStatus(ApplyStatusEnum.ADDSHEET.getCode());
        user.setRole(UserRoleEnum.getEnum(roleType).getCode());
        user.setUserStatus(UserStatusEnum.NORMAL.getCode());
        return userService.saveUser(user);
    }

    /**
     * 校验手机的验证码
     *
     * @param mobile
     * @param mcode
     * @return
     */
    private CodeStatus validateMcode(String mobile, McodeTypeEnum mcodeTypeEnum, String mcode) {
        if (StringUtils.isEmpty(mobile) || StringUtils.isEmpty(mcode)) {
            return BizErrorCode.PARM_ERROR;
        }
        //校验验证码
        Boolean result = codeService.isValidMcode(mobile, mcodeTypeEnum, mcode);

        if (result) {
            return SystemCode.SUCCESS;
        }
        return BizErrorCode.MCODE_VERIFY_ERROR;
    }
}