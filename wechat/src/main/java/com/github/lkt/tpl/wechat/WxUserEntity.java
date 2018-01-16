package com.github.lkt.tpl.wechat;

/**
 * wx 用户对象
 * Created by tangJ on 2017/12/19
 */
public class WxUserEntity {

  /**
   * {
   * "openid": "oFep60ajlmeZ1_OOqXra-65l8_lk",
   * "nickname": "飞飞",
   * "sex": 2,
   * "language": "zh_CN",
   * "city": "Shenzhen",
   * "province": "Guangdong",
   * "country": "CN",
   * "headimgurl": "http://wx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTJI1W66aZTqXGrYibqWv7ano3QYkD5atwNkja2ElsCo2qPg3wMria37ibn1icBjBwbOpibTJBWlS43Yf1Q/0",
   * "privilege": [],
   * "unionid": "oJWg103RWq1gNedDNlfheesO4gQ0"
   * }
   */

  public String openId;
  public String unionId;
  public String name;
  public int sex; //1.男 2.女
  public String city;
  public String province;
  public String country;
  public String avatar;

  public String errorMsg;
  public boolean ok = false;

  public int errorType; //1.wx  2.网络

}
