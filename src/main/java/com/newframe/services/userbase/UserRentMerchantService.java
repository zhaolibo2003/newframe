package com.newframe.services.userbase;

import com.newframe.entity.user.UserRentMerchant;

/**
 *
 *  租赁商
 *
 * This class corresponds to the database table user_rent_merchant
 *
 * app用户的token
 * @mbggenerated do_not_delete_during_merge
 */
public interface UserRentMerchantService {

    /**
     * 获取用户租赁商信息
     * @param uid
     * @return
     */
    UserRentMerchant findOne(Long uid);

    /**
     * 插入用户租赁商信息
     * @param userRentMerchant
     * @return
     */
    UserRentMerchant insert(UserRentMerchant userRentMerchant);

    /**
     * 更新用户租赁商信息
     * @param userRentMerchant
     * @return
     */
    int update(UserRentMerchant userRentMerchant);

    /**
     * 删除用户租赁商
     * @param uid
     */
    void delete(Long uid);
}