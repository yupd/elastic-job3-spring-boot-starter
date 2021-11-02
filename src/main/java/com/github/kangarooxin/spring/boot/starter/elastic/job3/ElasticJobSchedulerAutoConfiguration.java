package com.github.kangarooxin.spring.boot.starter.elastic.job3;

import com.github.kangarooxin.spring.boot.starter.elastic.job3.constant.Constants;
import com.github.kangarooxin.spring.boot.starter.elastic.job3.properties.ElasticJobSchedulerProperties;
import com.github.kangarooxin.spring.boot.starter.elastic.job3.service.ElasticJobService;
import com.github.kangarooxin.spring.boot.starter.elastic.job3.service.impl.ElasticJobServiceImpl;
import org.apache.shardingsphere.elasticjob.infra.env.IpUtils;
import org.apache.shardingsphere.elasticjob.reg.zookeeper.ZookeeperRegistryCenter;
import org.apache.shardingsphere.elasticjob.tracing.api.TracingConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

/**
 * @author kangaroo_xin
 */
@Configuration
@EnableConfigurationProperties({ElasticJobSchedulerProperties.class})
@Import({ElasticJobSchedulerAspect.class})
@ConditionalOnProperty(prefix = Constants.ELASTIC_JOB_PREFIX, name = "enabled", matchIfMissing = true)
public class ElasticJobSchedulerAutoConfiguration implements ApplicationContextAware {

    @Autowired
    private ElasticJobSchedulerProperties properties;

    @Autowired
    private ZookeeperRegistryCenter elasticJobRegCenter;

    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        ElasticJobSchedulerProperties.Network network = properties.getNetwork();
        if(StringUtils.hasText(network.getPreferredInterface())) {
            System.setProperty(IpUtils.PREFERRED_NETWORK_INTERFACE, network.getPreferredInterface());
        }
        if(StringUtils.hasText(network.getPreferredIp())) {
            System.setProperty(IpUtils.PREFERRED_NETWORK_IP, network.getPreferredIp());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public ElasticJobService elasticJobService() {
        return new ElasticJobServiceImpl(elasticJobRegCenter, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = Constants.ELASTIC_JOB_TRACING_PREFIX, name = "type", havingValue = "RDB")
    public TracingConfiguration<DataSource> tracingRDbConfiguration() {
        DataSource dataSource;
        String beanName = properties.getTracing().getDataSourceBeanName();
        if (StringUtils.hasText(beanName)) {
            dataSource = (DataSource) applicationContext.getBean(beanName);
        } else {
            dataSource = applicationContext.getBean(DataSource.class);
        }
        return new TracingConfiguration<>("RDB", dataSource);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
