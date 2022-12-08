package cn.tedu.csmall.passport.mapper;

import cn.tedu.csmall.passport.pojo.entity.Admin;
import org.springframework.stereotype.Repository;

/**
 * 处理管理员数据的Mapper接口
 *
 * @author java@tedu.cn
 * @version 0.0.1
 */
@Repository
public interface AdminMapper {

    /**
     * 插入管理员数据
     *
     * @param admin 管理员数据
     * @return 受影响的行数
     */
    int insert(Admin admin);

    /**
     * 根据用户名统计管理员的数量
     *
     * @param username 用户名
     * @return 匹配用户名的管理员的数据
     */
    int countByUsername(String username);

    /**
     * 根据手机号码统计管理员的数量
     *
     * @param phone 手机号码
     * @return 匹配手机号码的管理员的数据
     */
    int countByPhone(String phone);

    /**
     * 根据电子邮箱统计管理员的数量
     *
     * @param email 电子邮箱
     * @return 匹配电子邮箱的管理员的数据
     */
    int countByEmail(String email);

}
