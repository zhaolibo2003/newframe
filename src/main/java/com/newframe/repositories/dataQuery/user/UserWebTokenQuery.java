package com.newframe.repositories.dataQuery.user;

import com.newframe.entity.user.UserWebToken;
import com.newframe.utils.query.BaseQuery;
import com.newframe.utils.query.annotation.QBindEntity;
import lombok.Data;

@Data
@QBindEntity(entityClass = UserWebToken.class)
public class UserWebTokenQuery extends BaseQuery {
}