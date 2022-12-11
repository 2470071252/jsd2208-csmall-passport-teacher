package cn.tedu.csmall.passport.mapper;

import cn.tedu.csmall.passport.pojo.entity.Admin;
import cn.tedu.csmall.passport.pojo.vo.AdminListItemVO;
import cn.tedu.csmall.passport.pojo.vo.AdminStandardVO;
import org.springframework.stereotype.Repository;

import java.util.List;

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
     * 批量插入管理员数据
     *
     * @param adminList 若干个管理员数据的集合
     * @return 受影响的行数
     */
    int insertBatch(List<Admin> adminList);

    /**
     * 根据管理员id删除管理员数据
     * @param id 管理员id
     * @return 受影响的行数
     */
    int deleteById(Long id);

    /**
     * 批量删除管理员
     *
     * @param ids 需要删除的若干个管理员的id
     * @return 受影响的行数
     */
    int deleteByIds(Long[] ids);

    /**
     * 根据管理员id修改管理员的数据
     *
     * @param admin 封装了管理员id和新的数据的对象
     * @return 受影响的行数
     */
    int update(Admin admin);

    /**
     * 统计管理员数据的数量
     *
     * @return 管理员数据的数量
     */
    int count();

    /**
     * 根据用户名统计管理员数据的数量
     *
     * @param username 用户名
     * @return 匹配用户名的管理员数据的数据
     */
    int countByUsername(String username);

    /**
     * 根据手机号码统计管理员数据的数量
     *
     * @param phone 手机号码
     * @return 匹配手机号码的管理员数据的数据
     */
    int countByPhone(String phone);

    /**
     * 根据电子邮箱统计管理员数据的数量
     *
     * @param email 电子邮箱
     * @return 匹配电子邮箱的管理员数据的数据
     */
    int countByEmail(String email);

    /**
     * 根据管理员id查询管理员数据详情
     * @param id 管理员id
     * @return 匹配的管理员数据详情，如果没有匹配的数据，则返回null
     */
    AdminStandardVO getStandardById(Long id);

    /**
     * 查询管理员数据列表
     *
     * @return 管理员数据列表
     */
    List<AdminListItemVO> list();

}
