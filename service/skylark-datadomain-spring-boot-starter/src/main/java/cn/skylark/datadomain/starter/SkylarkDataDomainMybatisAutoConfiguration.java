package cn.skylark.datadomain.starter;

import cn.skylark.datadomain.starter.mybatis.DataDomainMybatisInterceptor;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis 插件 Bean 独立自动配置：未引入 MyBatis 时不应加载本类，
 * 否则若在「主」自动配置里声明返回 {@link Interceptor} 的 {@code @Bean}，
 * JVM 反射主配置类方法时会要求 classpath 上存在 MyBatis。
 */
@Configuration
@AutoConfigureAfter(name = "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration")
@ConditionalOnClass({SqlSessionFactory.class, Interceptor.class})
public class SkylarkDataDomainMybatisAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "dataDomainMybatisInterceptor")
  public Interceptor dataDomainMybatisInterceptor(SkylarkDataDomainProperties props) {
    return new DataDomainMybatisInterceptor(props);
  }
}
