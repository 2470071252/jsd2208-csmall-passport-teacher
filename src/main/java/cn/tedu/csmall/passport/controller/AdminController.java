package cn.tedu.csmall.passport.controller;

import cn.tedu.csmall.passport.pojo.dto.AdminAddNewDTO;
import cn.tedu.csmall.passport.pojo.vo.AdminListItemVO;
import cn.tedu.csmall.passport.service.IAdminService;
import cn.tedu.csmall.passport.web.JsonResult;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admins")
@Api(tags = "管理员管理模块")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @ApiOperation("添加管理员")
    @ApiOperationSupport(order = 100)
    @PostMapping("/add-new")
    public JsonResult addNew(AdminAddNewDTO adminAddNewDTO) {
        log.debug("开始处理【添加管理员】的请求，参数：{}", adminAddNewDTO);
        adminService.addNew(adminAddNewDTO);
        return JsonResult.ok();
    }

    // http://localhost:9081/admins
    @ApiOperation("查询管理员列表")
    @ApiOperationSupport(order = 420)
    @GetMapping("")
    public JsonResult list() {
        log.debug("开始处理【查询管理员列表】的请求，参数：无");
        List<AdminListItemVO> list = adminService.list();
        return JsonResult.ok(list);
    }

}
