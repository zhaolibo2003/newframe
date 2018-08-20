package com.newframe.controllers.api;

import com.newframe.controllers.BaseController;
import com.newframe.controllers.JsonResult;
import com.newframe.dto.OperationResult;
import com.newframe.dto.common.ExpressInfo;
import com.newframe.services.common.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author:wangdong
 * @description:
 */
/**
 * @author:wangdong
 * @description:根据物流公司和单号去查询快递
 */
@RestController
@RequestMapping("/api/common/")
public class CommonController extends BaseController {

    @Autowired
    private CommonService commonService;

    @RequestMapping("getExpressMessage")
    private JsonResult getExpressMessage(String expCode, String expNo){

        OperationResult<ExpressInfo> result = commonService.getExpressMessage(expCode,expNo);
        if (result.getSucc()) {
            return success(result.getEntity());
        }
        return error(result.getCode(), result.getMessage());

    }
}
