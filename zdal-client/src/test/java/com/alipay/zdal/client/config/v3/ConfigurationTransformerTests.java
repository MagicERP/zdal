/**
 * 
 */
package com.alipay.zdal.client.config.v3;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alipay.zdal.client.config.DataSourceConfigType;
import com.alipay.zdal.client.config.DataSourceParameter;
import com.alipay.zdal.client.config.ShardTableRule;
import com.alipay.zdal.client.config.ZdalConfig;
import com.alipay.zdal.client.config.utils.ZdalConfigParserUtils;
import com.alipay.zdal.client.exceptions.ZdalClientException;
import com.alipay.zdal.common.DBType;

import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * @author <a href="mailto:xiang.yangx@alipay.com">Yang Xiang</a>
 *
 */
public class ConfigurationTransformerTests {

	private static final String PHYSICAL_DS_MAP = "physicalDsMap";

	private static final String IDC_NAME2 = "idcName";

	private static final String DBMODE = "dbmode";

	private static final String APP_NAME2 = "appName";

	private static final String APP_DATA_SOURCE_LIST = "appDataSourceList";

	private static final String SPRING_ZDAL_DS_TEMPLATE_FTL = "spring-zdal-ds-template.ftl";
	
	private static final String SPRING_ZDAL_RULE_TEMPLATE_FTL = "spring-zdal-rule-template.ftl";

	private static final String MASTER_LOGIC_PHYSICS_DS_NAME_MAP = "masterLogicPhysicsDsNameMap";

	private static final String FAILOVER_LOGIC_PHYSICS_DS_NAME_MAP = "failoverLogicPhysicsDsNameMap";

	private static final String TEMPLATE_DIR = "./src/test/resources/config";

	public static String zdalV2FilePath = "config/trade-dev-rz00a-ds.xml";
	
	public static String zdalV3DsFilePath = "";
	
	public static String zdalV3RuleFilePath = "";
	
	public static String ZDAL_DS_TEMPALTE = "classpath:/config/spring-zdal-ds-template.ftl";
	
	public static String ZDAL_RULE_TEMPALTE = "classpath:/config/spring-zdal-rule-template.ftl";
	
	public static final String APP_NAME = "trade";
	
	public static final String APP_DS_NAME = "";
	
	public static final String DB_MODE = "";
	
	public static final String IDC_NAME = "";
		
	/** �߼�����master�ĺ�׺. */
    private static final String                        MASTER_RULE                   = "_masterRule";

    /** �߼�������slave�ĺ�׺. */
    private static final String                        SLAVE_RULE                    = "_slaveRule";
    
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public void testTransformConfigurationFromV2ToV3() {
		
		InputStream configStream = null;
        Map<String, ZdalConfig> zdalV2ConfigMap = null;
        
//        File configFile = new File(zdalV2FilePath);
        try {
        	//Step 1. Load Zdal 2.0 configuration up
        	configStream = this.getClass().getClassLoader().getResourceAsStream(zdalV2FilePath);
        	assertNotNull(configStream);
            zdalV2ConfigMap = ZdalConfigParserUtils.parseConfig(configStream, APP_NAME, DB_MODE,
            		IDC_NAME);
            assertNotNull(zdalV2ConfigMap);
            //Step 2. Put configuration into a map
            Map<String, Object> valueMap = populateDsMap(zdalV2ConfigMap);
            
            Map<String, Object> ruleValueMap = populateRuleMap(zdalV2ConfigMap);
            
            generateFiles(valueMap, ruleValueMap);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        } finally {
            if (configStream != null) {
                try {
                	configStream.close();
                } catch (IOException e) {
                	e.printStackTrace();
                    fail();
                }
            }
        }
	}

	protected Map<String, Object> populateDsMap(
			Map<String, ZdalConfig> zdalV2ConfigMap) {
		Map<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put(APP_DATA_SOURCE_LIST, zdalV2ConfigMap.values());
		valueMap.put(APP_NAME2, "trade");
		valueMap.put(DBMODE, "dev");
		valueMap.put(IDC_NAME2, "rz00a");
		//FOR DS
		Map<String, List<String>> failoverLogicPhysicsDsNameMap = new HashMap<String,List<String>>();
		Map<String, List<String>> masterLogicPhysicsDsNameMap = new HashMap<String,List<String>>();
		List<String> logDsNameList = null;
		valueMap.put(FAILOVER_LOGIC_PHYSICS_DS_NAME_MAP, failoverLogicPhysicsDsNameMap);
		valueMap.put(MASTER_LOGIC_PHYSICS_DS_NAME_MAP, masterLogicPhysicsDsNameMap);
		
		valueMap.put(PHYSICAL_DS_MAP, new HashSet<DataSourceParameter>());
		for( Entry<String, ZdalConfig> configSet : zdalV2ConfigMap.entrySet() ){
			if( null != configSet.getValue().getFailoverLogicPhysicsDsNames()
					&& !configSet.getValue().getFailoverLogicPhysicsDsNames().isEmpty() ){
				for(Entry<String, String> dsEntry : configSet.getValue().getFailoverLogicPhysicsDsNames().entrySet() ){
					logDsNameList = failoverLogicPhysicsDsNameMap.get(dsEntry.getValue());
					if( null == logDsNameList ){
						logDsNameList = new ArrayList<String>();
						failoverLogicPhysicsDsNameMap.put(dsEntry.getValue(), logDsNameList);
					}
					logDsNameList.add(dsEntry.getKey());
				}
			}
			if( null != configSet.getValue().getMasterLogicPhysicsDsNames()
					&& !configSet.getValue().getMasterLogicPhysicsDsNames().isEmpty() ){
				for(Entry<String, String> dsEntry : configSet.getValue().getMasterLogicPhysicsDsNames().entrySet() ){
					logDsNameList = masterLogicPhysicsDsNameMap.get(dsEntry.getValue());
					if( null == logDsNameList ){
						logDsNameList = new ArrayList<String>();
						masterLogicPhysicsDsNameMap.put(dsEntry.getValue(), logDsNameList);
					}
					logDsNameList.add(dsEntry.getKey());
				}
			}
		}
		return valueMap;
	}

	protected Map<String, Object> populateRuleMap(
			Map<String, ZdalConfig> zdalV2ConfigMap) {
		//FOR RULE
		Map<String, Object> ruleValueMap = new HashMap<String, Object>();
		Map<String, Set<ShardTableRule>> allRuleMap = new HashMap<String, Set<ShardTableRule>>();
		Map<String, Collection<ShardTableRule>> masterRuleMap = new HashMap<String, Collection<ShardTableRule>>();
		Map<String, Collection<ShardTableRule>> slaveRuleMap = new HashMap<String, Collection<ShardTableRule>>();
		Map<String, Collection<ShardTableRule>> readwriteRuleMap = new HashMap<String, Collection<ShardTableRule>>();
		Map<String, DBType> dbTypeMap = new HashMap<String, DBType>();
		Map<String, String> tbSuffixPaddingMap = new HashMap<String, String>();
		Map<String, String> tbNumForEachDbMap = new HashMap<String, String>();
		List<String> appDsNameList = new ArrayList<String>();
		ruleValueMap.put("masterRuleMap", masterRuleMap);
		ruleValueMap.put("slaveRuleMap", slaveRuleMap);
		ruleValueMap.put("readwriteRuleMap", readwriteRuleMap);
		ruleValueMap.put("tbSuffixPaddingMap", tbSuffixPaddingMap);
		ruleValueMap.put("tbNumForEachDbMap", tbNumForEachDbMap);
		ruleValueMap.put("appDsNameList", appDsNameList);
		ruleValueMap.put("dbTypeMap", dbTypeMap);
		ruleValueMap.put("allRuleMap", allRuleMap);
		Collection<ShardTableRule> shardRuleList = null;
		Set<ShardTableRule> shardRuleSet = null;
		for( Entry<String, ZdalConfig> configSet : zdalV2ConfigMap.entrySet() ){
			if( !MapUtils.isEmpty(configSet.getValue().getShardTableRules()) ){
				appDsNameList.add(configSet.getKey());
				if( null != configSet.getValue().getDataSourceConfigType()){
					shardRuleList = configSet.getValue().getShardTableRules().values();
					if ( configSet.getValue().getDataSourceConfigType() == DataSourceConfigType.SHARD_FAILOVER) {
						masterRuleMap.put(configSet.getKey(), shardRuleList);
						slaveRuleMap.put(configSet.getKey(), shardRuleList);
		        	}else if( configSet.getValue().getDataSourceConfigType() == DataSourceConfigType.SHARD_GROUP ){
		        		readwriteRuleMap.put(configSet.getKey(), shardRuleList);
		        	}
					shardRuleSet = allRuleMap.get(configSet.getKey());
					if( null == shardRuleSet ){
						shardRuleSet = new HashSet<ShardTableRule>();
						allRuleMap.put(configSet.getKey(), shardRuleSet);
					}
					dbTypeMap.put(configSet.getKey(), configSet.getValue().getDbType());
					for( ShardTableRule rule : shardRuleList ){
						shardRuleSet.add(rule);
					}
		    	}
			}
		}
		boolean diffMasterSlaveRule = false;
		if( diffMasterSlaveRule ){
			
		}
		return ruleValueMap;
	}

	protected void generateFiles(Map<String, Object> valueMap,
			Map<String, Object> ruleValueMap) throws IOException {
		File templateDir = new File(TEMPLATE_DIR);
		Configuration templateCfg = new Configuration();
		templateCfg.setDirectoryForTemplateLoading(templateDir);
		Template dsTemplate = templateCfg.getTemplate(SPRING_ZDAL_DS_TEMPLATE_FTL);
		Template ruleTemplate = templateCfg.getTemplate(SPRING_ZDAL_RULE_TEMPLATE_FTL);
		
		//Step 4. flush out
		File dsFile = new File(TEMPLATE_DIR + File.separator + APP_NAME + "-zdal-ds.xml");
		if( dsFile.exists() ){
			dsFile.delete();
		}
		File ruleFile = new File(TEMPLATE_DIR + File.separator + APP_NAME + "-zdal-rule.xml");
		if( ruleFile.exists() ){
			ruleFile.delete();
		}
		if(dsFile.createNewFile()){
			FileWriter dsWriter = new FileWriter(dsFile);
			try{
				dsTemplate.process(valueMap, dsWriter);
				dsWriter.flush();
			}catch(Exception e){
				
			}finally{
				if( null != dsWriter ) dsWriter.close();
			}
		}
		if(ruleFile.createNewFile()){
			FileWriter ruleWriter = new FileWriter(ruleFile);
			try{
				ruleTemplate.process(ruleValueMap, ruleWriter);
				ruleWriter.flush();
			}catch(Exception e){
				
			}finally{
				if( null != ruleWriter ) ruleWriter.close();
			}
		}
	}
}