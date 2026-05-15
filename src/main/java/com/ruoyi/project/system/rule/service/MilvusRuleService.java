package com.ruoyi.project.system.rule.service;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.SearchResults;
import io.milvus.param.ConnectParam;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import io.milvus.param.R;
import io.milvus.param.collection.CreateCollectionParam;
import io.milvus.param.collection.FieldType;
import io.milvus.param.collection.HasCollectionParam;
import io.milvus.param.collection.LoadCollectionParam;
import io.milvus.param.dml.InsertParam;
import io.milvus.param.dml.SearchParam;
import io.milvus.param.dml.UpsertParam;
import io.milvus.param.index.CreateIndexParam;
import io.milvus.response.SearchResultsWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MilvusRuleService {

    @Value("${milvus.host:127.0.0.1}")
    private String host;

    @Value("${milvus.port:19530}")
    private int port;

    private MilvusServiceClient milvusClient;
    private final String COLLECTION_NAME = "archive_appraisal_rules";
    private final int BGE_DIMENSION = 1024; // BGE-large-zh 输出维度

    @PostConstruct
    public void init() {
        milvusClient = new MilvusServiceClient(
                ConnectParam.newBuilder()
                        .withHost(host)
                        .withPort(port)
                        .build()
        );
        initCollection();
    }

    /**
     * 1. 结构初始化：建表、建索引并加载进内存
     */
    private void initCollection() {
        R<Boolean> hasCol = milvusClient.hasCollection(HasCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build());
        if (hasCol.getData() != null && hasCol.getData()) {
            return; // 已经存在则跳过
        }

        // 定义字段：主键ID, 门类代号, 向量特征
        FieldType ruleIdField = FieldType.newBuilder().withName("rule_id").withDataType(io.milvus.grpc.DataType.VarChar).withMaxLength(64).withPrimaryKey(true).build();
        FieldType categoryCodeField = FieldType.newBuilder().withName("category_code").withDataType(io.milvus.grpc.DataType.VarChar).withMaxLength(32).build();
        FieldType vectorField = FieldType.newBuilder().withName("rule_vector").withDataType(io.milvus.grpc.DataType.FloatVector).withDimension(BGE_DIMENSION).build();

        CreateCollectionParam createParam = CreateCollectionParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withDescription("档案鉴定规则知识库向量表")
                .addFieldType(ruleIdField)
                .addFieldType(categoryCodeField)
                .addFieldType(vectorField)
                .build();
        milvusClient.createCollection(createParam);

        // 创建 HNSW 索引以支撑毫秒级高并发召回
        milvusClient.createIndex(CreateIndexParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFieldName("rule_vector")
                .withIndexType(IndexType.HNSW)
                .withMetricType(MetricType.COSINE) // 语义相似度推荐使用余弦距离
                .withExtraParam("{\"M\":16,\"efConstruction\":200}")
                .build());

        // 加载集合至显存/内存以备查询
        milvusClient.loadCollection(LoadCollectionParam.newBuilder().withCollectionName(COLLECTION_NAME).build());
    }

    /**
     * 2. 特征入库：将提取出的规则特征推入向量引擎
     */
    public void upsertRuleVector(String ruleId, String categoryCode, List<Float> vector) {
        if (vector == null || vector.isEmpty()) return;

        List<InsertParam.Field> fields = new ArrayList<>();
        fields.add(new InsertParam.Field("rule_id", Collections.singletonList(ruleId)));
        fields.add(new InsertParam.Field("category_code", Collections.singletonList(categoryCode != null ? categoryCode : "")));
        fields.add(new InsertParam.Field("rule_vector", Collections.singletonList(vector)));

        milvusClient.upsert(UpsertParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withFields(fields)
                .build());
    }

    /**
     * 3. 核心召回：基于向量检索 Top-K 规则 (可附带门类硬过滤)
     */
    public List<SearchResultsWrapper.IDScore> searchRules(List<Float> targetVector, String categoryCode, int topK) {
        SearchParam.Builder builder = SearchParam.newBuilder()
                .withCollectionName(COLLECTION_NAME)
                .withMetricType(MetricType.COSINE)
                .withTopK(topK)
                .withVectors(Collections.singletonList(targetVector))
                .withVectorFieldName("rule_vector")
                .withParams("{\"ef\":64}");

        // 💡 绝招：向量与标量混合查询，绝对杜绝跨门类“张冠李戴”
        if (categoryCode != null && !categoryCode.isEmpty()) {
            builder.withExpr("category_code == '" + categoryCode + "'");
        }

        R<SearchResults> resp = milvusClient.search(builder.build());
        if (resp.getStatus() == R.Status.Success.getCode()) {
            SearchResultsWrapper wrapper = new SearchResultsWrapper(resp.getData().getResults());
            return wrapper.getIDScore(0);
        }
        return new ArrayList<>();
    }
}