# elastic-job3-spring-boot-starter

extend offical elasticjob-lite-spring-boot-starter, support configure job with annotation.

## Usage:
1. import dependency in pom.xml
```
<dependency>
    <groupId>io.github.yupd</groupId>
    <artifactId>elastic-job3-spring-boot-starter</artifactId>
    <version>1.0.3</version>
</dependency>
```
2. config in properties
```properties
   #reg-center
   elasticjob.reg-center.server-lists=localhost:2181
   elasticjob.reg-center.namespace=${spring.application.name}.elastic-job
```
3. use with annotation
```java
@Service
@ElasticJobScheduler(cron = "0/5 * * * * ?", shardingTotalCount = 4, name = "SimpleElasticJob",
        shardingItemParameters = "0=0,1=0,2=1,3=1", jobParameters = "parameter")
@Slf4j
public class SimpleElasticJob implements SimpleJob {

   @Override
   public void execute(ShardingContext shardingContext) {
      log.info("Thread ID: {}, ShardingTotalCount: {}, ShardingItem: {}, ShardingParameter: {}, JobName: {}, JobParameter: {}",
              Thread.currentThread().getId(),
              shardingContext.getShardingTotalCount(),
              shardingContext.getShardingItem(),
              shardingContext.getShardingParameter(),
              shardingContext.getJobName(),
              shardingContext.getJobParameter()
      );
   }
}
```
4. configure multiple job
```java
@Service
@ElasticJobScheduler(cron = "0/5 * * * * ?", shardingTotalCount = 4, name = "SimpleElasticJobLevel1",
        shardingItemParameters = "0=0,1=0,2=1,3=1", jobParameters = "parameter")
@ElasticJobScheduler(cron = "0 */1 * * * ?", shardingTotalCount = 4, name = "SimpleElasticJobLevel2",
        shardingItemParameters = "0=0,1=0,2=1,3=1", jobParameters = "parameter")
@Slf4j
public class MultipleElasticJob implements SimpleJob {

   @Override
   public void execute(ShardingContext shardingContext) {
      log.info("Thread ID: {}, ShardingTotalCount: {}, ShardingItem: {}, ShardingParameter: {}, JobName: {}, JobParameter: {}",
              Thread.currentThread().getId(),
              shardingContext.getShardingTotalCount(),
              shardingContext.getShardingItem(),
              shardingContext.getShardingParameter(),
              shardingContext.getJobName(),
              shardingContext.getJobParameter()
      );
   }
}
```
5. if not config cron in annotation, corn can config in properties.
```properties
elasticjob.crons.SimpleElasticJob=0/5 * * * * ?
```
6. use ElasticJobService add job manually.
```java
public interface ElasticJobService {

    /**
     * 添加简单作业
     *
     * @param elasticJob
     * @param jobName
     * @param shardingTotalCount
     * @param cron
     */
    JobBootstrap addJob(ElasticJob elasticJob, String jobName, int shardingTotalCount, String cron);

    /**
     * 添加简单作业
     *
     * @param elasticJob
     * @param jobName
     * @param shardingTotalCount
     * @param cron
     * @param jobParameter
     * @param shardingItemParameters
     */
    JobBootstrap addJob(ElasticJob elasticJob, String jobName, int shardingTotalCount, String cron, String jobParameter, String... shardingItemParameters);

    /**
     * 添加简单作业
     *
     * @param jobConfiguration
     * @param jobBootstrapBeanName 非必填
     * @param elasticJob
     * @return
     */
    JobBootstrap addJob(JobConfiguration jobConfiguration, String jobBootstrapBeanName, ElasticJob elasticJob);

    /**
     * 添加数据流作业
     *
     * @param jobConfiguration
     * @param jobBootstrapBeanName
     * @param elasticJob
     * @param streamingProcess     是否开启流式处理 默认false
     * @return
     */
    <T> JobBootstrap addDataFlowJob(JobConfiguration jobConfiguration, String jobBootstrapBeanName, DataflowJob<T> elasticJob, boolean streamingProcess);

    /**
     * 添加定时Script调度
     *
     * @param jobConfiguration
     * @param scriptCommendLine
     */
    JobBootstrap addScriptJob(JobConfiguration jobConfiguration, String jobBootstrapBeanName, String scriptCommendLine);

    /**
     * 添加定时Http调度
     *
     * @param jobConfiguration
     * @param httpProp
     */
    JobBootstrap addHttpJob(JobConfiguration jobConfiguration, String jobBootstrapBeanName, HttpJobProp httpProp);

    /**
     * 移除job
     *
     * @param jobName
     */
    void removeJob(String jobName);

    /**
     * 移除job
     *
     * @param jobName
     * @param jobBootstrapBeanName
     */
    void removeJob(String jobName, String jobBootstrapBeanName);

    /**
     * get job configuration.
     *
     * @param jobName job name
     * @return job configuration
     */
    JobConfigurationPOJO getJobConfiguration(String jobName);

    /**
     * Update job configuration.
     *
     * @param jobConfig job configuration
     */
    void updateJobConfiguration(JobConfigurationPOJO jobConfig);

    /**
     * Remove job configuration.
     *
     * @param jobName job name
     */
    void removeJobConfiguration(String jobName);

    /**
     * Trigger job to run at once.
     *
     * <p>Job will not start until it does not conflict with the last running job, and this tag will be automatically cleaned up after it starts.</p>
     *
     * @param jobName job name
     */
    void trigger(String jobName);

    /**
     * Get jobs total count.
     *
     * @return jobs total count.
     */
    int getJobsTotalCount();

    /**
     * Get all jobs brief info.
     *
     * @return all jobs brief info.
     */
    Collection<JobBriefInfo> getAllJobsBriefInfo();

    /**
     * Get job brief info.
     *
     * @param jobName job name
     * @return job brief info
     */
    JobBriefInfo getJobBriefInfo(String jobName);
}
```
6. more config
```
#config strategy, more strategy refer to official document
#cofig error handler type.  LOG,THROW,IGNORE,EMAIL,WECHAT,DINGTALK
elasticjob.job-error-handler-type=LOG
#config sharding strategy type. AVG_ALLOCATION,ODEVITY,ROUND_ROBIN
elasticjob.job-sharding-strategy-type=AVG_ALLOCATION
elasticjob.job-executor-service-handler-type=CPU

#tracing
#elasticjob.tracing.type=RDB
#multiple datasource
#elasticjob.tracing.data-source-bean-name=db1MasterSlaveRoutingDatasource

#config email notify
elasticjob.props.email.host=host
elasticjob.props.email.port=465
elasticjob.props.email.username=username
elasticjob.props.email.password=password
elasticjob.props.email.useSsl=true
elasticjob.props.email.subject=ElasticJob error message
elasticjob.props.email.from=from@xxx.xx
elasticjob.props.email.to=to1@xxx.xx,to2@xxx.xx
elasticjob.props.email.cc=cc@xxx.xx
elasticjob.props.email.bcc=bcc@xxx.xx
elasticjob.props.email.debug=false

#config wechat notify
elasticjob.props.wechat.webhook=you_webhook
elasticjob.props.wechat.connectTimeout=3000
elasticjob.props.wechat.readTimeout=5000

#config dingtalk notify
elasticjob.props.dingtalk.webhook=you_webhook
elasticjob.props.dingtalk.keyword=you_keyword
elasticjob.props.dingtalk.secret=you_secret
elasticjob.props.dingtalk.connectTimeout=3000
elasticjob.props.dingtalk.readTimeout=5000
```
