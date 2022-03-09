# deeds-dapp

## Configuration options

| VARIABLE               | MANDATORY | DEFAULT VALUE | DESCRIPTION                                                                               |
|------------------------|-----------|---------------|-------------------------------------------------------------------------------------------|
| meeds.blockchain.networkUrl        | YES        |  | Blockchain HTTPs URL using infura or Alchemy by example |
| meeds.blockchain.tenantProvisioningAddress        | NO        |  | Blockchain Address of TenantProvisioningStrategy.sol contract deployed on Mainnet |
| meeds.blockchain.deedAddress        | NO        |  | Blockchain Address of Deed.sol contract deployed on Mainnet |
| meeds.elasticsearch.url        | YES        | http://127.0.0.1:9200 | Elasticsearch API HTTP URL |
| meeds.elasticsearch.username | NO        |  | Elasticsearch API Basic authentication username   |
| meeds.elasticsearch.password | NO        |  | Elasticsearch API Basic authentication password   |
| meeds.redis.channelName | NO        | channel | Channel name used for triggering events |
| meeds.redis.host | NO        | localhost | Redis Host |
| meeds.redis.port | NO        | 6379 | Redis port |
| meeds.redis.username | NO        | | Redis username if authentication enabled |
| meeds.redis.password | NO        | | Redis password if authentication enabled |
| meeds.exchange.currencyApiKey | YES        | | API Key used to retrieve currency exchange rates from api.currencyapi.com |
